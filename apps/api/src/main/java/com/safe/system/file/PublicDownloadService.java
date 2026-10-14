package com.safe.system.file;

import com.safe.system.analytics.AnalyticsRecorder;
import com.safe.system.common.ApiException;
import com.safe.system.resume.ResumeRepository;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PublicDownloadService {
  private final FileService files;
  private final ResumeRepository resumes;
  private final AnalyticsRecorder analytics;

  public PublicDownloadService(
      FileService files, ResumeRepository resumes, AnalyticsRecorder analytics) {
    this.files = files;
    this.resumes = resumes;
    this.analytics = analytics;
  }

  public ResponseEntity<?> file(UUID id, boolean inline) {
    var response = files.download(id, true, inline);
    if (!inline) analytics.download(id);
    return response;
  }

  public ResponseEntity<?> resume(UUID id, boolean inline) {
    var resume =
        resumes
            .findById(id)
            .filter(r -> r.published && r.fileId != null)
            .orElseThrow(ApiException::notFound);
    return file(resume.fileId, inline);
  }
}
