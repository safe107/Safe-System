package com.safe.system.file;

import java.util.UUID;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/files")
public class FileController {
  private final FileService files;

  public FileController(FileService files) {
    this.files = files;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public FileService.Metadata upload(@RequestPart("file") MultipartFile file) {
    return files.upload(file);
  }

  @GetMapping("/{id}")
  public FileService.Metadata metadata(@PathVariable UUID id) {
    return files.metadata(id);
  }

  @GetMapping("/{id}/download")
  public ResponseEntity<?> download(
      @PathVariable UUID id, @RequestParam(defaultValue = "false") boolean inline) {
    return files.download(id, false, inline);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    files.delete(id);
  }
}
