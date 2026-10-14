package com.safe.system.skill;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public class SkillDto {
  public record Translation(
      @Size(max = 12000) String name, @Size(max = 12000) String description) {}

  public record Input(
      @NotBlank @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 160) String slug,
      @NotBlank @Size(max = 80) String category,
      boolean published,
      @Min(0) int displayOrder,
      @NotNull @Size(min = 1, max = 2)
          Map<@Pattern(regexp = "pt-BR|en") String, @NotNull @Valid Translation> translations) {}

  public record Output(
      UUID id,
      String slug,
      String category,
      boolean published,
      int displayOrder,
      Map<String, Translation> translations,
      Instant createdAt,
      Instant updatedAt) {}
}
