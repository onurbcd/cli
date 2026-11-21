package com.onurbcd.cli.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AuditDtoTest {

    @Test
    @DisplayName("a new instance has no audit dates")
    void givenNewInstance_shouldHaveNullDates() {
        var dto = new AuditDto();

        assertThat(dto.getCreatedDate()).isNull();
        assertThat(dto.getLastModifiedDate()).isNull();
    }

    @Test
    @DisplayName("setters store each date independently")
    void givenDates_whenSet_shouldReturnThemFromGetters() {
        var created = LocalDateTime.of(2024, 1, 2, 3, 4, 5);
        var modified = LocalDateTime.of(2025, 6, 7, 8, 9, 10);
        var dto = new AuditDto();

        dto.setCreatedDate(created);
        dto.setLastModifiedDate(modified);

        assertThat(dto.getCreatedDate()).isEqualTo(created);
        assertThat(dto.getLastModifiedDate()).isEqualTo(modified);
    }
}
