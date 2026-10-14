package com.safe.system.security;

import com.safe.system.common.ApiException;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthenticationManager manager;
  private final SecurityContextRepository contexts;
  private final CsrfTokenRepository csrf;
  private final LoginLimiter limiter;

  public AuthController(
      AuthenticationManager manager,
      SecurityContextRepository contexts,
      CsrfTokenRepository csrf,
      LoginLimiter limiter) {
    this.manager = manager;
    this.contexts = contexts;
    this.csrf = csrf;
    this.limiter = limiter;
  }

  public record Login(
      @NotBlank @Size(max = 100) String username, @NotBlank @Size(max = 72) String password) {}

  @GetMapping("/csrf")
  Map<String, String> csrf(CsrfToken token) {
    return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
  }

  @PostMapping("/login")
  Map<String, String> login(
      @Valid @RequestBody Login login, HttpServletRequest req, HttpServletResponse res) {
    limiter.check(req.getRemoteAddr());
    try {
      if (login.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
        throw new BadCredentialsException("Invalid credentials");
      var authentication =
          manager.authenticate(
              UsernamePasswordAuthenticationToken.unauthenticated(
                  login.username(), login.password()));
      req.getSession(true);
      req.changeSessionId();
      var context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(authentication);
      SecurityContextHolder.setContext(context);
      contexts.saveContext(context, req, res);
      csrf.saveToken(null, req, res);
      return Map.of("username", authentication.getName());
    } catch (AuthenticationException e) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
    }
  }

  @GetMapping("/me")
  Map<String, String> me(Authentication auth) {
    return Map.of("username", auth.getName());
  }

  @PostMapping("/logout")
  void logout(HttpServletRequest req, HttpServletResponse res) {
    var session = req.getSession(false);
    if (session != null) session.invalidate();
    SecurityContextHolder.clearContext();
    csrf.saveToken(null, req, res);
  }
}
