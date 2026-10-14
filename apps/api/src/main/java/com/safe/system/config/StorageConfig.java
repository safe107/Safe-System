package com.safe.system.config;

import com.safe.system.file.*;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class StorageConfig {
  @Bean
  Storage storage(
      @Value("${safe.storage-mode}") String mode,
      @Value("${safe.storage-root}") String root,
      @Value("${safe.storage-bucket}") String bucket,
      @Value("${safe.storage-endpoint}") String endpoint,
      @Value("${safe.storage-region}") String region,
      @Value("${STORAGE_ACCESS_KEY:}") String access,
      @Value("${STORAGE_SECRET_KEY:}") String secret) {
    if (mode.equals("local")) return new LocalStorage(root);
    if (!mode.equals("s3") || bucket.isBlank() || access.isBlank() || secret.isBlank())
      throw new IllegalStateException("S3 requires bucket and credentials");
    var credentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(access, secret));
    var client =
        S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(credentials)
            .forcePathStyle(true);
    var signer =
        S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider(credentials)
            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
    if (!endpoint.isBlank()) {
      if (!endpoint.startsWith("https://"))
        throw new IllegalStateException("S3 endpoint requires HTTPS");
      client.endpointOverride(URI.create(endpoint));
      signer.endpointOverride(URI.create(endpoint));
    }
    return new S3Storage(client.build(), signer.build(), bucket);
  }
}
