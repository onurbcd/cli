package com.onurbcd.cli.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class ClockConfigTest {

    @Nested
    @DisplayName("clock")
    class ClockBean {

        @Test
        @DisplayName("should return the system clock in the default time zone")
        void whenClock_shouldReturnSystemClockInDefaultZone() {
            var result = new ClockConfig().clock();

            assertThat(result).isEqualTo(Clock.system(ZoneId.systemDefault()));
            assertThat(result.getZone()).isEqualTo(ZoneId.systemDefault());
        }
    }
}
