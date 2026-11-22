package com.onurbcd.cli.dto.budget;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.onurbcd.cli.TestConstants.BILL_TYPE_ID;
import static org.assertj.core.api.Assertions.assertThat;

class BudgetValuesDtoTest {

    @Test
    @DisplayName("the record exposes its components")
    void givenValues_shouldExposeComponents() {
        var dto = new BudgetValuesDto(BigDecimal.TEN, BILL_TYPE_ID, "bills/water", true);

        assertThat(dto.amount()).isEqualTo(BigDecimal.TEN);
        assertThat(dto.billTypeId()).isEqualTo(BILL_TYPE_ID);
        assertThat(dto.path()).isEqualTo("bills/water");
        assertThat(dto.paid()).isTrue();
    }
}
