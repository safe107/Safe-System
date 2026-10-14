package com.safe.system.github;

import java.net.*;
import java.net.http.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class GithubService {
  private record Cached(Map<String, Object> data, Instant expires) {}

  private final JdbcTemplate db;
  private final String token;
  private final ObjectMapper json;
  private final HttpClient client;
  private final ConcurrentMap<String, Cached> cache = new ConcurrentHashMap<>();
  private final Set<String> pending = ConcurrentHashMap.newKeySet();
  private final ExecutorService executor = Executors.newFixedThreadPool(2);

  public GithubService(
      JdbcTemplate db, @Value("${safe.github-token}") String token, ObjectMapper json) {
    this.db = db;
    this.token = token;
    this.json = json;
    var builder =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NEVER);
    String proxy = System.getenv("HTTPS_PROXY");
    if (proxy != null && !proxy.isBlank()) {
      var u = URI.create(proxy);
      if (u.getHost() != null && u.getUserInfo() == null)
        builder.proxy(
            ProxySelector.of(
                new InetSocketAddress(u.getHost(), u.getPort() > 0 ? u.getPort() : 80)));
    }
    client = builder.build();
  }

  public Map<String, Object> selected() {
    List<String> urls =
        db
            .queryForList(
                "SELECT DISTINCT github_url FROM projects WHERE published AND github_url IS NOT NULL ORDER BY github_url LIMIT 20",
                String.class)
            .stream()
            .filter(u -> u.matches("https://github\\.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+/?"))
            .toList();
    cache.keySet().retainAll(urls);
    var results = new ArrayList<Map<String, Object>>();
    for (String url : urls) {
      var saved = cache.get(url);
      if ((saved == null || Instant.now().isAfter(saved.expires())) && pending.add(url)) {
        executor.submit(
            () -> {
              try {
                refresh(url);
              } finally {
                pending.remove(url);
              }
            });
      }
      results.add(saved == null ? manual(url) : saved.data());
    }
    return Map.of(
        "repositories", results, "configured", !urls.isEmpty(), "source", "manual-and-cache");
  }

  private Map<String, Object> manual(String url) {
    return Map.of(
        "url", url, "name", URI.create(url).getPath().replaceAll("^/|/$", ""), "available", false);
  }

  private void refresh(String url) {
    var result = new LinkedHashMap<>(manual(url));
    try {
      String path = URI.create(url).getPath().replaceAll("/$", "");
      var builder =
          HttpRequest.newBuilder(URI.create("https://api.github.com/repos" + path))
              .timeout(Duration.ofSeconds(4))
              .header("Accept", "application/vnd.github+json")
              .header("X-GitHub-Api-Version", "2022-11-28");
      if (!token.isBlank()) builder.header("Authorization", "Bearer " + token);
      var response = client.send(builder.GET().build(), HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        var node = json.readTree(response.body());
        if (!node.path("private").asBoolean(true)) {
          result.put("available", true);
          result.put("description", node.path("description").asText(""));
          result.put("language", node.path("language").asText(""));
        }
      }
    } catch (Exception e) {
      if (e instanceof InterruptedException) Thread.currentThread().interrupt();
    }
    cache.put(url, new Cached(Map.copyOf(result), Instant.now().plusSeconds(900)));
  }

  @jakarta.annotation.PreDestroy
  void stop() {
    executor.shutdownNow();
  }
}
