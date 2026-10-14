package com.safe.system.experience;

import com.safe.system.common.*;
import com.safe.system.file.*;
import java.util.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExperienceService {
  private final ExperienceRepository repository;
  private final com.safe.system.skill.SkillRepository skillRepository;

  public ExperienceService(
      ExperienceRepository repository, com.safe.system.skill.SkillRepository skillRepository) {
    this.repository = repository;
    this.skillRepository = skillRepository;
  }

  public ExperienceDto.Output get(UUID id) {
    return output(repository.findById(id).orElseThrow(ApiException::notFound));
  }

  public void delete(UUID id) {
    repository.delete(repository.findById(id).orElseThrow(ApiException::notFound));
  }

  public ExperienceDto.Output save(UUID id, ExperienceDto.Input input) {
    Experience entity =
        id == null ? new Experience() : repository.findById(id).orElseThrow(ApiException::notFound);
    entity.organization = input.organization();
    entity.type = input.type();
    entity.startedAt = input.startedAt();
    entity.endedAt = input.endedAt();
    entity.current = input.current();
    entity.published = input.published();
    entity.displayOrder = input.displayOrder();
    if (!input.translations().containsKey("pt-BR")
        || input.translations().get("pt-BR").role() == null
        || input.translations().get("pt-BR").role().isBlank())
      throw ApiException.badRequest("Tradução PT-BR com role é obrigatória");
    entity.translations.clear();
    input
        .translations()
        .forEach(
            (locale, dto) -> {
              var t = new ExperienceTranslation();
              t.role = dto.role();
              t.description = dto.description();
              t.responsibilities = dto.responsibilities();
              t.results = dto.results();
              entity.translations.put(locale, t);
            });
    var selected = skillRepository.findAllById(input.skillIds());
    if (selected.size() != input.skillIds().size())
      throw ApiException.badRequest("Competência inexistente");
    entity.skills = new HashSet<>(selected);
    Rules.dates(input.startedAt(), input.endedAt());
    if (input.current() && input.endedAt() != null)
      throw ApiException.badRequest("Experiência atual não pode ter data de término");
    repository.saveAndFlush(entity);
    return output(entity);
  }

  public PageResponse<ExperienceDto.Output> list(
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
    Specification<Experience> spec =
        (r, q, b) -> {
          var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
          if (publicOnly) predicates.add(b.isTrue(r.get("published")));
          if (search != null && !search.isBlank()) {
            var t = r.join("translations");
            predicates.add(b.like(b.lower(t.get("role")), "%" + Rules.like(search) + "%", '\\'));
            q.distinct(true);
          }
          if (skill != null) {
            predicates.add(b.equal(r.join("skills").get("id"), skill));
            q.distinct(true);
          }
          return b.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    return PageResponse.from(
        repository
            .findAll(spec, PageResponse.request(page, size, sort))
            .map(e -> output(e, publicOnly)));
  }

  public ExperienceDto.Output publication(UUID id, boolean published) {
    var e = repository.findById(id).orElseThrow(ApiException::notFound);
    e.published = published;
    return output(repository.save(e));
  }

  public ExperienceDto.Output output(Experience e) {
    return output(e, false);
  }

  public ExperienceDto.Output output(Experience e, boolean publicOnly) {
    var translations = new HashMap<String, ExperienceDto.Translation>();
    e.translations.forEach(
        (locale, t) ->
            translations.put(
                locale,
                new ExperienceDto.Translation(
                    t.role, t.description, t.responsibilities, t.results)));
    return new ExperienceDto.Output(
        e.id,
        e.organization,
        e.type,
        e.startedAt,
        e.endedAt,
        e.current,
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
