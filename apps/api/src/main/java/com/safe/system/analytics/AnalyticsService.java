package com.safe.system.analytics;

import com.safe.system.common.ApiException;
import com.safe.system.settings.SiteSettings;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {
  private final JdbcTemplate db;
  private final SiteSettings settings;
  private final Map<String, Instant> throttle = new HashMap<>();

  public AnalyticsService(JdbcTemplate db, SiteSettings settings) {
    this.db = db;
    this.settings = settings;
  }

  public record Event(
      @NotBlank @Size(max = 255) String path, @NotNull @Pattern(regexp = "view") String event) {}

  public synchronized void event(Event event, String clientAddress) {
    if (!Boolean.parseBoolean(settings.get().get("analyticsEnabled"))) return;
    throttle.entrySet().removeIf(e -> e.getValue().isBefore(Instant.now().minusSeconds(30)));
    String address = clientAddress + "|" + event.path() + "|" + event.event();
    if (throttle.containsKey(address) || throttle.size() > 10000) return;
    throttle.put(address, Instant.now());
    String path = event.path().replaceFirst("^/(pt-BR|en)", "");
    if (path.isEmpty()) path = "/";
    if (!Set.of("/", "/projects", "/certificates", "/experience", "/skills", "/resume", "/contact")
        .contains(path)) {
      boolean project = path.startsWith("/projects/");
      boolean cert = path.startsWith("/certificates/");
      if (!project && !cert) throw ApiException.badRequest("Página inválida");
      String slug = path.substring(path.lastIndexOf('/') + 1);
      if (db.queryForObject(
              "SELECT count(*) FROM "
                  + (project ? "projects" : "certificates")
                  + " WHERE published AND slug=?",
              Long.class,
              slug)
          == 0) throw ApiException.notFound();
    }
    db.update(
        "INSERT INTO analytics_daily(day,path,event,count) VALUES(CURRENT_DATE,?,?,1) ON CONFLICT(day,path,event) DO UPDATE SET count=analytics_daily.count+1",
        path,
        event.event());
  }

  public record Daily(java.time.LocalDate day, String path, String event, long count) {}

  public record Summary(
      boolean configured, List<Daily> rows, long totalViews, long totalDownloads) {}

  public Summary summary() {
    boolean enabled = Boolean.parseBoolean(settings.get().get("analyticsEnabled"));
    return new Summary(
        enabled,
        db.query(
            "SELECT day,path,event,count FROM analytics_daily WHERE day>=CURRENT_DATE-90 ORDER BY day DESC,path",
            (r, n) ->
                new Daily(
                    r.getObject("day", java.time.LocalDate.class),
                    r.getString("path"),
                    r.getString("event"),
                    r.getLong("count"))),
        db.queryForObject(
            "SELECT coalesce(sum(count),0) FROM analytics_daily WHERE event='view'", Long.class),
        db.queryForObject(
            "SELECT coalesce(sum(count),0) FROM analytics_daily WHERE event='download'",
            Long.class));
  }
}
