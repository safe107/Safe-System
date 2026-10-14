package com.safe.system.jobapplication;

import java.util.UUID;
import org.springframework.data.jpa.repository.*;

public interface JobApplicationRepository
    extends JpaRepository<JobApplication, UUID>, JpaSpecificationExecutor<JobApplication> {}
