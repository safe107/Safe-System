package com.safe.system.jobapplication;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public class JobApplicationDto {
  public record Input(
      @NotBlank @Size(max = 200) String company,
      @NotBlank @Size(max = 200) String position,
      @Size(max = 1000) String jobUrl,
      @Size(max = 1000) String workMode,
      @DecimalMin("0") java.math.BigDecimal salaryMin,
      @DecimalMin("0") java.math.BigDecimal salaryMax,
      @Pattern(regexp = "[A-Z]{3}") String currency,
      java.time.LocalDate appliedAt,
      @NotBlank
          @Pattern(
              regexp =
                  "interest|sent|screening|interview|technical-test|offer|hired|rejected|withdrawn")
          String status,
      @Size(max = 1000) String source,
      UUID resumeId,
      @Size(max = 12000) String notes,
      @Size(max = 1000) String nextAction,
      java.time.LocalDate followUpAt,
      @Min(0) int displayOrder) {}

  public record Output(
      UUID id,
      String company,
      String position,
      String jobUrl,
      String workMode,
      java.math.BigDecimal salaryMin,
      java.math.BigDecimal salaryMax,
      String currency,
      java.time.LocalDate appliedAt,
      String status,
      String source,
      UUID resumeId,
      String notes,
      String nextAction,
      java.time.LocalDate followUpAt,
      int displayOrder,
      Instant createdAt,
      Instant updatedAt) {}
}
