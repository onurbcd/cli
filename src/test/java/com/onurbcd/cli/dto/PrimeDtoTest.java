package com.onurbcd.cli.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PrimeDtoTest {

    @Test
    @DisplayName("setters store the fields and inherit the audit dates")
    void givenValues_whenSet_shouldReturnThemFromGetters() {
        var id = UUID.fromString("22222222-2222-2222-2222-222222222222");
        var created = LocalDateTime.of(2024, 1, 2, 3, 4, 5);
        var dto = new PrimeDto();

        dto.setId(id);
        dto.setName("Prime");
        dto.setActive(true);
        dto.setCreatedDate(created);

        assertThat(dto).isInstanceOf(AuditDto.class).isInstanceOf(Dtoable.class);
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getName()).isEqualTo("Prime");
        assertThat(dto.getActive()).isTrue();
        assertThat(dto.getCreatedDate()).isEqualTo(created);
        assertThat(dto.getLastModifiedDate()).isNull();
    }

    @Nested
    @DisplayName("isActive")
    class IsActive {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @NullSource
        @DisplayName("returns the active flag as is")
        void givenActive_whenIsActive_shouldReturnSameValue(Boolean active) {
            var dto = new PrimeDto();
            dto.setActive(active);

            assertThat(dto.isActive()).isEqualTo(active);
        }
    }
}
