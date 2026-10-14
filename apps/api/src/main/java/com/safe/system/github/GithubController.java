package com.safe.system.github;

import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/github/repositories")
public class GithubController {
  private final GithubService github;

  public GithubController(GithubService github) {
    this.github = github;
  }

  @GetMapping
  public Map<String, Object> list() {
    return github.selected();
  }
}
