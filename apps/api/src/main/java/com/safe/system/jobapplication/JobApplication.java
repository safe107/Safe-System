package com.safe.system.jobapplication;

import com.safe.system.common.AuditedEntity;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "job_applications")
public class JobApplication extends AuditedEntity {
  @Column(name = "company")
  public String company;

  @Column(name = "position")
  public String position;

  @Column(name = "job_url")
  public String jobUrl;

  @Column(name = "work_mode")
  public String workMode;

  @Column(name = "salary_min")
  public java.math.BigDecimal salaryMin;

  @Column(name = "salary_max")
  public java.math.BigDecimal salaryMax;

  @Column(name = "currency")
  public String currency;

  @Column(name = "applied_at")
  public java.time.LocalDate appliedAt;

  @Column(name = "status")
  public String status;

  @Column(name = "source")
  public String source;

  @Column(name = "resume_id")
  public UUID resumeId;

  @Column(name = "notes")
  public String notes;

  @Column(name = "next_action")
  public String nextAction;

  @Column(name = "follow_up_at")
  public java.time.LocalDate followUpAt;
}
