package com.safe.system.analytics;

import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
  private final JdbcTemplate db;

  public DashboardService(JdbcTemplate db) {
    this.db = db;
  }

  public record Draft(UUID id, String slug, String title) {}

  public record Course(UUID id, String name, String status, Integer progress) {}

  public record NextAction(
      UUID id, String company, String position, String nextAction, LocalDate followUpAt) {}

  public record Activity(String type, String label, Instant updatedAt) {}

  public record StatusCount(String status, long count) {}

  public record Dashboard(
      long projects,
      long publishedProjects,
      long certificates,
      long publishedCertificates,
      long activeCourses,
      long activeApplications,
      List<Draft> draftProjects,
      List<Course> pendingCourses,
      List<NextAction> nextActions,
      List<Activity> recentActivity,
      List<StatusCount> applicationStats,
      boolean analyticsConfigured,
      Long totalViews) {}

  public Dashboard dashboard() {
    var drafts =
        db.query(
            "SELECT p.id,p.slug,t.title FROM projects p LEFT JOIN project_translations t ON t.project_id=p.id AND t.locale='pt-BR' WHERE NOT published ORDER BY p.updated_at DESC LIMIT 5",
            (r, n) ->
                new Draft(
                    r.getObject("id", UUID.class), r.getString("slug"), r.getString("title")));
    var courses =
        db.query(
            "SELECT id,name,status,progress FROM courses WHERE status IN ('planned','in-progress','paused') ORDER BY updated_at DESC LIMIT 5",
            (r, n) ->
                new Course(
                    r.getObject("id", UUID.class),
                    r.getString("name"),
                    r.getString("status"),
                    r.getObject("progress", Integer.class)));
    var actions =
        db.query(
            "SELECT id,company,position,next_action,follow_up_at FROM job_applications WHERE follow_up_at IS NOT NULL AND status NOT IN ('hired','rejected','withdrawn') ORDER BY follow_up_at LIMIT 10",
            (r, n) ->
                new NextAction(
                    r.getObject("id", UUID.class),
                    r.getString("company"),
                    r.getString("position"),
                    r.getString("next_action"),
                    r.getObject("follow_up_at", LocalDate.class)));
    var activity =
        db.query(
            "SELECT 'project' AS type,slug AS label,updated_at FROM projects UNION ALL SELECT 'course',name,updated_at FROM courses UNION ALL SELECT 'application',company,updated_at FROM job_applications ORDER BY updated_at DESC LIMIT 10",
            (r, n) ->
                new Activity(
                    r.getString("type"),
                    r.getString("label"),
                    r.getTimestamp("updated_at").toInstant()));
    var stats =
        db.query(
            "SELECT status,count(*) AS count FROM job_applications GROUP BY status ORDER BY status",
            (r, n) -> new StatusCount(r.getString("status"), r.getLong("count")));
    boolean configured =
        Boolean.TRUE.equals(
            db.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM site_settings WHERE key='analyticsEnabled' AND value='true')",
                Boolean.class));
    return new Dashboard(
        count("SELECT count(*) FROM projects"),
        count("SELECT count(*) FROM projects WHERE published"),
        count("SELECT count(*) FROM certificates"),
        count("SELECT count(*) FROM certificates WHERE published"),
        count("SELECT count(*) FROM courses WHERE status='in-progress'"),
        count(
            "SELECT count(*) FROM job_applications WHERE status NOT IN ('hired','rejected','withdrawn')"),
        drafts,
        courses,
        actions,
        activity,
        stats,
        configured,
        configured
            ? count("SELECT coalesce(sum(count),0) FROM analytics_daily WHERE event='view'")
            : null);
  }

  private long count(String sql) {
    return db.queryForObject(sql, Long.class);
  }
}
