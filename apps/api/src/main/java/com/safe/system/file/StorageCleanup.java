package com.safe.system.file;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StorageCleanup {
  private final JdbcTemplate db;
  private final Storage storage;

  public StorageCleanup(JdbcTemplate db, Storage storage) {
    this.db = db;
    this.storage = storage;
  }

  @Scheduled(fixedDelay = 60000, initialDelay = 60000)
  public void clean() {
    for (String key :
        db.queryForList(
            "SELECT storage_key FROM file_deletion_jobs ORDER BY created_at LIMIT 50",
            String.class)) {
      try {
        storage.delete(key);
        db.update("DELETE FROM file_deletion_jobs WHERE storage_key=?", key);
      } catch (RuntimeException e) {
        db.update("UPDATE file_deletion_jobs SET attempts=attempts+1 WHERE storage_key=?", key);
        org.slf4j.LoggerFactory.getLogger(StorageCleanup.class)
            .warn("Storage cleanup deferred; pending operation retained");
      }
    }
  }
}
