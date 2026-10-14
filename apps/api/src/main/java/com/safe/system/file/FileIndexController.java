package com.safe.system.file;

import com.safe.system.common.PageResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/files")
public class FileIndexController {
  private final FileIndexService service;

  public FileIndexController(FileIndexService service) {
    this.service = service;
  }

  @GetMapping
  public PageResponse<FileIndexService.Entry> list(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return service.list(page, size);
  }
}
