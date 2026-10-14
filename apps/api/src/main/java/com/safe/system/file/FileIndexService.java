package com.safe.system.file;

import com.safe.system.common.*;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class FileIndexService {
  private final JdbcTemplate db;

  public FileIndexService(JdbcTemplate db) {
    this.db = db;
  }

  public record Entry(
      UUID id,
      String originalName,
      String mimeType,
      long sizeBytes,
      Instant createdAt,
      boolean linked) {}

  public PageResponse<Entry> list(int page, int size) {
    PageResponse.request(page, size, "recent");
    long total = db.queryForObject("SELECT count(*) FROM files", Long.class);
    var rows =
        db.query(
            "SELECT f.*, EXISTS(SELECT 1 FROM certificates WHERE file_id=f.id UNION ALL SELECT 1 FROM resumes WHERE file_id=f.id UNION ALL SELECT 1 FROM project_media WHERE file_id=f.id) AS linked FROM files f ORDER BY f.created_at DESC,f.id LIMIT ? OFFSET ?",
            (r, n) ->
                new Entry(
                    r.getObject("id", UUID.class),
                    r.getString("original_name"),
                    r.getString("mime_type"),
                    r.getLong("size_bytes"),
                    r.getTimestamp("created_at").toInstant(),
                    r.getBoolean("linked")),
            size,
            (long) page * size);
    return new PageResponse<>(rows, page, size, total, (int) Math.ceil((double) total / size));
  }
}
