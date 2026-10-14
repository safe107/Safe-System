package com.safe.system.jobapplication;

import com.safe.system.common.ApiException;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApplicationHistory {
  private final JdbcTemplate db;

  public ApplicationHistory(JdbcTemplate db) {
    this.db = db;
  }

  public record Event(
      UUID id, String previousStatus, String newStatus, String notes, Instant occurredAt) {}

  public record Change(
      @NotBlank
          @Pattern(
              regexp =
                  "interest|sent|screening|interview|technical-test|offer|hired|rejected|withdrawn")
          String status,
      @Size(max = 12000) String notes) {}

  public void record(UUID id, String previous, String next, String notes) {
    db.update(
        "INSERT INTO application_events(id,application_id,previous_status,new_status,notes,occurred_at) VALUES(?,?,?,?,?,now())",
        UUID.randomUUID(),
        id,
        previous,
        next,
        notes);
  }

  public List<Event> list(UUID id) {
    if (db.queryForObject("SELECT count(*) FROM job_applications WHERE id=?", Long.class, id) == 0)
      throw ApiException.notFound();
    return db.query(
        "SELECT * FROM application_events WHERE application_id=? ORDER BY occurred_at,id",
        (r, n) ->
            new Event(
                r.getObject("id", UUID.class),
                r.getString("previous_status"),
                r.getString("new_status"),
                r.getString("notes"),
                r.getTimestamp("occurred_at").toInstant()),
        id);
  }

  public void change(UUID id, Change change) {
    var rows = db.queryForList("SELECT status FROM job_applications WHERE id=? FOR UPDATE", id);
    if (rows.isEmpty()) throw ApiException.notFound();
    String previous = (String) rows.getFirst().get("status");
    if (!Objects.equals(previous, change.status())) {
      db.update(
          "UPDATE job_applications SET status=?,updated_at=now() WHERE id=?", change.status(), id);
      record(id, previous, change.status(), change.notes());
    }
  }
}
