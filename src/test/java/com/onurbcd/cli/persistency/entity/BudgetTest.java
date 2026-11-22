package com.onurbcd.cli.persistency.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static com.onurbcd.cli.TestConstants.BILL_TYPE_ID;
import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;

class BudgetTest {

    private static final UUID OTHER_ID = UUID.fromString("99999999-9999-9999-0000-000000000001");
    private static final BigDecimal AMOUNT = new BigDecimal("99.9900");

    @Test
    @DisplayName("the builder fills every field")
    void givenBuilder_whenBuild_shouldFillEveryField() {
        var billType = BillType.builder().id(BILL_TYPE_ID).build();

        var budget = Budget.builder()
                .id(ID)
                .name("rent")
                .active(true)
                .sequence((short) 2)
                .refYear((short) 2024)
                .refMonth((short) 3)
                .billType(billType)
                .amount(AMOUNT)
                .paid(false)
                .build();

        assertThat(budget.getId()).isEqualTo(ID);
        assertThat(budget.getName()).isEqualTo("rent");
        assertThat(budget.getActive()).isTrue();
        assertThat(budget.getSequence()).isEqualTo((short) 2);
        assertThat(budget.getRefYear()).isEqualTo((short) 2024);
        assertThat(budget.getRefMonth()).isEqualTo((short) 3);
        assertThat(budget.getBillType()).isSameAs(billType);
        assertThat(budget.getAmount()).isEqualTo(AMOUNT);
        assertThat(budget.getPaid()).isFalse();
    }

    @Test
    @DisplayName("the all-args constructor keeps every field in order")
    void givenAllArgs_whenConstruct_shouldKeepEveryField() {
        var billType = BillType.builder().id(BILL_TYPE_ID).build();

        var budget = new Budget((short) 4, (short) 2025, (short) 5, billType, AMOUNT, true);

        assertThat(budget.getSequence()).isEqualTo((short) 4);
        assertThat(budget.getRefYear()).isEqualTo((short) 2025);
        assertThat(budget.getRefMonth()).isEqualTo((short) 5);
        assertThat(budget.getBillType()).isSameAs(billType);
        assertThat(budget.getAmount()).isEqualTo(AMOUNT);
        assertThat(budget.getPaid()).isTrue();
    }

    @Test
    @DisplayName("setters store each field")
    void givenValues_whenSet_shouldReturnThemFromGetters() {
        var billType = BillType.builder().id(BILL_TYPE_ID).build();
        var budget = new Budget();

        budget.setSequence((short) 7);
        budget.setRefYear((short) 2026);
        budget.setRefMonth((short) 8);
        budget.setBillType(billType);
        budget.setAmount(AMOUNT);
        budget.setPaid(true);

        assertThat(budget.getSequence()).isEqualTo((short) 7);
        assertThat(budget.getRefYear()).isEqualTo((short) 2026);
        assertThat(budget.getRefMonth()).isEqualTo((short) 8);
        assertThat(budget.getBillType()).isSameAs(billType);
        assertThat(budget.getAmount()).isEqualTo(AMOUNT);
        assertThat(budget.getPaid()).isTrue();
    }

    @Test
    @DisplayName("the sequence accessors expose year, month and sequence")
    void givenBudget_whenSequenceAccessors_shouldExposeReferenceAndSequence() {
        var budget = Budget.builder().sequence((short) 6).refYear((short) 2024).refMonth((short) 11).build();

        assertThat(budget.getSequenceYear()).isEqualTo((short) 2024);
        assertThat(budget.getSequenceMonth()).isEqualTo((short) 11);
        assertThat(budget.getSequenceValue()).isEqualTo((short) 6);
    }

    @Test
    @DisplayName("equality follows the id")
    void givenIds_whenEqualsAndHashCode_shouldFollowId() {
        var budget = Budget.builder().id(ID).build();

        assertThat(budget).isEqualTo(Budget.builder().id(ID).build()).hasSameHashCodeAs(Budget.builder().id(ID).build());
        assertThat(budget).isNotEqualTo(Budget.builder().id(OTHER_ID).build())
                .doesNotHaveSameHashCodeAs(Budget.builder().id(OTHER_ID).build());
    }
}
