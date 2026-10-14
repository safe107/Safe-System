package com.safe.system.certificate;

import com.safe.system.common.AuditedEntity;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "certificates")
public class Certificate extends AuditedEntity {
  @Column(name = "slug")
  public String slug;

  @Column(name = "issuer")
  public String issuer;

  @Column(name = "category")
  public String category;

  @Column(name = "issued_at")
  public java.time.LocalDate issuedAt;

  @Column(name = "expires_at")
  public java.time.LocalDate expiresAt;

  @Column(name = "hours")
  public Integer hours;

  @Column(name = "credential_id")
  public String credentialId;

  @Column(name = "verification_url")
  public String verificationUrl;

  @Column(name = "file_id")
  public UUID fileId;

  @Column(name = "published")
  public boolean published;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
      name = "certificate_translations",
      joinColumns = @JoinColumn(name = "certificate_id"))
  @MapKeyColumn(name = "locale")
  public Map<String, CertificateTranslation> translations = new HashMap<>();

  @ManyToMany
  @JoinTable(
      name = "certificate_skills",
      joinColumns = @JoinColumn(name = "certificate_id"),
      inverseJoinColumns = @JoinColumn(name = "skill_id"))
  public Set<com.safe.system.skill.Skill> skills = new HashSet<>();
}
