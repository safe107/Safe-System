package com.safe.system.file;

import com.safe.system.common.ApiException;
import java.io.IOException;
import java.nio.file.*;
import org.springframework.http.HttpStatus;

public class LocalStorage implements Storage {
  private final Path root;

  public LocalStorage(String root) {
    this.root = Path.of(root).toAbsolutePath().normalize();
  }

  private Path path(String key) {
    Path p = root.resolve(key).normalize();
    if (!p.startsWith(root) || key.contains("/") || key.contains("\\"))
      throw ApiException.badRequest("Chave inválida");
    return p;
  }

  public void put(String key, byte[] bytes, String contentType) {
    try {
      Files.createDirectories(root);
      Files.write(path(key), bytes, StandardOpenOption.CREATE_NEW);
    } catch (IOException e) {
      throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Armazenamento indisponível");
    }
  }

  public byte[] read(String key) {
    try {
      return Files.readAllBytes(path(key));
    } catch (IOException e) {
      throw ApiException.notFound();
    }
  }

  public void delete(String key) {
    try {
      Files.deleteIfExists(path(key));
    } catch (IOException e) {
      throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Não foi possível excluir arquivo");
    }
  }
}
