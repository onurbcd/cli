package com.onurbcd.cli;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = "com.onurbcd.cli")
@EnableJpaRepositories(basePackages = "com.onurbcd.cli.persistency.repository")
@EntityScan(basePackages = "com.onurbcd.cli.persistency.entity")
public class CliApp {

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        SpringApplication.run(CliApp.class, args);
    }
}
