package com.safe.system.file;

import com.safe.system.common.ApiException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class FileService {
  private final FileRepository repository;
  private final Storage storage;
  private final JdbcTemplate db;

  public FileService(FileRepository repository, Storage storage, JdbcTemplate db) {
    this.repository = repository;
    this.storage = storage;
    this.db = db;
  }

  public record Metadata(
      UUID id,
      String originalName,
      String mimeType,
      long sizeBytes,
      String checksum,
      Instant createdAt) {}

  public Metadata metadata(UUID id) {
    return dto(find(id));
  }

  public void requirePdf(UUID id) {
    if (!find(id).mimeType.equals("application/pdf"))
      throw ApiException.badRequest("Arquivo deve ser PDF");
  }

  public void requireImage(UUID id) {
    if (!find(id).mimeType.startsWith("image/"))
      throw ApiException.badRequest("Arquivo deve ser imagem");
  }

  private StoredFile find(UUID id) {
    return repository.findById(id).orElseThrow(ApiException::notFound);
  }

  private Metadata dto(StoredFile f) {
    return new Metadata(f.id, f.originalName, f.mimeType, f.sizeBytes, f.checksum, f.createdAt);
  }

  public Metadata upload(MultipartFile input) {
    if (input.isEmpty() || input.getSize() > 10485760)
      throw ApiException.badRequest("Arquivo deve ter entre 1 byte e 10 MB");
    String name = Optional.ofNullable(input.getOriginalFilename()).orElse("file");
    if (name.contains("/")
        || name.contains("\\")
        || name.contains("\r")
        || name.contains("\n")
        || name.length() > 200) throw ApiException.badRequest("Nome de arquivo inválido");
    try {
      byte[] bytes = input.getBytes();
      String lower = name.toLowerCase(Locale.ROOT);
      String type;
      if (lower.endsWith(".pdf") && starts(bytes, new byte[] {37, 80, 68, 70, 45})) {
        String document = new String(bytes, StandardCharsets.ISO_8859_1);
        if (!document.stripTrailing().endsWith("%%EOF")
            || document.matches("(?s).*/(?:JavaScript|JS|Launch|EmbeddedFile)\\b.*"))
          throw ApiException.badRequest("PDF inválido ou conteúdo ativo não permitido");
        FileValidation.pdf(bytes);
        type = "application/pdf";
      } else if (lower.endsWith(".png")
          && starts(bytes, new byte[] {(byte) 137, 80, 78, 71, 13, 10, 26, 10})) type = "image/png";
      else if ((lower.endsWith(".jpg") || lower.endsWith(".jpeg"))
          && starts(bytes, new byte[] {(byte) 255, (byte) 216, (byte) 255})) type = "image/jpeg";
      else if (lower.endsWith(".webp")
          && bytes.length > 12
          && new String(bytes, 0, 4, StandardCharsets.US_ASCII).equals("RIFF")
          && new String(bytes, 8, 4, StandardCharsets.US_ASCII).equals("WEBP")) type = "image/webp";
      else throw ApiException.badRequest("Formato/assinatura não permitido: PDF, JPG, PNG ou WebP");
      if (input.getContentType() == null || !input.getContentType().equals(type))
        throw ApiException.badRequest("MIME declarado não corresponde ao formato");
      if (type.startsWith("image/")) FileValidation.image(bytes);
      var file = new StoredFile();
      file.id = UUID.randomUUID();
      file.storageKey = file.id.toString();
      file.originalName = name;
      file.mimeType = type;
      file.sizeBytes = bytes.length;
      file.checksum = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
      file.createdAt = Instant.now();
      storage.put(file.storageKey, bytes, type);
      try {
        repository.saveAndFlush(file);
      } catch (RuntimeException e) {
        storage.delete(file.storageKey);
        throw e;
      }
      return dto(file);
    } catch (java.io.IOException | java.security.NoSuchAlgorithmException e) {
      throw ApiException.badRequest("Não foi possível ler o arquivo");
    }
  }

  private boolean starts(byte[] bytes, byte[] prefix) {
    return bytes.length >= prefix.length
        && Arrays.equals(Arrays.copyOf(bytes, prefix.length), prefix);
  }

  public boolean publiclyLinked(UUID id) {
    return db.queryForObject(
            "SELECT (SELECT count(*) FROM certificates WHERE published AND file_id=?)+(SELECT count(*) FROM resumes WHERE published AND file_id=?)+(SELECT count(*) FROM project_media m JOIN projects p ON p.id=m.project_id WHERE p.published AND m.file_id=?)",
            Long.class,
            id,
            id,
            id)
        > 0;
  }

  public ResponseEntity<?> download(UUID id, boolean publicOnly, boolean inline) {
    if (publicOnly && !publiclyLinked(id)) throw ApiException.notFound();
    var f = find(id);
    String signed = storage.signedDownload(f.storageKey, f.originalName, f.mimeType, inline);
    if (signed != null)
      return ResponseEntity.status(302)
          .header("Location", signed)
          .header("Cache-Control", "no-store")
          .build();
    return ResponseEntity.ok()
        .header("Content-Type", f.mimeType)
        .header(
            "Content-Disposition",
            ContentDisposition.builder(inline ? "inline" : "attachment")
                .filename(f.originalName, StandardCharsets.UTF_8)
                .build()
                .toString())
        .header("Cache-Control", "no-store")
        .header("X-Content-Type-Options", "nosniff")
        .header("Content-Security-Policy", "sandbox; default-src 'none'; frame-ancestors 'self'")
        .body(storage.read(f.storageKey));
  }

  public void delete(UUID id) {
    var f = find(id);
    long refs =
        db.queryForObject(
            "SELECT (SELECT count(*) FROM certificates WHERE file_id=?)+(SELECT count(*) FROM resumes WHERE file_id=?)+(SELECT count(*) FROM project_media WHERE file_id=?)",
            Long.class,
            id,
            id,
            id);
    if (refs > 0)
      throw new ApiException(HttpStatus.CONFLICT, "Remova os vínculos antes de excluir o arquivo");
    repository.delete(f);
    repository.flush();
    db.update("INSERT INTO file_deletion_jobs(storage_key) VALUES(?)", f.storageKey);
  }
}
