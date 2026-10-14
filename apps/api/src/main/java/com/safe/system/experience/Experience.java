package com.safe.system.experience;

import com.safe.system.common.AuditedEntity;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "experiences")
public class Experience extends AuditedEntity {
  @Column(name = "organization")
  public String organization;

  @Column(name = "type")
  public String type;

  @Column(name = "started_at")
  public java.time.LocalDate startedAt;

  @Column(name = "ended_at")
  public java.time.LocalDate endedAt;

  @Column(name = "current")
  public boolean current;

  @Column(name = "published")
  public boolean published;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
      name = "experience_translations",
      joinColumns = @JoinColumn(name = "experience_id"))
  @MapKeyColumn(name = "locale")
  public Map<String, ExperienceTranslation> translations = new HashMap<>();

  @ManyToMany
  @JoinTable(
      name = "experience_skills",
      joinColumns = @JoinColumn(name = "experience_id"),
      inverseJoinColumns = @JoinColumn(name = "skill_id"))
  public Set<com.safe.system.skill.Skill> skills = new HashSet<>();
}
