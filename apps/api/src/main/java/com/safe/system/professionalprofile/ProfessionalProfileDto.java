package com.safe.system.professionalprofile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public class ProfessionalProfileDto {
  public record Translation(@Size(max = 12000) String title, @Size(max = 12000) String summary) {}

  public record Input(
      @NotBlank @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 160) String slug,
      boolean enabled,
      UUID defaultResumeId,
      @Min(0) int displayOrder,
      @NotNull @Size(min = 1, max = 2)
          Map<@Pattern(regexp = "pt-BR|en") String, @NotNull @Valid Translation> translations,
      @NotNull @Size(max = 100) Set<UUID> skillIds,
      @NotNull @Size(max = 100) Set<UUID> projectIds) {}

  public record Output(
      UUID id,
      String slug,
      boolean enabled,
      UUID defaultResumeId,
      int displayOrder,
      Map<String, Translation> translations,
      Set<UUID> skillIds,
      Set<UUID> projectIds,
      Instant createdAt,
      Instant updatedAt) {}
}
