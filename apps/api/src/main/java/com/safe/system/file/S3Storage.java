package com.safe.system.file;

import java.time.Duration;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

public class S3Storage implements Storage {
  private final S3Client client;
  private final S3Presigner signer;
  private final String bucket;

  public S3Storage(S3Client client, S3Presigner signer, String bucket) {
    this.client = client;
    this.signer = signer;
    this.bucket = bucket;
  }

  public void put(String key, byte[] bytes, String type) {
    client.putObject(
        PutObjectRequest.builder().bucket(bucket).key(key).contentType(type).build(),
        RequestBody.fromBytes(bytes));
  }

  public byte[] read(String key) {
    return client
        .getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(key).build())
        .asByteArray();
  }

  public void delete(String key) {
    client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
  }

  public String signedDownload(String key, String name, String type, boolean inline) {
    var req =
        GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .responseContentType(type)
            .responseContentDisposition(
                (inline ? "inline" : "attachment")
                    + "; filename=\"document"
                    + (type.equals("application/pdf") ? ".pdf" : "")
                    + "\"")
            .build();
    return signer
        .presignGetObject(
            GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(2))
                .getObjectRequest(req)
                .build())
        .url()
        .toString();
  }
}
