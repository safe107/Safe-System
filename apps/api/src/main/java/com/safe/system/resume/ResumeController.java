package com.safe.system.resume;

import com.safe.system.common.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/resumes")
public class ResumeController {
  private final ResumeService service;

  public ResumeController(ResumeService service) {
    this.service = service;
  }

  @GetMapping
  public PageResponse<ResumeDto.Output> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "relevance") String sort,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) UUID skill,
      @RequestParam(required = false) UUID professionalProfile,
      @RequestParam(required = false) String issuer,
      @RequestParam(required = false) String locale) {
    return service.list(
        false,
        page,
        size,
        sort,
        search,
        category,
        status,
        skill,
        professionalProfile,
        issuer,
        locale);
  }

  @GetMapping("/{id}")
  public ResumeDto.Output get(@PathVariable UUID id) {
    return service.get(id);
  }

  @PostMapping
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public ResumeDto.Output create(@Valid @RequestBody ResumeDto.Input input) {
    return service.save(null, input);
  }

  @PutMapping("/{id}")
  public ResumeDto.Output edit(@PathVariable UUID id, @Valid @RequestBody ResumeDto.Input input) {
    return service.save(id, input);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }

  public record Publication(boolean published) {}

  @PatchMapping("/{id}/publication")
  public ResumeDto.Output publication(@PathVariable UUID id, @RequestBody Publication input) {
    return service.publication(id, input.published());
  }
}
