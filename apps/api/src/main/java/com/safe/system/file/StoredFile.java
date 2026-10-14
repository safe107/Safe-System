package com.safe.system.file;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "files")
public class StoredFile {
  @Id public UUID id;
  public String storageKey;
  public String originalName;
  public String mimeType;
  public long sizeBytes;
  public String checksum;
  public String visibility = "private";
  public Instant createdAt;
}
