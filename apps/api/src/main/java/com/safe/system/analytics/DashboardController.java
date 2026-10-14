package com.safe.system.analytics;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class DashboardController {
  private final DashboardService service;

  public DashboardController(DashboardService service) {
    this.service = service;
  }

  @GetMapping
  public DashboardService.Dashboard dashboard() {
    return service.dashboard();
  }
}
