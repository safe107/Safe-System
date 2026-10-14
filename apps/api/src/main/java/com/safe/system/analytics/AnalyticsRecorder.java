package com.safe.system.analytics;

import com.safe.system.settings.SiteSettings;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsRecorder {
  private final JdbcTemplate db;
  private final SiteSettings settings;

  public AnalyticsRecorder(JdbcTemplate db, SiteSettings settings) {
    this.db = db;
    this.settings = settings;
  }

  public void download(UUID file) {
    if (!Boolean.parseBoolean(settings.get().get("analyticsEnabled"))) return;
    var paths =
        db.queryForList(
            "SELECT '/resume' AS path FROM resumes WHERE published AND file_id=? UNION ALL SELECT '/certificates/'||slug AS path FROM certificates WHERE published AND file_id=? LIMIT 1",
            String.class,
            file,
            file);
    if (paths.isEmpty()) return;
    db.update(
        "INSERT INTO analytics_daily(day,path,event,count) VALUES(CURRENT_DATE,?,'download',1) ON CONFLICT(day,path,event) DO UPDATE SET count=analytics_daily.count+1",
        paths.getFirst());
  }
}
