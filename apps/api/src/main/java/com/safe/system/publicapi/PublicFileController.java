package com.safe.system.publicapi;

import com.safe.system.file.PublicDownloadService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public")
public class PublicFileController {
  private final PublicDownloadService service;

  public PublicFileController(PublicDownloadService service) {
    this.service = service;
  }

  @GetMapping("/files/{id}/download")
  public ResponseEntity<?> download(
      @PathVariable UUID id, @RequestParam(defaultValue = "false") boolean inline) {
    return service.file(id, inline);
  }

  @GetMapping("/resumes/{id}/download")
  public ResponseEntity<?> resume(
      @PathVariable UUID id, @RequestParam(defaultValue = "false") boolean inline) {
    return service.resume(id, inline);
  }
}
