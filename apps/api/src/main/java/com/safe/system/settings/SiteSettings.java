package com.safe.system.settings;

import com.safe.system.common.*;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SiteSettings {
  private final JdbcTemplate db;

  public SiteSettings(JdbcTemplate db) {
    this.db = db;
  }

  public record Input(
      @NotBlank @Size(max = 100) String name,
      @Size(max = 200) String titlePt,
      @Size(max = 200) String titleEn,
      @Size(max = 3000) String bioPt,
      @Size(max = 3000) String bioEn,
      @Size(max = 200) String location,
      @Email @Size(max = 200) String email,
      @Size(max = 1000) String githubUrl,
      @Size(max = 1000) String linkedinUrl,
      @Size(max = 200) String availabilityPt,
      @Size(max = 200) String availabilityEn,
      @Pattern(regexp = "pt-BR|en") String defaultLocale,
      @Size(max = 200) String seoTitle,
      @Size(max = 500) String seoDescription,
      boolean analyticsEnabled) {}

  private static final Set<String> ALLOWED =
      Set.of(
          "name",
          "titlePt",
          "titleEn",
          "bioPt",
          "bioEn",
          "location",
          "email",
          "githubUrl",
          "linkedinUrl",
          "availabilityPt",
          "availabilityEn",
          "defaultLocale",
          "seoTitle",
          "seoDescription",
          "analyticsEnabled");

  public Map<String, String> get() {
    var m = new LinkedHashMap<String, String>();
    m.put("name", "José Miguel");
    m.put("titlePt", "Estudante de Sistemas de Informação");
    m.put("titleEn", "Information Systems student");
    m.put(
        "bioPt",
        "Estudante na UNIFESSPA. Interesse em suporte e infraestrutura de TI, desenvolvimento back-end, redes e segurança da informação.");
    m.put(
        "bioEn",
        "UNIFESSPA student interested in IT support and infrastructure, back-end development, networks and information security.");
    m.put("defaultLocale", "pt-BR");
    m.put("analyticsEnabled", "false");
    db.query(
        "SELECT key,value FROM site_settings",
        (org.springframework.jdbc.core.RowCallbackHandler)
            r -> {
              if (ALLOWED.contains(r.getString(1))) m.put(r.getString(1), r.getString(2));
            });
    return m;
  }

  public Map<String, String> save(Input input) {
    Rules.url(input.githubUrl());
    Rules.url(input.linkedinUrl());
    var m = new LinkedHashMap<String, String>();
    m.put("name", input.name());
    m.put("titlePt", input.titlePt());
    m.put("titleEn", input.titleEn());
    m.put("bioPt", input.bioPt());
    m.put("bioEn", input.bioEn());
    m.put("location", input.location());
    m.put("email", input.email());
    m.put("githubUrl", input.githubUrl());
    m.put("linkedinUrl", input.linkedinUrl());
    m.put("availabilityPt", input.availabilityPt());
    m.put("availabilityEn", input.availabilityEn());
    m.put("defaultLocale", input.defaultLocale());
    m.put("seoTitle", input.seoTitle());
    m.put("seoDescription", input.seoDescription());
    m.put("analyticsEnabled", Boolean.toString(input.analyticsEnabled()));
    m.forEach(
        (k, v) ->
            db.update(
                "INSERT INTO site_settings(key,value,updated_at) VALUES(?,?,now()) ON CONFLICT(key) DO UPDATE SET value=excluded.value,updated_at=now()",
                k,
                Objects.toString(v, "")));
    return get();
  }
}
