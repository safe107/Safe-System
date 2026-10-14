package com.safe.system.security;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "admin_users")
public class AdminUser {
  @Id public UUID id;

  @Column(nullable = false, unique = true)
  public String username;

  @Column(nullable = false)
  public String passwordHash;

  public boolean enabled = true;
  public boolean singleton = true;
  public Instant createdAt;
  public Instant updatedAt;
}
