package com.safe.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@org.springframework.scheduling.annotation.EnableScheduling
@SpringBootApplication
public class SafeApplication {
  public static void main(String[] args) {
    SpringApplication.run(SafeApplication.class, args);
  }
}
