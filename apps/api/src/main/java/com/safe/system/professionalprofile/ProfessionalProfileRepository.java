package com.safe.system.professionalprofile;

import java.util.UUID;
import org.springframework.data.jpa.repository.*;

public interface ProfessionalProfileRepository
    extends JpaRepository<ProfessionalProfile, UUID>,
        JpaSpecificationExecutor<ProfessionalProfile> {
  java.util.Optional<ProfessionalProfile> findBySlug(String slug);
}
