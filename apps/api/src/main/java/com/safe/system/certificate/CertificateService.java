package com.safe.system.certificate;

import com.safe.system.common.*;
import com.safe.system.file.*;
import java.util.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CertificateService {
  private final CertificateRepository repository;
  private final com.safe.system.skill.SkillRepository skillRepository;
  private final FileService files;

  public CertificateService(
      CertificateRepository repository,
      com.safe.system.skill.SkillRepository skillRepository,
      FileService files) {
    this.repository = repository;
    this.skillRepository = skillRepository;
    this.files = files;
  }

  public CertificateDto.Output get(UUID id) {
    return output(repository.findById(id).orElseThrow(ApiException::notFound));
  }

  public void delete(UUID id) {
    repository.delete(repository.findById(id).orElseThrow(ApiException::notFound));
  }

  public CertificateDto.Output save(UUID id, CertificateDto.Input input) {
    Certificate entity =
        id == null
            ? new Certificate()
            : repository.findById(id).orElseThrow(ApiException::notFound);
    entity.slug = input.slug();
    entity.issuer = input.issuer();
    entity.category = input.category();
    entity.issuedAt = input.issuedAt();
    entity.expiresAt = input.expiresAt();
    entity.hours = input.hours();
    entity.credentialId = input.credentialId();
    Rules.url(input.verificationUrl());
    entity.verificationUrl = input.verificationUrl();
    entity.fileId = input.fileId();
    entity.published = input.published();
    entity.displayOrder = input.displayOrder();
    if (!input.translations().containsKey("pt-BR")
        || input.translations().get("pt-BR").title() == null
        || input.translations().get("pt-BR").title().isBlank())
      throw ApiException.badRequest("Tradução PT-BR com title é obrigatória");
    entity.translations.clear();
    input
        .translations()
        .forEach(
            (locale, dto) -> {
              var t = new CertificateTranslation();
              t.title = dto.title();
              t.description = dto.description();
              entity.translations.put(locale, t);
            });
    var selected = skillRepository.findAllById(input.skillIds());
    if (selected.size() != input.skillIds().size())
      throw ApiException.badRequest("Competência inexistente");
    entity.skills = new HashSet<>(selected);
    if (entity.fileId != null) files.requirePdf(entity.fileId);
    if (entity.published && entity.fileId == null)
      throw ApiException.badRequest("Documento PDF obrigatório para publicar");
    Rules.dates(input.issuedAt(), input.expiresAt());
    repository.saveAndFlush(entity);
    return output(entity);
  }

  public PageResponse<CertificateDto.Output> list(
      boolean publicOnly,
      int page,
      int size,
      String sort,
      String search,
      String category,
      String status,
      UUID skill,
      UUID profile,
      String issuer,
      String locale) {
    Specification<Certificate> spec =
        (r, q, b) -> {
          var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
          if (publicOnly) predicates.add(b.isTrue(r.get("published")));
          if (search != null && !search.isBlank()) {
            var t = r.join("translations");
            predicates.add(b.like(b.lower(t.get("title")), "%" + Rules.like(search) + "%", '\\'));
            q.distinct(true);
          }
          if (category != null && !category.isBlank())
            predicates.add(b.equal(r.get("category"), category));
          if (skill != null) {
            predicates.add(b.equal(r.join("skills").get("id"), skill));
            q.distinct(true);
          }
          if (issuer != null && !issuer.isBlank())
            predicates.add(b.like(b.lower(r.get("issuer")), "%" + Rules.like(issuer) + "%", '\\'));
          return b.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    return PageResponse.from(
        repository
            .findAll(spec, PageResponse.request(page, size, sort, "issuedAt"))
            .map(e -> output(e, publicOnly)));
  }

  public CertificateDto.Output bySlug(String slug) {
    var e =
        repository.findBySlug(slug).filter(x -> x.published).orElseThrow(ApiException::notFound);
    return output(e, true);
  }

  public CertificateDto.Output publication(UUID id, boolean published) {
    var e = repository.findById(id).orElseThrow(ApiException::notFound);
    if (published && e.fileId == null) throw ApiException.badRequest("PDF obrigatório");
    e.published = published;
    return output(repository.save(e));
  }

  public CertificateDto.Output output(Certificate e) {
    return output(e, false);
  }

  public CertificateDto.Output output(Certificate e, boolean publicOnly) {
    var translations = new HashMap<String, CertificateDto.Translation>();
    e.translations.forEach(
        (locale, t) ->
            translations.put(locale, new CertificateDto.Translation(t.title, t.description)));
    return new CertificateDto.Output(
        e.id,
        e.slug,
        e.issuer,
        e.category,
        e.issuedAt,
        e.expiresAt,
        e.hours,
        e.credentialId,
        e.verificationUrl,
        e.fileId,
        e.published,
        e.displayOrder,
        translations,
        e.skills.stream()
            .filter(s -> !publicOnly || s.published)
            .map(s -> s.id)
            .collect(java.util.stream.Collectors.toSet()),
        e.createdAt,
        e.updatedAt);
  }
}
