package com.safe.system.jobapplication;

import jakarta.validation.Valid;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/applications/{id}/events")
public class HistoryController {
  private final ApplicationHistory history;

  public HistoryController(ApplicationHistory history) {
    this.history = history;
  }

  @GetMapping
  public List<ApplicationHistory.Event> list(@PathVariable UUID id) {
    return history.list(id);
  }

  @PostMapping
  public void change(@PathVariable UUID id, @Valid @RequestBody ApplicationHistory.Change change) {
    history.change(id, change);
  }
}
