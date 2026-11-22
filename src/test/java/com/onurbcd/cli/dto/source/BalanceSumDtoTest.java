package com.onurbcd.cli.dto.source;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BalanceSumDtoTest {

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        @DisplayName("should keep partial and total in their own fields")
        void givenValues_shouldKeepFields() {
            var dto = new BalanceSumDto(new BigDecimal("1.5"), new BigDecimal("2.5"));

            assertThat(dto.getPartial()).isEqualByComparingTo("1.5");
            assertThat(dto.getTotal()).isEqualByComparingTo("2.5");
        }

        @Test
        @DisplayName("should turn a null partial into zero")
        void givenNullPartial_shouldUseZero() {
            var dto = new BalanceSumDto(null, BigDecimal.TEN);

            assertThat(dto.getPartial()).isSameAs(BigDecimal.ZERO);
            assertThat(dto.getTotal()).isEqualTo(BigDecimal.TEN);
        }

        @Test
        @DisplayName("should turn a null total into zero")
        void givenNullTotal_shouldUseZero() {
            var dto = new BalanceSumDto(BigDecimal.TEN, null);

            assertThat(dto.getPartial()).isEqualTo(BigDecimal.TEN);
            assertThat(dto.getTotal()).isSameAs(BigDecimal.ZERO);
        }
    }

    @Test
    @DisplayName("the no-args constructor and setters work")
    void givenNoArgs_whenSet_shouldExposeValues() {
        var dto = new BalanceSumDto();
        assertThat(dto.getPartial()).isNull();
        assertThat(dto.getTotal()).isNull();

        dto.setPartial(BigDecimal.ONE);
        dto.setTotal(BigDecimal.TEN);

        assertThat(dto.getPartial()).isEqualTo(BigDecimal.ONE);
        assertThat(dto.getTotal()).isEqualTo(BigDecimal.TEN);
    }
}
