package com.safe.system.common;

import java.net.URI;
import java.time.LocalDate;
import java.util.Locale;

public final class Rules {
  private Rules() {}

  public static void url(String value) {
    if (value == null || value.isBlank()) return;
    try {
      var u = URI.create(value);
      if (!"https".equals(u.getScheme()) || u.getHost() == null || u.getUserInfo() != null)
        throw new IllegalArgumentException();
    } catch (IllegalArgumentException e) {
      throw ApiException.badRequest("URL precisa ser HTTPS válida");
    }
  }

  public static void dates(LocalDate start, LocalDate end) {
    if (start != null && end != null && start.isAfter(end))
      throw ApiException.badRequest("Data de término anterior ao início");
  }

  public static String like(String input) {
    return input
        .toLowerCase(Locale.ROOT)
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_");
  }
}
