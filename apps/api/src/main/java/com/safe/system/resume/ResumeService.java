package com.safe.system.resume;

import com.safe.system.common.*;
import com.safe.system.file.*;
import java.util.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ResumeService {
  private final ResumeRepository repository;
  private final FileService files;

  public ResumeService(ResumeRepository repository, FileService files) {
    this.repository = repository;
    this.files = files;
  }

  public ResumeDto.Output get(UUID id) {
    return output(repository.findById(id).orElseThrow(ApiException::notFound));
  }

  public void delete(UUID id) {
    repository.delete(repository.findById(id).orElseThrow(ApiException::notFound));
  }

  public ResumeDto.Output save(UUID id, ResumeDto.Input input) {
    Resume entity =
        id == null ? new Resume() : repository.findById(id).orElseThrow(ApiException::notFound);
    entity.title = input.title();
    entity.locale = input.locale();
    entity.targetProfile = input.targetProfile();
    entity.description = input.description();
    entity.fileId = input.fileId();
    entity.isPrimary = input.isPrimary();
    entity.published = input.published();
    entity.displayOrder = input.displayOrder();
    if (entity.fileId != null) files.requirePdf(entity.fileId);
    if (entity.published && entity.fileId == null)
      throw ApiException.badRequest("Documento PDF obrigatório para publicar");
    if (entity.isPrimary && !entity.published)
      throw ApiException.badRequest("Currículo principal deve estar publicado");
    if (entity.isPrimary)
      repository.findAll().stream()
          .filter(r -> r.isPrimary && r.locale.equals(entity.locale) && !r.id.equals(entity.id))
          .forEach(
              r -> {
                r.isPrimary = false;
                repository.save(r);
              });
    repository.flush();
    repository.saveAndFlush(entity);
    return output(entity);
  }

  public PageResponse<ResumeDto.Output> list(
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
    Specification<Resume> spec =
        (r, q, b) -> {
          var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
          if (publicOnly) predicates.add(b.isTrue(r.get("published")));
          if (search != null && !search.isBlank())
            predicates.add(b.like(b.lower(r.get("title")), "%" + Rules.like(search) + "%", '\\'));
          if (locale != null && !locale.isBlank()) predicates.add(b.equal(r.get("locale"), locale));
          return b.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    return PageResponse.from(
        repository
            .findAll(spec, PageResponse.request(page, size, sort))
            .map(e -> output(e, publicOnly)));
  }

  public ResumeDto.Output publication(UUID id, boolean published) {
    var e = repository.findById(id).orElseThrow(ApiException::notFound);
    if (published && e.fileId == null) throw ApiException.badRequest("PDF obrigatório");
    e.published = published;
    if (!published) e.isPrimary = false;
    return output(repository.save(e));
  }

  public ResumeDto.Output output(Resume e) {
    return output(e, false);
  }

  public ResumeDto.Output output(Resume e, boolean publicOnly) {
    return new ResumeDto.Output(
        e.id,
        e.title,
        e.locale,
        e.targetProfile,
        e.description,
        e.fileId,
        e.isPrimary,
        e.published,
        e.displayOrder,
        e.createdAt,
        e.updatedAt);
  }
}
