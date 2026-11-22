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

        assertThat(budget)
                .returns(ID, Budget::getId)
                .returns("rent", Budget::getName)
                .returns(true, Budget::getActive)
                .returns((short) 2, Budget::getSequence)
                .returns((short) 2024, Budget::getRefYear)
                .returns((short) 3, Budget::getRefMonth)
                .satisfies(a -> assertThat(a.getBillType()).isSameAs(billType))
                .returns(AMOUNT, Budget::getAmount)
                .returns(false, Budget::getPaid);
    }

    @Test
    @DisplayName("the all-args constructor keeps every field in order")
    void givenAllArgs_whenConstruct_shouldKeepEveryField() {
        var billType = BillType.builder().id(BILL_TYPE_ID).build();

        var budget = new Budget((short) 4, (short) 2025, (short) 5, billType, AMOUNT, true);

        assertThat(budget)
                .returns((short) 4, Budget::getSequence)
                .returns((short) 2025, Budget::getRefYear)
                .returns((short) 5, Budget::getRefMonth)
                .satisfies(a -> assertThat(a.getBillType()).isSameAs(billType))
                .returns(AMOUNT, Budget::getAmount)
                .returns(true, Budget::getPaid);
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

        assertThat(budget)
                .returns((short) 7, Budget::getSequence)
                .returns((short) 2026, Budget::getRefYear)
                .returns((short) 8, Budget::getRefMonth)
                .satisfies(a -> assertThat(a.getBillType()).isSameAs(billType))
                .returns(AMOUNT, Budget::getAmount)
                .returns(true, Budget::getPaid);
    }

    @Test
    @DisplayName("the sequence accessors expose year, month and sequence")
    void givenBudget_whenSequenceAccessors_shouldExposeReferenceAndSequence() {
        var budget = Budget.builder().sequence((short) 6).refYear((short) 2024).refMonth((short) 11).build();

        assertThat(budget)
                .returns((short) 2024, Budget::getSequenceYear)
                .returns((short) 11, Budget::getSequenceMonth)
                .returns((short) 6, Budget::getSequenceValue);
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
