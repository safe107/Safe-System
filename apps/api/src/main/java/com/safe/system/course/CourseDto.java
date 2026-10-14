package com.safe.system.course;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public class CourseDto {
  public record Input(
      @NotBlank @Size(max = 200) String name,
      @NotBlank @Size(max = 200) String provider,
      @Size(max = 1000) String category,
      @Size(max = 1000) String sourceUrl,
      @NotBlank @Pattern(regexp = "planned|in-progress|completed|paused") String status,
      @Min(0) @Max(100) Integer progress,
      java.time.LocalDate startedAt,
      java.time.LocalDate completedAt,
      @Size(max = 12000) String notes,
      UUID certificateId,
      @Min(0) int displayOrder,
      @NotNull @Size(max = 100) Set<UUID> skillIds) {}

  public record Output(
      UUID id,
      String name,
      String provider,
      String category,
      String sourceUrl,
      String status,
      Integer progress,
      java.time.LocalDate startedAt,
      java.time.LocalDate completedAt,
      String notes,
      UUID certificateId,
      int displayOrder,
      Set<UUID> skillIds,
      Instant createdAt,
      Instant updatedAt) {}
}
