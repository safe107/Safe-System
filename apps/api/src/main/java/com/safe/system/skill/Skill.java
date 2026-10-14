package com.safe.system.skill;

import com.safe.system.common.AuditedEntity;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "skills")
public class Skill extends AuditedEntity {
  @Column(name = "slug")
  public String slug;

  @Column(name = "category")
  public String category;

  @Column(name = "published")
  public boolean published;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "skill_translations", joinColumns = @JoinColumn(name = "skill_id"))
  @MapKeyColumn(name = "locale")
  public Map<String, SkillTranslation> translations = new HashMap<>();
}
