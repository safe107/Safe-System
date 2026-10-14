package com.safe.system.professionalprofile;

import com.safe.system.common.AuditedEntity;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "professional_profiles")
public class ProfessionalProfile extends AuditedEntity {
  @Column(name = "slug")
  public String slug;

  @Column(name = "enabled")
  public boolean enabled;

  @Column(name = "default_resume_id")
  public UUID defaultResumeId;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
      name = "professional_profile_translations",
      joinColumns = @JoinColumn(name = "professional_profile_id"))
  @MapKeyColumn(name = "locale")
  public Map<String, ProfessionalProfileTranslation> translations = new HashMap<>();

  @ManyToMany
  @JoinTable(
      name = "professional_profile_skills",
      joinColumns = @JoinColumn(name = "professional_profile_id"),
      inverseJoinColumns = @JoinColumn(name = "skill_id"))
  public Set<com.safe.system.skill.Skill> skills = new HashSet<>();

  @ManyToMany
  @JoinTable(
      name = "profile_projects",
      joinColumns = @JoinColumn(name = "profile_id"),
      inverseJoinColumns = @JoinColumn(name = "project_id"))
  public Set<com.safe.system.project.Project> projects = new HashSet<>();
}
