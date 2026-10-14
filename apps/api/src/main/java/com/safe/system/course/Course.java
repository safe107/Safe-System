package com.safe.system.course;

import com.safe.system.common.AuditedEntity;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "courses")
public class Course extends AuditedEntity {
  @Column(name = "name")
  public String name;

  @Column(name = "provider")
  public String provider;

  @Column(name = "category")
  public String category;

  @Column(name = "source_url")
  public String sourceUrl;

  @Column(name = "status")
  public String status;

  @Column(name = "progress")
  public Integer progress;

  @Column(name = "started_at")
  public java.time.LocalDate startedAt;

  @Column(name = "completed_at")
  public java.time.LocalDate completedAt;

  @Column(name = "notes")
  public String notes;

  @Column(name = "certificate_id")
  public UUID certificateId;

  @ManyToMany
  @JoinTable(
      name = "course_skills",
      joinColumns = @JoinColumn(name = "course_id"),
      inverseJoinColumns = @JoinColumn(name = "skill_id"))
  public Set<com.safe.system.skill.Skill> skills = new HashSet<>();
}
