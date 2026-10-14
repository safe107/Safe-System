package com.safe.system.certificate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public class CertificateDto {
  public record Translation(
      @Size(max = 12000) String title, @Size(max = 12000) String description) {}

  public record Input(
      @NotBlank @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 160) String slug,
      @NotBlank @Size(max = 200) String issuer,
      @NotBlank @Size(max = 80) String category,
      java.time.LocalDate issuedAt,
      java.time.LocalDate expiresAt,
      @Min(1) @Max(10000) Integer hours,
      @Size(max = 1000) String credentialId,
      @Size(max = 1000) String verificationUrl,
      UUID fileId,
      boolean published,
      @Min(0) int displayOrder,
      @NotNull @Size(min = 1, max = 2)
          Map<@Pattern(regexp = "pt-BR|en") String, @NotNull @Valid Translation> translations,
      @NotNull @Size(max = 100) Set<UUID> skillIds) {}

  public record Output(
      UUID id,
      String slug,
      String issuer,
      String category,
      java.time.LocalDate issuedAt,
      java.time.LocalDate expiresAt,
      Integer hours,
      String credentialId,
      String verificationUrl,
      UUID fileId,
      boolean published,
      int displayOrder,
      Map<String, Translation> translations,
      Set<UUID> skillIds,
      Instant createdAt,
      Instant updatedAt) {}
}
