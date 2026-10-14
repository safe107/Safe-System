package com.safe.system.resume;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public class ResumeDto {
  public record Input(
      @NotBlank @Size(max = 200) String title,
      @NotBlank @Pattern(regexp = "pt-BR|en") String locale,
      @Size(max = 1000) String targetProfile,
      @Size(max = 12000) String description,
      UUID fileId,
      boolean isPrimary,
      boolean published,
      @Min(0) int displayOrder) {}

  public record Output(
      UUID id,
      String title,
      String locale,
      String targetProfile,
      String description,
      UUID fileId,
      boolean isPrimary,
      boolean published,
      int displayOrder,
      Instant createdAt,
      Instant updatedAt) {}
}
