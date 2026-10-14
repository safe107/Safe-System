package com.safe.system.skill;

import java.util.UUID;
import org.springframework.data.jpa.repository.*;

public interface SkillRepository
    extends JpaRepository<Skill, UUID>, JpaSpecificationExecutor<Skill> {
  java.util.Optional<Skill> findBySlug(String slug);
}
