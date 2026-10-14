package com.safe.system.security;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<AdminUser, UUID> {
  Optional<AdminUser> findByUsername(String username);
}
