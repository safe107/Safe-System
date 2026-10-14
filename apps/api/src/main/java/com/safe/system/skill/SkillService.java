package com.safe.system.skill;

import com.safe.system.common.*;
import com.safe.system.file.*;
import java.util.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SkillService {
  private final SkillRepository repository;

  public SkillService(SkillRepository repository) {
    this.repository = repository;
  }

  public SkillDto.Output get(UUID id) {
    return output(repository.findById(id).orElseThrow(ApiException::notFound));
  }

  public void delete(UUID id) {
    repository.delete(repository.findById(id).orElseThrow(ApiException::notFound));
  }

  public SkillDto.Output save(UUID id, SkillDto.Input input) {
    Skill entity =
        id == null ? new Skill() : repository.findById(id).orElseThrow(ApiException::notFound);
    entity.slug = input.slug();
    entity.category = input.category();
    entity.published = input.published();
    entity.displayOrder = input.displayOrder();
    if (!input.translations().containsKey("pt-BR")
        || input.translations().get("pt-BR").name() == null
        || input.translations().get("pt-BR").name().isBlank())
      throw ApiException.badRequest("Tradução PT-BR com name é obrigatória");
    entity.translations.clear();
    input
        .translations()
        .forEach(
            (locale, dto) -> {
              var t = new SkillTranslation();
              t.name = dto.name();
              t.description = dto.description();
              entity.translations.put(locale, t);
            });
    repository.saveAndFlush(entity);
    return output(entity);
  }

  public PageResponse<SkillDto.Output> list(
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
    Specification<Skill> spec =
        (r, q, b) -> {
          var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
          if (publicOnly) predicates.add(b.isTrue(r.get("published")));
          if (search != null && !search.isBlank()) {
            var t = r.join("translations");
            predicates.add(b.like(b.lower(t.get("name")), "%" + Rules.like(search) + "%", '\\'));
            q.distinct(true);
          }
          if (category != null && !category.isBlank())
            predicates.add(b.equal(r.get("category"), category));
          return b.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    return PageResponse.from(
        repository
            .findAll(spec, PageResponse.request(page, size, sort))
            .map(e -> output(e, publicOnly)));
  }

  public SkillDto.Output bySlug(String slug) {
    var e =
        repository.findBySlug(slug).filter(x -> x.published).orElseThrow(ApiException::notFound);
    return output(e, true);
  }

  public SkillDto.Output publication(UUID id, boolean published) {
    var e = repository.findById(id).orElseThrow(ApiException::notFound);
    e.published = published;
    return output(repository.save(e));
  }

  public SkillDto.Output output(Skill e) {
    return output(e, false);
  }

  public SkillDto.Output output(Skill e, boolean publicOnly) {
    var translations = new HashMap<String, SkillDto.Translation>();
    e.translations.forEach(
        (locale, t) -> translations.put(locale, new SkillDto.Translation(t.name, t.description)));
    return new SkillDto.Output(
        e.id,
        e.slug,
        e.category,
        e.published,
        e.displayOrder,
        translations,
        e.createdAt,
        e.updatedAt);
  }
}
