package com.onurbcd.cli.dto.budget;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BudgetSumDtoTest {

    @Test
    @DisplayName("the all-args constructor stores amount, paid and size")
    void givenArgs_whenConstructed_shouldExposeThem() {
        var dto = new BudgetSumDto(BigDecimal.TEN, true, 3L);

        assertThat(dto.getAmount()).isEqualTo(BigDecimal.TEN);
        assertThat(dto.getPaid()).isTrue();
        assertThat(dto.getSize()).isEqualTo(3L);
    }

    @Test
    @DisplayName("the no-args constructor and setters work")
    void givenNoArgs_whenSet_shouldExposeValues() {
        var dto = new BudgetSumDto();
        assertThat(dto.getAmount()).isNull();
        assertThat(dto.getPaid()).isNull();
        assertThat(dto.getSize()).isNull();

        dto.setAmount(BigDecimal.ONE);
        dto.setPaid(false);
        dto.setSize(9L);

        assertThat(dto.getAmount()).isEqualTo(BigDecimal.ONE);
        assertThat(dto.getPaid()).isFalse();
        assertThat(dto.getSize()).isEqualTo(9L);
    }
}
