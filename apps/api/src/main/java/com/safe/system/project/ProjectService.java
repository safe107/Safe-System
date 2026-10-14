package com.safe.system.project;

import com.safe.system.common.*;
import com.safe.system.file.*;
import java.util.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectService {
  private final ProjectRepository repository;
  private final com.safe.system.skill.SkillRepository skillRepository;
  private final FileService files;

  public ProjectService(
      ProjectRepository repository,
      com.safe.system.skill.SkillRepository skillRepository,
      FileService files) {
    this.repository = repository;
    this.skillRepository = skillRepository;
    this.files = files;
  }

  public ProjectDto.Output get(UUID id) {
    return output(repository.findById(id).orElseThrow(ApiException::notFound));
  }

  public void delete(UUID id) {
    repository.delete(repository.findById(id).orElseThrow(ApiException::notFound));
  }

  public ProjectDto.Output save(UUID id, ProjectDto.Input input) {
    Project entity =
        id == null ? new Project() : repository.findById(id).orElseThrow(ApiException::notFound);
    entity.slug = input.slug();
    entity.category = input.category();
    entity.status = input.status();
    Rules.url(input.githubUrl());
    entity.githubUrl = input.githubUrl();
    Rules.url(input.demoUrl());
    entity.demoUrl = input.demoUrl();
    entity.featured = input.featured();
    entity.published = input.published();
    entity.startedAt = input.startedAt();
    entity.completedAt = input.completedAt();
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
              var t = new ProjectTranslation();
              t.title = dto.title();
              t.summary = dto.summary();
              t.description = dto.description();
              t.problem = dto.problem();
              t.solution = dto.solution();
              t.contribution = dto.contribution();
              t.architecture = dto.architecture();
              t.challenges = dto.challenges();
              t.learnings = dto.learnings();
              t.results = dto.results();
              entity.translations.put(locale, t);
            });
    var selected = skillRepository.findAllById(input.skillIds());
    if (selected.size() != input.skillIds().size())
      throw ApiException.badRequest("Competência inexistente");
    entity.skills = new HashSet<>(selected);
    input.mediaIds().forEach(fileId -> files.requireImage(fileId));
    entity.mediaIds = new HashSet<>(input.mediaIds());
    Rules.dates(input.startedAt(), input.completedAt());
    repository.saveAndFlush(entity);
    return output(entity);
  }

  public PageResponse<ProjectDto.Output> list(
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
    Specification<Project> spec =
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
          if (status != null && !status.isBlank()) predicates.add(b.equal(r.get("status"), status));
          if (skill != null) {
            predicates.add(b.equal(r.join("skills").get("id"), skill));
            q.distinct(true);
          }
          if (profile != null) {
            var sub = q.subquery(UUID.class);
            var prof = sub.from(com.safe.system.professionalprofile.ProfessionalProfile.class);
            sub.select(prof.join("projects").get("id"))
                .where(b.equal(prof.get("id"), profile), b.isTrue(prof.get("enabled")));
            predicates.add(r.get("id").in(sub));
          }
          return b.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    return PageResponse.from(
        repository
            .findAll(spec, PageResponse.request(page, size, sort))
            .map(e -> output(e, publicOnly)));
  }

  public ProjectDto.Output bySlug(String slug) {
    var e =
        repository.findBySlug(slug).filter(x -> x.published).orElseThrow(ApiException::notFound);
    return output(e, true);
  }

  public ProjectDto.Output publication(UUID id, boolean published) {
    var e = repository.findById(id).orElseThrow(ApiException::notFound);
    e.published = published;
    return output(repository.save(e));
  }

  public ProjectDto.Output output(Project e) {
    return output(e, false);
  }

  public ProjectDto.Output output(Project e, boolean publicOnly) {
    var translations = new HashMap<String, ProjectDto.Translation>();
    e.translations.forEach(
        (locale, t) ->
            translations.put(
                locale,
                new ProjectDto.Translation(
                    t.title,
                    t.summary,
                    t.description,
                    t.problem,
                    t.solution,
                    t.contribution,
                    t.architecture,
                    t.challenges,
                    t.learnings,
                    t.results)));
    return new ProjectDto.Output(
        e.id,
        e.slug,
        e.category,
        e.status,
        e.githubUrl,
        e.demoUrl,
        e.featured,
        e.published,
        e.startedAt,
        e.completedAt,
        e.displayOrder,
        translations,
        e.skills.stream()
            .filter(s -> !publicOnly || s.published)
            .map(s -> s.id)
            .collect(java.util.stream.Collectors.toSet()),
        new HashSet<>(e.mediaIds),
        e.createdAt,
        e.updatedAt);
  }
}
