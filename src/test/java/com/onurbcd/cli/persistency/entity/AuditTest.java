package com.onurbcd.cli.persistency.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AuditTest {

    private static final LocalDateTime CREATED = LocalDateTime.of(2024, 1, 2, 3, 4, 5);
    private static final LocalDateTime MODIFIED = LocalDateTime.of(2025, 6, 7, 8, 9, 10);

    @Test
    @DisplayName("a new instance has no audit dates")
    void givenNewInstance_shouldHaveNullDates() {
        var audit = new Audit() {
        };

        assertThat(audit.getCreatedDate()).isNull();
        assertThat(audit.getLastModifiedDate()).isNull();
    }

    @Test
    @DisplayName("the all-args constructor stores both dates in order")
    void givenDates_whenAllArgsConstructor_shouldStoreThem() {
        var audit = new Audit(CREATED, MODIFIED) {
        };

        assertThat(audit.getCreatedDate()).isEqualTo(CREATED);
        assertThat(audit.getLastModifiedDate()).isEqualTo(MODIFIED);
    }

    @Test
    @DisplayName("setters store each date independently")
    void givenDates_whenSet_shouldReturnThemFromGetters() {
        var audit = new Audit() {
        };

        audit.setCreatedDate(CREATED);
        audit.setLastModifiedDate(MODIFIED);

        assertThat(audit.getCreatedDate()).isEqualTo(CREATED);
        assertThat(audit.getLastModifiedDate()).isEqualTo(MODIFIED);
    }

    @Test
    @DisplayName("the builder stores both dates")
    void givenBuilder_whenBuild_shouldStoreDates() {
        var secret = Secret.builder().createdDate(CREATED).lastModifiedDate(MODIFIED).build();

        assertThat(secret.getCreatedDate()).isEqualTo(CREATED);
        assertThat(secret.getLastModifiedDate()).isEqualTo(MODIFIED);
    }
}
