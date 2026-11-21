package com.onurbcd.cli.dto.budget;

import com.onurbcd.cli.dto.PrimeDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.onurbcd.cli.TestConstants.BILL_TYPE_ID;
import static com.onurbcd.cli.TestFactory.createBudgetDto;
import static org.assertj.core.api.Assertions.assertThat;

class BudgetDtoTest {

    @Test
    @DisplayName("is a PrimeDto and exposes the fields set through the setters")
    void givenFactoryBudget_shouldExposeFields() {
        var budget = createBudgetDto();

        assertThat(budget).isInstanceOf(PrimeDto.class);
        assertThat(budget.getSequence()).isEqualTo((short) 4);
        assertThat(budget.getRefYear()).isEqualTo((short) 2024);
        assertThat(budget.getRefMonth()).isEqualTo((short) 3);
        assertThat(budget.getBillTypeId()).isEqualTo(BILL_TYPE_ID);
        assertThat(budget.getBillTypeName()).isEqualTo("bill type");
        assertThat(budget.getAmount()).isEqualTo(new BigDecimal("100.50"));
        assertThat(budget.getPaid()).isFalse();
    }
}
