package com.safe.system.project;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public class ProjectDto {
  public record Translation(
      @Size(max = 12000) String title,
      @Size(max = 12000) String summary,
      @Size(max = 12000) String description,
      @Size(max = 12000) String problem,
      @Size(max = 12000) String solution,
      @Size(max = 12000) String contribution,
      @Size(max = 12000) String architecture,
      @Size(max = 12000) String challenges,
      @Size(max = 12000) String learnings,
      @Size(max = 12000) String results) {}

  public record Input(
      @NotBlank @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 160) String slug,
      @NotBlank @Size(max = 80) String category,
      @NotBlank @Pattern(regexp = "planned|in-progress|completed|archived") String status,
      @Size(max = 1000) String githubUrl,
      @Size(max = 1000) String demoUrl,
      boolean featured,
      boolean published,
      java.time.LocalDate startedAt,
      java.time.LocalDate completedAt,
      @Min(0) int displayOrder,
      @NotNull @Size(min = 1, max = 2)
          Map<@Pattern(regexp = "pt-BR|en") String, @NotNull @Valid Translation> translations,
      @NotNull @Size(max = 100) Set<UUID> skillIds,
      @NotNull @Size(max = 30) Set<UUID> mediaIds) {}

  public record Output(
      UUID id,
      String slug,
      String category,
      String status,
      String githubUrl,
      String demoUrl,
      boolean featured,
      boolean published,
      java.time.LocalDate startedAt,
      java.time.LocalDate completedAt,
      int displayOrder,
      Map<String, Translation> translations,
      Set<UUID> skillIds,
      Set<UUID> mediaIds,
      Instant createdAt,
      Instant updatedAt) {}
}
