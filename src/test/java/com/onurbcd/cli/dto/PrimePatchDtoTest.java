package com.onurbcd.cli.dto;

import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PrimePatchDtoTest {

    @SuperBuilder
    private static final class TestPatchDto extends PrimePatchDto {
    }

    @Nested
    @DisplayName("isActive")
    class IsActive {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @NullSource
        @DisplayName("returns the flag given to the builder")
        void givenBuilderActive_whenIsActive_shouldReturnSameValue(Boolean active) {
            var dto = TestPatchDto.builder().active(active).build();

            assertThat(dto).isInstanceOf(Dtoable.class);
            assertThat(dto.isActive()).isEqualTo(active);
        }

        @Test
        @DisplayName("returns null when the flag was never set")
        void givenNoActive_whenIsActive_shouldReturnNull() {
            assertThat(TestPatchDto.builder().build().isActive()).isNull();
        }

        @Test
        @DisplayName("reflects the value given to the setter")
        void givenSetter_whenIsActive_shouldReturnNewValue() {
            var dto = TestPatchDto.builder().active(true).build();

            dto.setActive(false);

            assertThat(dto.isActive()).isFalse();
        }
    }
}
