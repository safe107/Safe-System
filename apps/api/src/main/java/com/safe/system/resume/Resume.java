package com.safe.system.resume;

import com.safe.system.common.AuditedEntity;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "resumes")
public class Resume extends AuditedEntity {
  @Column(name = "title")
  public String title;

  @Column(name = "locale")
  public String locale;

  @Column(name = "target_profile")
  public String targetProfile;

  @Column(name = "description")
  public String description;

  @Column(name = "file_id")
  public UUID fileId;

  @Column(name = "is_primary")
  public boolean isPrimary;

  @Column(name = "published")
  public boolean published;
}
