package com.safe.system.common;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class Errors {
  public record Error(String code, String message, Map<String, String> fields) {}

  @ExceptionHandler(ApiException.class)
  ResponseEntity<Error> application(ApiException e) {
    return ResponseEntity.status(e.status)
        .body(new Error(e.status.name(), e.getMessage(), Map.of()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<Error> validation(MethodArgumentNotValidException e) {
    return ResponseEntity.badRequest()
        .body(
            new Error(
                "VALIDATION",
                "Revise os campos",
                e.getBindingResult().getFieldErrors().stream()
                    .collect(
                        Collectors.toMap(
                            x -> x.getField(), x -> x.getDefaultMessage(), (a, b) -> a))));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<Error> conflict() {
    return ResponseEntity.status(409)
        .body(
            new Error(
                "CONFLICT", "Conteúdo duplicado ou ainda vinculado a outro recurso", Map.of()));
  }

  @ExceptionHandler({
    MethodArgumentTypeMismatchException.class,
    org.springframework.http.converter.HttpMessageNotReadableException.class,
    jakarta.validation.ConstraintViolationException.class
  })
  ResponseEntity<Error> invalid() {
    return ResponseEntity.badRequest()
        .body(new Error("INVALID_REQUEST", "Parâmetros ou formato inválidos", Map.of()));
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  ResponseEntity<Error> size() {
    return ResponseEntity.status(413)
        .body(new Error("FILE_TOO_LARGE", "Limite de arquivo: 10 MB", Map.of()));
  }

  @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
  ResponseEntity<Error> missing() {
    return ResponseEntity.status(404)
        .body(new Error("NOT_FOUND", "Recurso não encontrado", Map.of()));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<Error> unexpected(Exception e) {
    org.slf4j.LoggerFactory.getLogger(Errors.class)
        .error("Unhandled request failure: {}", e.getClass().getSimpleName());
    return ResponseEntity.internalServerError()
        .body(new Error("INTERNAL", "Não foi possível concluir a operação", Map.of()));
  }
}
