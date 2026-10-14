package com.safe.system.experience;

import java.util.UUID;
import org.springframework.data.jpa.repository.*;

public interface ExperienceRepository
    extends JpaRepository<Experience, UUID>, JpaSpecificationExecutor<Experience> {}
