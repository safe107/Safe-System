package com.safe.system.file;

public interface Storage {
  void put(String key, byte[] bytes, String contentType);

  byte[] read(String key);

  void delete(String key);

  default String signedDownload(String key, String name, String contentType, boolean inline) {
    return null;
  }
}
