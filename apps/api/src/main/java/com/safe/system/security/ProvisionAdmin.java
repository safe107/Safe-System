package com.safe.system.security;

import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class ProvisionAdmin implements ApplicationRunner {
  private final AdminRepository repository;
  private final PasswordEncoder encoder;
  private final String username, password;
  private final boolean production, secure;
  private final String storage, url, dbPassword;

  public ProvisionAdmin(
      AdminRepository repository,
      PasswordEncoder encoder,
      @Value("${safe.admin-username}") String username,
      @Value("${safe.admin-password}") String password,
      @Value("${safe.production}") boolean production,
      @Value("${server.servlet.session.cookie.secure}") boolean secure,
      @Value("${safe.storage-mode}") String storage,
      @Value("${safe.frontend-url}") String url,
      @Value("${spring.datasource.password}") String dbPassword) {
    this.repository = repository;
    this.encoder = encoder;
    this.username = username;
    this.password = password;
    this.production = production;
    this.secure = secure;
    this.storage = storage;
    this.url = url;
    this.dbPassword = dbPassword;
  }

  public void run(ApplicationArguments args) {
    if (production
        && (!secure
            || !url.startsWith("https://")
            || !storage.equals("s3")
            || dbPassword.equals("safe-local-only")))
      throw new IllegalStateException(
          "Production requires HTTPS, secure cookie, S3 and non-default database credentials");
    if (repository.count() == 0 && !username.isBlank() && !password.isBlank()) {
      if (password.length() < 12
          || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
        throw new IllegalStateException(
            "Admin password must have >=12 characters and <=72 UTF-8 bytes");
      var u = new AdminUser();
      u.id = UUID.randomUUID();
      u.username = username;
      u.passwordHash = encoder.encode(password);
      u.createdAt = Instant.now();
      u.updatedAt = u.createdAt;
      repository.save(u);
    }
    if (production && repository.count() == 0)
      throw new IllegalStateException(
          "Provision administrator explicitly before production startup");
  }
}
