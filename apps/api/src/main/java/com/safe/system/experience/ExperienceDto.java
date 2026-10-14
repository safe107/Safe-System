package com.safe.system.experience;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public class ExperienceDto {
  public record Translation(
      @Size(max = 12000) String role,
      @Size(max = 12000) String description,
      @Size(max = 12000) String responsibilities,
      @Size(max = 12000) String results) {}

  public record Input(
      @NotBlank @Size(max = 200) String organization,
      @NotBlank @Size(max = 80) String type,
      java.time.LocalDate startedAt,
      java.time.LocalDate endedAt,
      boolean current,
      boolean published,
      @Min(0) int displayOrder,
      @NotNull @Size(min = 1, max = 2)
          Map<@Pattern(regexp = "pt-BR|en") String, @NotNull @Valid Translation> translations,
      @NotNull @Size(max = 100) Set<UUID> skillIds) {}

  public record Output(
      UUID id,
      String organization,
      String type,
      java.time.LocalDate startedAt,
      java.time.LocalDate endedAt,
      boolean current,
      boolean published,
      int displayOrder,
      Map<String, Translation> translations,
      Set<UUID> skillIds,
      Instant createdAt,
      Instant updatedAt) {}
}
