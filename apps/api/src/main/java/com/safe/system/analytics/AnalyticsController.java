package com.safe.system.analytics;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class AnalyticsController {
  private final AnalyticsService service;

  public AnalyticsController(AnalyticsService service) {
    this.service = service;
  }

  @PostMapping("/api/v1/public/analytics/events")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  public void event(@Valid @RequestBody AnalyticsService.Event event, HttpServletRequest request) {
    service.event(event, request.getRemoteAddr());
  }

  @GetMapping("/api/v1/admin/analytics")
  public AnalyticsService.Summary summary() {
    return service.summary();
  }
}
