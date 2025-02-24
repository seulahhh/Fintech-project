package com.project.fintech;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class FintechApplication {
  public static void main(String[] args) {
    SpringApplication.run(FintechApplication.class, args);
  }
}
