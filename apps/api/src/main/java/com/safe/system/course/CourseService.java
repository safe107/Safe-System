package com.safe.system.course;

import com.safe.system.common.*;
import com.safe.system.file.*;
import java.util.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CourseService {
  private final CourseRepository repository;
  private final com.safe.system.skill.SkillRepository skillRepository;

  public CourseService(
      CourseRepository repository, com.safe.system.skill.SkillRepository skillRepository) {
    this.repository = repository;
    this.skillRepository = skillRepository;
  }

  public CourseDto.Output get(UUID id) {
    return output(repository.findById(id).orElseThrow(ApiException::notFound));
  }

  public void delete(UUID id) {
    repository.delete(repository.findById(id).orElseThrow(ApiException::notFound));
  }

  public CourseDto.Output save(UUID id, CourseDto.Input input) {
    Course entity =
        id == null ? new Course() : repository.findById(id).orElseThrow(ApiException::notFound);
    entity.name = input.name();
    entity.provider = input.provider();
    entity.category = input.category();
    Rules.url(input.sourceUrl());
    entity.sourceUrl = input.sourceUrl();
    entity.status = input.status();
    entity.progress = input.progress();
    entity.startedAt = input.startedAt();
    entity.completedAt = input.completedAt();
    entity.notes = input.notes();
    entity.certificateId = input.certificateId();
    entity.displayOrder = input.displayOrder();
    var selected = skillRepository.findAllById(input.skillIds());
    if (selected.size() != input.skillIds().size())
      throw ApiException.badRequest("Competência inexistente");
    entity.skills = new HashSet<>(selected);
    Rules.dates(input.startedAt(), input.completedAt());
    repository.saveAndFlush(entity);
    return output(entity);
  }

  public PageResponse<CourseDto.Output> list(
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
    Specification<Course> spec =
        (r, q, b) -> {
          var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
          if (search != null && !search.isBlank())
            predicates.add(b.like(b.lower(r.get("name")), "%" + Rules.like(search) + "%", '\\'));
          if (category != null && !category.isBlank())
            predicates.add(b.equal(r.get("category"), category));
          if (status != null && !status.isBlank()) predicates.add(b.equal(r.get("status"), status));
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

  public CourseDto.Output output(Course e) {
    return output(e, false);
  }

  public CourseDto.Output output(Course e, boolean publicOnly) {
    return new CourseDto.Output(
        e.id,
        e.name,
        e.provider,
        e.category,
        e.sourceUrl,
        e.status,
        e.progress,
        e.startedAt,
        e.completedAt,
        e.notes,
        e.certificateId,
        e.displayOrder,
        e.skills.stream()
            .filter(s -> !publicOnly || s.published)
            .map(s -> s.id)
            .collect(java.util.stream.Collectors.toSet()),
        e.createdAt,
        e.updatedAt);
  }
}
