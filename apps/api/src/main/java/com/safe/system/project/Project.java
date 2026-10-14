package com.safe.system.project;

import com.safe.system.common.AuditedEntity;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "projects")
public class Project extends AuditedEntity {
  @Column(name = "slug")
  public String slug;

  @Column(name = "category")
  public String category;

  @Column(name = "status")
  public String status;

  @Column(name = "github_url")
  public String githubUrl;

  @Column(name = "demo_url")
  public String demoUrl;

  @Column(name = "featured")
  public boolean featured;

  @Column(name = "published")
  public boolean published;

  @Column(name = "started_at")
  public java.time.LocalDate startedAt;

  @Column(name = "completed_at")
  public java.time.LocalDate completedAt;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "project_translations", joinColumns = @JoinColumn(name = "project_id"))
  @MapKeyColumn(name = "locale")
  public Map<String, ProjectTranslation> translations = new HashMap<>();

  @ManyToMany
  @JoinTable(
      name = "project_skills",
      joinColumns = @JoinColumn(name = "project_id"),
      inverseJoinColumns = @JoinColumn(name = "skill_id"))
  public Set<com.safe.system.skill.Skill> skills = new HashSet<>();

  @ElementCollection
  @CollectionTable(name = "project_media", joinColumns = @JoinColumn(name = "project_id"))
  @Column(name = "file_id")
  public Set<UUID> mediaIds = new HashSet<>();
}
