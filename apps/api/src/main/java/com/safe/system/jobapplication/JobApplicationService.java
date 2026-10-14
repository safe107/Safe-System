package com.safe.system.jobapplication;

import com.safe.system.common.*;
import com.safe.system.file.*;
import java.util.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JobApplicationService {
  private final JobApplicationRepository repository;
  private final com.safe.system.jobapplication.ApplicationHistory history;

  public JobApplicationService(
      JobApplicationRepository repository,
      com.safe.system.jobapplication.ApplicationHistory history) {
    this.repository = repository;
    this.history = history;
  }

  public JobApplicationDto.Output get(UUID id) {
    return output(repository.findById(id).orElseThrow(ApiException::notFound));
  }

  public void delete(UUID id) {
    repository.delete(repository.findById(id).orElseThrow(ApiException::notFound));
  }

  public JobApplicationDto.Output save(UUID id, JobApplicationDto.Input input) {
    JobApplication entity =
        id == null
            ? new JobApplication()
            : repository.findById(id).orElseThrow(ApiException::notFound);
    String previous = entity.status;
    entity.company = input.company();
    entity.position = input.position();
    Rules.url(input.jobUrl());
    entity.jobUrl = input.jobUrl();
    entity.workMode = input.workMode();
    entity.salaryMin = input.salaryMin();
    entity.salaryMax = input.salaryMax();
    entity.currency = input.currency();
    entity.appliedAt = input.appliedAt();
    entity.status = input.status();
    entity.source = input.source();
    entity.resumeId = input.resumeId();
    entity.notes = input.notes();
    entity.nextAction = input.nextAction();
    entity.followUpAt = input.followUpAt();
    entity.displayOrder = input.displayOrder();
    if (input.salaryMin() != null
        && input.salaryMax() != null
        && input.salaryMin().compareTo(input.salaryMax()) > 0)
      throw ApiException.badRequest("Faixa salarial inválida");
    repository.saveAndFlush(entity);
    if (!Objects.equals(previous, entity.status))
      history.record(entity.id, previous, entity.status, null);
    return output(entity);
  }

  public PageResponse<JobApplicationDto.Output> list(
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
    Specification<JobApplication> spec =
        (r, q, b) -> {
          var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
          if (search != null && !search.isBlank())
            predicates.add(b.like(b.lower(r.get("company")), "%" + Rules.like(search) + "%", '\\'));
          if (status != null && !status.isBlank()) predicates.add(b.equal(r.get("status"), status));
          return b.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    return PageResponse.from(
        repository
            .findAll(spec, PageResponse.request(page, size, sort))
            .map(e -> output(e, publicOnly)));
  }

  public JobApplicationDto.Output output(JobApplication e) {
    return output(e, false);
  }

  public JobApplicationDto.Output output(JobApplication e, boolean publicOnly) {
    return new JobApplicationDto.Output(
        e.id,
        e.company,
        e.position,
        e.jobUrl,
        e.workMode,
        e.salaryMin,
        e.salaryMax,
        e.currency,
        e.appliedAt,
        e.status,
        e.source,
        e.resumeId,
        e.notes,
        e.nextAction,
        e.followUpAt,
        e.displayOrder,
        e.createdAt,
        e.updatedAt);
  }
}
