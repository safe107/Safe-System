package com.safe.system.common;

import java.util.List;
import org.springframework.data.domain.*;

public record PageResponse<T>(
    List<T> content, int page, int size, long totalElements, int totalPages) {
  public static <T> PageResponse<T> from(Page<T> p) {
    return new PageResponse<>(
        p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
  }

  public static Pageable request(int page, int size, String sort) {
    return request(page, size, sort, "createdAt");
  }

  public static Pageable request(int page, int size, String sort, String dateField) {
    if (page < 0 || size < 1 || size > 100)
      throw ApiException.badRequest("Página >= 0 e tamanho entre 1 e 100");
    Sort order =
        switch (sort) {
          case "recent" -> Sort.by(Sort.Direction.DESC, dateField);
          case "oldest" -> Sort.by(dateField);
          case "relevance" -> Sort.by("displayOrder").and(Sort.by(Sort.Direction.DESC, dateField));
          default -> throw ApiException.badRequest("Ordenação inválida");
        };
    return PageRequest.of(page, size, order.and(Sort.by("id")));
  }
}
