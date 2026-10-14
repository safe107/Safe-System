package com.safe.system.professionalprofile;

import com.safe.system.common.*;
import com.safe.system.file.*;
import java.util.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProfessionalProfileService {
  private final ProfessionalProfileRepository repository;
  private final com.safe.system.skill.SkillRepository skillRepository;
  private final com.safe.system.project.ProjectRepository projectRepository;
  private final com.safe.system.resume.ResumeRepository resumeRepository;

  public ProfessionalProfileService(
      ProfessionalProfileRepository repository,
      com.safe.system.skill.SkillRepository skillRepository,
      com.safe.system.project.ProjectRepository projectRepository,
      com.safe.system.resume.ResumeRepository resumeRepository) {
    this.repository = repository;
    this.skillRepository = skillRepository;
    this.projectRepository = projectRepository;
    this.resumeRepository = resumeRepository;
  }

  public ProfessionalProfileDto.Output get(UUID id) {
    return output(repository.findById(id).orElseThrow(ApiException::notFound));
  }

  public void delete(UUID id) {
    repository.delete(repository.findById(id).orElseThrow(ApiException::notFound));
  }

  public ProfessionalProfileDto.Output save(UUID id, ProfessionalProfileDto.Input input) {
    ProfessionalProfile entity =
        id == null
            ? new ProfessionalProfile()
            : repository.findById(id).orElseThrow(ApiException::notFound);
    entity.slug = input.slug();
    entity.enabled = input.enabled();
    entity.defaultResumeId = input.defaultResumeId();
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
              var t = new ProfessionalProfileTranslation();
              t.title = dto.title();
              t.summary = dto.summary();
              entity.translations.put(locale, t);
            });
    var selected = skillRepository.findAllById(input.skillIds());
    if (selected.size() != input.skillIds().size())
      throw ApiException.badRequest("Competência inexistente");
    entity.skills = new HashSet<>(selected);
    var projects = projectRepository.findAllById(input.projectIds());
    if (projects.size() != input.projectIds().size())
      throw ApiException.badRequest("Projeto inexistente");
    entity.projects = new HashSet<>(projects);
    repository.saveAndFlush(entity);
    return output(entity);
  }

  public PageResponse<ProfessionalProfileDto.Output> list(
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
    Specification<ProfessionalProfile> spec =
        (r, q, b) -> {
          var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
          if (publicOnly) predicates.add(b.isTrue(r.get("enabled")));
          if (search != null && !search.isBlank()) {
            var t = r.join("translations");
            predicates.add(b.like(b.lower(t.get("title")), "%" + Rules.like(search) + "%", '\\'));
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

  public ProfessionalProfileDto.Output bySlug(String slug) {
    var e = repository.findBySlug(slug).filter(x -> x.enabled).orElseThrow(ApiException::notFound);
    return output(e, true);
  }

  public ProfessionalProfileDto.Output output(ProfessionalProfile e) {
    return output(e, false);
  }

  public ProfessionalProfileDto.Output output(ProfessionalProfile e, boolean publicOnly) {
    var translations = new HashMap<String, ProfessionalProfileDto.Translation>();
    e.translations.forEach(
        (locale, t) ->
            translations.put(locale, new ProfessionalProfileDto.Translation(t.title, t.summary)));
    return new ProfessionalProfileDto.Output(
        e.id,
        e.slug,
        e.enabled,
        publicOnly && e.defaultResumeId != null
            ? resumeRepository
                .findById(e.defaultResumeId)
                .filter(r -> r.published)
                .map(r -> r.id)
                .orElse(null)
            : e.defaultResumeId,
        e.displayOrder,
        translations,
        e.skills.stream()
            .filter(s -> !publicOnly || s.published)
            .map(s -> s.id)
            .collect(java.util.stream.Collectors.toSet()),
        e.projects.stream()
            .filter(p -> !publicOnly || p.published)
            .map(p -> p.id)
            .collect(java.util.stream.Collectors.toSet()),
        e.createdAt,
        e.updatedAt);
  }
}
