package com.onurbcd.cli.dto.secret;

import com.onurbcd.cli.dto.PrimePatchDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SecretPatchDtoTest {

    @Nested
    @DisplayName("of")
    class Of {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @NullSource
        @DisplayName("should build a patch carrying the given active flag")
        void givenActive_whenOf_shouldCarryFlag(Boolean active) {
            var result = SecretPatchDto.of(active);

            assertThat(result).isInstanceOf(PrimePatchDto.class);
            assertThat(result.isActive()).isEqualTo(active);
        }
    }
}
