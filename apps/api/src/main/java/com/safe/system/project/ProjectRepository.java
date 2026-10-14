package com.safe.system.project;

import java.util.UUID;
import org.springframework.data.jpa.repository.*;

public interface ProjectRepository
    extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {
  java.util.Optional<Project> findBySlug(String slug);
}
