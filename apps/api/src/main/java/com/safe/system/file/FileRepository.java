package com.safe.system.file;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<StoredFile, UUID> {}
