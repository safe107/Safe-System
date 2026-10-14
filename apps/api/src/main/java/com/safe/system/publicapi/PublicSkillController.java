package com.safe.system.publicapi;

import com.safe.system.common.*;
import com.safe.system.skill.*;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/skills")
public class PublicSkillController {
  private final SkillService service;

  public PublicSkillController(SkillService service) {
    this.service = service;
  }

  @GetMapping
  public PageResponse<SkillDto.Output> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "relevance") String sort,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) UUID skill,
      @RequestParam(required = false) UUID professionalProfile,
      @RequestParam(required = false) String issuer,
      @RequestParam(required = false) String locale) {
    return service.list(
        true,
        page,
        size,
        sort,
        search,
        category,
        status,
        skill,
        professionalProfile,
        issuer,
        locale);
  }
}
