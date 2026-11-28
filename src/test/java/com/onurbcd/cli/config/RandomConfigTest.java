package com.onurbcd.cli.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RandomConfigTest {

    @Nested
    @DisplayName("secureRandom")
    class SecureRandomBean {

        @Test
        @DisplayName("should return a new SecureRandom that produces random bytes")
        void whenSecureRandom_shouldReturnWorkingSecureRandom() {
            var config = new RandomConfig();

            var result = config.secureRandom();
            var first = new byte[32];
            var second = new byte[32];
            result.nextBytes(first);
            result.nextBytes(second);

            assertThat(result).isNotNull();
            assertThat(first).isNotEqualTo(second).isNotEqualTo(new byte[32]);
            assertThat(config.secureRandom()).isNotSameAs(result);
        }
    }
}
