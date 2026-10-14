package com.safe.system.settings;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
public class SettingsController {
  private final SiteSettings settings;

  public SettingsController(SiteSettings settings) {
    this.settings = settings;
  }

  @GetMapping({"/api/v1/public/profile", "/api/v1/public/settings", "/api/v1/admin/settings"})
  public Output get() {
    return output(settings.get());
  }

  @PutMapping("/api/v1/admin/settings")
  public Output save(@Valid @RequestBody SiteSettings.Input input) {
    return output(settings.save(input));
  }

  public record Output(
      String name,
      String titlePt,
      String titleEn,
      String bioPt,
      String bioEn,
      String location,
      String email,
      String githubUrl,
      String linkedinUrl,
      String availabilityPt,
      String availabilityEn,
      String defaultLocale,
      String seoTitle,
      String seoDescription,
      String analyticsEnabled) {}

  private Output output(Map<String, String> values) {
    return new Output(
        values.getOrDefault("name", ""),
        values.getOrDefault("titlePt", ""),
        values.getOrDefault("titleEn", ""),
        values.getOrDefault("bioPt", ""),
        values.getOrDefault("bioEn", ""),
        values.getOrDefault("location", ""),
        values.getOrDefault("email", ""),
        values.getOrDefault("githubUrl", ""),
        values.getOrDefault("linkedinUrl", ""),
        values.getOrDefault("availabilityPt", ""),
        values.getOrDefault("availabilityEn", ""),
        values.getOrDefault("defaultLocale", ""),
        values.getOrDefault("seoTitle", ""),
        values.getOrDefault("seoDescription", ""),
        values.getOrDefault("analyticsEnabled", ""));
  }
}
