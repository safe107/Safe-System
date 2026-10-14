package com.safe.system.course;

import java.util.UUID;
import org.springframework.data.jpa.repository.*;

public interface CourseRepository
    extends JpaRepository<Course, UUID>, JpaSpecificationExecutor<Course> {}
