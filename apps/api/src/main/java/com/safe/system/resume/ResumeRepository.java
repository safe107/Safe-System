package com.safe.system.resume;

import java.util.UUID;
import org.springframework.data.jpa.repository.*;

public interface ResumeRepository
    extends JpaRepository<Resume, UUID>, JpaSpecificationExecutor<Resume> {}
