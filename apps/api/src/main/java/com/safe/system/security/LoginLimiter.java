package com.safe.system.security;

import com.safe.system.common.ApiException;
import java.time.Instant;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class LoginLimiter {
  private final Map<String, Deque<Instant>> attempts = new HashMap<>();

  public synchronized void check(String address) {
    Instant cutoff = Instant.now().minusSeconds(900);
    attempts
        .values()
        .forEach(
            q -> {
              while (!q.isEmpty() && q.peekFirst().isBefore(cutoff)) q.removeFirst();
            });
    attempts.entrySet().removeIf(e -> e.getValue().isEmpty());
    if (attempts.size() > 10000)
      throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Tente novamente mais tarde");
    Deque<Instant> q = attempts.computeIfAbsent(address, k -> new ArrayDeque<>());
    if (q.size() >= 10)
      throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Tente novamente em 15 minutos");
    q.add(Instant.now());
  }
}
