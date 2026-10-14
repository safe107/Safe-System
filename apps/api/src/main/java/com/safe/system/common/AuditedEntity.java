package com.safe.system.common;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
public abstract class AuditedEntity {
  @Id public UUID id;

  @Column(nullable = false, updatable = false)
  public Instant createdAt;

  @Column(nullable = false)
  public Instant updatedAt;

  @Column(nullable = false)
  public int displayOrder;

  @PrePersist
  void insert() {
    if (id == null) id = UUID.randomUUID();
    createdAt = Instant.now();
    updatedAt = createdAt;
  }

  @PreUpdate
  void update() {
    updatedAt = Instant.now();
  }
}
