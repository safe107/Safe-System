package com.safe.system.certificate;

import java.util.UUID;
import org.springframework.data.jpa.repository.*;

public interface CertificateRepository
    extends JpaRepository<Certificate, UUID>, JpaSpecificationExecutor<Certificate> {
  java.util.Optional<Certificate> findBySlug(String slug);
}
