package com.safe.system;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(
    properties = {
      "spring.datasource.url=${TEST_DATABASE_URL:jdbc:postgresql://localhost:5432/safe_test}",
      "spring.datasource.password=${DATABASE_PASSWORD:safe-local-only}",
      "safe.admin-username=integration-admin",
      "safe.admin-password=isolated-test-password-42",
      "safe.storage-root=./target/test-files"
    })
@AutoConfigureMockMvc
class ApiIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate db;
  @Autowired ObjectMapper json;
  @Autowired com.safe.system.file.StorageCleanup cleanup;

  @BeforeEach
  void clear() {
    db.execute(
        "TRUNCATE projects,certificates,experiences,skills,resumes,courses,job_applications,professional_profiles,files,site_settings,analytics_daily,file_deletion_jobs CASCADE");
  }

  private String project(String slug, boolean published) {
    return """
 {"slug":"%s","category":"backend","status":"in-progress","featured":true,"published":%s,"displayOrder":0,"skillIds":[],"mediaIds":[],"translations":{"pt-BR":{"title":"Projeto verificável","description":"Integração real"},"en":{"title":"Verified project"}}}
 """
        .formatted(slug, published);
  }

  private String createProject(String slug, boolean published) throws Exception {
    var response =
        mvc.perform(
                post("/api/v1/admin/projects")
                    .with(user("admin").roles("ADMIN"))
                    .with(csrf())
                    .contentType("application/json")
                    .content(project(slug, published)))
            .andExpect(status().isCreated())
            .andReturn();
    return json.readTree(response.getResponse().getContentAsString()).path("id").asText();
  }

  private String uploadPdf() throws Exception {
    var file =
        new MockMultipartFile(
            "file",
            "certificate.pdf",
            "application/pdf",
            ApiIntegrationTest.class.getResourceAsStream("/fixtures/document.pdf").readAllBytes());
    var response =
        mvc.perform(
                multipart("/api/v1/admin/files")
                    .file(file)
                    .with(user("admin").roles("ADMIN"))
                    .with(csrf()))
            .andExpect(status().isCreated())
            .andReturn();
    return json.readTree(response.getResponse().getContentAsString()).path("id").asText();
  }

  @Test
  void privateEndpointsRequireServerAuthentication() throws Exception {
    mvc.perform(get("/api/v1/admin/projects")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/v1/admin/applications")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/v1/public/courses")).andExpect(status().isNotFound());
  }

  @Test
  void csrfCannotBeOmitted() throws Exception {
    mvc.perform(
            post("/api/v1/admin/projects")
                .with(user("admin").roles("ADMIN"))
                .contentType("application/json")
                .content(project("csrf", false)))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/api/v1/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"x\",\"password\":\"x\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void loginRotatesSessionAndLogoutInvalidatesIt() throws Exception {
    var r =
        mvc.perform(
                post("/api/v1/auth/login")
                    .with(csrf())
                    .contentType("application/json")
                    .content(
                        "{\"username\":\"integration-admin\",\"password\":\"isolated-test-password-42\"}"))
            .andExpect(status().isOk())
            .andReturn();
    var session = (MockHttpSession) r.getRequest().getSession(false);
    assertNotNull(session);
    mvc.perform(get("/api/v1/auth/me").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("username").value("integration-admin"));
    mvc.perform(post("/api/v1/auth/logout").session(session).with(csrf()))
        .andExpect(status().isOk());
    assertTrue(session.isInvalid());
  }

  @Test
  void invalidCredentialsAreGeneric() throws Exception {
    mvc.perform(
            post("/api/v1/auth/login")
                .with(csrf())
                .contentType("application/json")
                .content("{\"username\":\"missing\",\"password\":\"bad-password\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("message").value("Credenciais inválidas"));
  }

  @Test
  void publicationControlsListAndSlug() throws Exception {
    String id = createProject("private-draft", false);
    mvc.perform(get("/api/v1/public/projects")).andExpect(jsonPath("totalElements").value(0));
    mvc.perform(get("/api/v1/public/projects/private-draft")).andExpect(status().isNotFound());
    mvc.perform(
            patch("/api/v1/admin/projects/" + id + "/publication")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType("application/json")
                .content("{\"published\":true}"))
        .andExpect(status().isOk());
    mvc.perform(get("/api/v1/public/projects/private-draft"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("translations.en.title").value("Verified project"));
    mvc.perform(
            delete("/api/v1/admin/projects/" + id).with(user("admin").roles("ADMIN")).with(csrf()))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/v1/public/projects/private-draft")).andExpect(status().isNotFound());
  }

  @Test
  void searchPaginationAndSortAreValidated() throws Exception {
    createProject("searchable", true);
    mvc.perform(
            get(
                "/api/v1/public/projects?search=VERIFICÁVEL&category=backend&status=in-progress&size=1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("totalElements").value(1));
    mvc.perform(get("/api/v1/public/projects?size=101")).andExpect(status().isBadRequest());
    mvc.perform(get("/api/v1/public/projects?sort=id%20desc%3BDROP"))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/v1/public/projects?search=%25"))
        .andExpect(jsonPath("totalElements").value(0));
  }

  @Test
  void duplicateSlugAndInvalidUrlAreRejected() throws Exception {
    createProject("unique", false);
    mvc.perform(
            post("/api/v1/admin/projects")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType("application/json")
                .content(project("unique", true)))
        .andExpect(status().isConflict());
    mvc.perform(
            post("/api/v1/admin/projects")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType("application/json")
                .content(
                    project("unsafe-url", false)
                        .replace(
                            "\"featured\":true",
                            "\"githubUrl\":\"javascript:alert(1)\",\"featured\":true")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void privateFileCannotBeDownloadedByKnowingItsId() throws Exception {
    String file = uploadPdf();
    mvc.perform(get("/api/v1/public/files/" + file + "/download")).andExpect(status().isNotFound());
    mvc.perform(get("/api/v1/admin/files/" + file + "/download"))
        .andExpect(status().isUnauthorized());
    mvc.perform(get("/api/v1/admin/files/" + file + "/download").with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", "application/pdf"));
  }

  @Test
  void publishedPdfBecomesPrivateAgainWhenHidden() throws Exception {
    String file = uploadPdf();
    String body =
        "{\"slug\":\"certificate\",\"issuer\":\"Test issuer\",\"category\":\"backend\",\"fileId\":\""
            + file
            + "\",\"published\":true,\"displayOrder\":0,\"skillIds\":[],\"translations\":{\"pt-BR\":{\"title\":\"Test certificate\"}}}";
    var response =
        mvc.perform(
                post("/api/v1/admin/certificates")
                    .with(user("admin").roles("ADMIN"))
                    .with(csrf())
                    .contentType("application/json")
                    .content(body))
            .andExpect(status().isCreated())
            .andReturn();
    String id = json.readTree(response.getResponse().getContentAsString()).path("id").asText();
    mvc.perform(get("/api/v1/public/files/" + file + "/download")).andExpect(status().isOk());
    mvc.perform(
            delete("/api/v1/admin/files/" + file).with(user("admin").roles("ADMIN")).with(csrf()))
        .andExpect(status().isConflict());
    mvc.perform(
            patch("/api/v1/admin/certificates/" + id + "/publication")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType("application/json")
                .content("{\"published\":false}"))
        .andExpect(status().isOk());
    mvc.perform(get("/api/v1/public/files/" + file + "/download")).andExpect(status().isNotFound());
  }

  @Test
  void activePdfAndTraversalAreRejected() throws Exception {
    for (String name : new String[] {"../test.pdf", "bad.pdf"}) {
      var f =
          new MockMultipartFile(
              "file",
              name,
              "application/pdf",
              "%PDF-1.4\n/JavaScript evil\n%%EOF".getBytes(StandardCharsets.US_ASCII));
      mvc.perform(
              multipart("/api/v1/admin/files")
                  .file(f)
                  .with(user("admin").roles("ADMIN"))
                  .with(csrf()))
          .andExpect(status().isBadRequest());
    }
  }

  @Test
  void applicationHistoryIsPrivateAndReflectsStageChange() throws Exception {
    String id = UUID.randomUUID().toString();
    db.update(
        "INSERT INTO job_applications(id,company,position,status,display_order,created_at,updated_at) VALUES(?,'Test company','Developer','interest',0,now(),now())",
        UUID.fromString(id));
    mvc.perform(
            post("/api/v1/admin/applications/" + id + "/events")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType("application/json")
                .content("{\"status\":\"interview\",\"notes\":\"Private test note\"}"))
        .andExpect(status().isOk());
    mvc.perform(
            get("/api/v1/admin/applications/" + id + "/events").with(user("admin").roles("ADMIN")))
        .andExpect(jsonPath("$[0].previousStatus").value("interest"))
        .andExpect(jsonPath("$[0].newStatus").value("interview"));
    mvc.perform(get("/api/v1/admin/applications/" + id + "/events"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void analyticsDisabledByDefaultAndContainsNoIp() throws Exception {
    mvc.perform(
            post("/api/v1/public/analytics/events")
                .with(csrf())
                .contentType("application/json")
                .content("{\"path\":\"/pt-BR/projects\",\"event\":\"view\"}"))
        .andExpect(status().isNoContent());
    assertEquals(0L, db.queryForObject("SELECT count(*) FROM analytics_daily", Long.class));
    mvc.perform(get("/api/v1/public/profile")).andExpect(jsonPath("name").value("José Miguel"));
  }

  @Test
  void unlinkedFileDeletionIsDurablyQueued() throws Exception {
    String file = uploadPdf();
    mvc.perform(
            delete("/api/v1/admin/files/" + file).with(user("admin").roles("ADMIN")).with(csrf()))
        .andExpect(status().isNoContent());
    assertEquals(1L, db.queryForObject("SELECT count(*) FROM file_deletion_jobs", Long.class));
    mvc.perform(get("/api/v1/admin/files/" + file).with(user("admin").roles("ADMIN")))
        .andExpect(status().isNotFound());
    cleanup.clean();
    assertEquals(0L, db.queryForObject("SELECT count(*) FROM file_deletion_jobs", Long.class));
  }

  @Test
  void resumeLocaleAndPrimaryVersionAreConsistent() throws Exception {
    String file = uploadPdf();
    String body =
        "{\"title\":\"Test resume\",\"locale\":\"pt-BR\",\"fileId\":\""
            + file
            + "\",\"isPrimary\":true,\"published\":true,\"displayOrder\":0}";
    for (int i = 0; i < 2; i++)
      mvc.perform(
              post("/api/v1/admin/resumes")
                  .with(user("admin").roles("ADMIN"))
                  .with(csrf())
                  .contentType("application/json")
                  .content(body))
          .andExpect(status().isCreated());
    assertEquals(
        1L, db.queryForObject("SELECT count(*) FROM resumes WHERE is_primary", Long.class));
    mvc.perform(get("/api/v1/public/resumes?locale=en"))
        .andExpect(jsonPath("totalElements").value(0));
    mvc.perform(get("/api/v1/public/resumes?locale=pt-BR"))
        .andExpect(jsonPath("totalElements").value(2));
  }

  @Test
  void learningProgressUsesValidatedPrivateCrud() throws Exception {
    String body =
        "{\"name\":\"Integration course\",\"provider\":\"Test provider\",\"status\":\"in-progress\",\"progress\":50,\"displayOrder\":0,\"skillIds\":[]}";
    var response =
        mvc.perform(
                post("/api/v1/admin/courses")
                    .with(user("admin").roles("ADMIN"))
                    .with(csrf())
                    .contentType("application/json")
                    .content(body))
            .andExpect(status().isCreated())
            .andReturn();
    String id = json.readTree(response.getResponse().getContentAsString()).path("id").asText();
    mvc.perform(
            put("/api/v1/admin/courses/" + id)
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType("application/json")
                .content(body.replace("50", "101")))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/v1/admin/courses/" + id)).andExpect(status().isUnauthorized());
    mvc.perform(
            put("/api/v1/admin/courses/" + id)
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType("application/json")
                .content(body.replace("50", "100").replace("in-progress", "completed")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("progress").value(100));
  }

  @Test
  void corsRejectsUnknownOrigin() throws Exception {
    mvc.perform(
            options("/api/v1/admin/projects")
                .header("Origin", "https://unexpected.example")
                .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isForbidden());
    mvc.perform(
            options("/api/v1/admin/projects")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
  }
}
