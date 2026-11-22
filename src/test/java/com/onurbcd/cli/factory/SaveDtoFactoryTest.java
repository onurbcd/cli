package com.onurbcd.cli.factory;

import com.onurbcd.cli.dto.PrimeDto;
import com.onurbcd.cli.dto.balance.BalanceSaveDto;
import com.onurbcd.cli.dto.bill.BillCloseDto;
import com.onurbcd.cli.dto.bill.BillOpenDto;
import com.onurbcd.cli.dto.bill.BillSaveDto;
import com.onurbcd.cli.dto.billtype.BillTypeSaveDto;
import com.onurbcd.cli.dto.budget.BudgetSaveDto;
import com.onurbcd.cli.dto.category.CategorySaveDto;
import com.onurbcd.cli.dto.incomesource.IncomeSourceSaveDto;
import com.onurbcd.cli.dto.secret.SecretSaveDto;
import com.onurbcd.cli.dto.source.SourceSaveDto;
import com.onurbcd.cli.enums.FlowField;
import com.onurbcd.cli.enums.FlowType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.shell.jline.tui.component.context.ComponentContext;

import static com.onurbcd.cli.TestConstants.BILL_TYPE_ID;
import static com.onurbcd.cli.TestConstants.CATEGORY_ID;
import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static com.onurbcd.cli.TestConstants.SOURCE_ID;
import static com.onurbcd.cli.TestFactory.createBalanceDto;
import static com.onurbcd.cli.TestFactory.createBillDto;
import static com.onurbcd.cli.TestFactory.createBillTypeDto;
import static com.onurbcd.cli.TestFactory.createBudgetDto;
import static com.onurbcd.cli.TestFactory.createCategoryDto;
import static com.onurbcd.cli.TestFactory.createIncomeSourceDto;
import static com.onurbcd.cli.TestFactory.createSecretDto;
import static com.onurbcd.cli.TestFactory.createSourceDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaveDtoFactoryTest {

    private static ComponentContext<?> context(Object... keyValues) {
        var context = ComponentContext.empty();

        for (var i = 0; i < keyValues.length; i += 2) {
            context.put(((FlowField) keyValues[i]).getId(), keyValues[i + 1]);
        }

        return context;
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("BALANCE, should build a BalanceSaveDto from the context and the balance")
        void givenBalance_shouldBuildBalanceSaveDto() {
            var context = context(FlowField.SOURCE, SOURCE_ID.toString(), FlowField.CODE, " C  1 ");
            var balance = createBalanceDto();

            var result = SaveDtoFactory.create(context, balance, FlowType.BALANCE);

            assertThat(result).isInstanceOfSatisfying(BalanceSaveDto.class, dto -> {
                assertThat(dto.getSourceId()).isEqualTo(SOURCE_ID);
                assertThat(dto.getCode()).isEqualTo("C 1");
                assertThat(dto.getSequence()).isEqualTo(balance.getSequence());
                assertThat(dto.isActive()).isEqualTo(balance.isActive());
            });
        }

        @Test
        @DisplayName("BILL_TYPE, should build a BillTypeSaveDto from the context and the bill type")
        void givenBillType_shouldBuildBillTypeSaveDto() {
            var context = context(FlowField.PATH, " bills/gas ", FlowField.CATEGORY_ID, CATEGORY_ID.toString());

            var result = SaveDtoFactory.create(context, createBillTypeDto(), FlowType.BILL_TYPE);

            assertThat(result).isInstanceOfSatisfying(BillTypeSaveDto.class, dto -> {
                assertThat(dto.getPath()).isEqualTo("bills/gas");
                assertThat(dto.getCategoryId()).isEqualTo(CATEGORY_ID);
                assertThat(dto.isActive()).isFalse();
            });
        }

        @Test
        @DisplayName("BUDGET, should build a BudgetSaveDto from the context and the budget")
        void givenBudget_shouldBuildBudgetSaveDto() {
            var context = context(FlowField.BILL_TYPE_ID, BILL_TYPE_ID.toString(), FlowField.REF_YEAR, "2025");

            var result = SaveDtoFactory.create(context, createBudgetDto(), FlowType.BUDGET);

            assertThat(result).isInstanceOfSatisfying(BudgetSaveDto.class, dto -> {
                assertThat(dto.getBillTypeId()).isEqualTo(BILL_TYPE_ID);
                assertThat(dto.getRefYear()).isEqualTo((short) 2025);
                assertThat(dto.getSequence()).isEqualTo((short) 4);
            });
        }

        @Test
        @DisplayName("CATEGORY, should build a CategorySaveDto from the context and the category")
        void givenCategory_shouldBuildCategorySaveDto() {
            var context = context(FlowField.NAME, " Food  Court ", FlowField.PARENT_ID, PARENT_ID.toString());

            var result = SaveDtoFactory.create(context, createCategoryDto(), FlowType.CATEGORY);

            assertThat(result).isInstanceOfSatisfying(CategorySaveDto.class, dto -> {
                assertThat(dto.getName()).isEqualTo("Food Court");
                assertThat(dto.getParentId()).isEqualTo(PARENT_ID);
                assertThat(dto.isActive()).isFalse();
            });
        }

        @Test
        @DisplayName("INCOME_SOURCE, should build an IncomeSourceSaveDto from the context and the income source")
        void givenIncomeSource_shouldBuildIncomeSourceSaveDto() {
            var context = context(FlowField.NAME, " Salary ");

            var result = SaveDtoFactory.create(context, createIncomeSourceDto(), FlowType.INCOME_SOURCE);

            assertThat(result).isInstanceOfSatisfying(IncomeSourceSaveDto.class, dto -> {
                assertThat(dto.getName()).isEqualTo("Salary");
                assertThat(dto.isActive()).isFalse();
            });
        }

        @Test
        @DisplayName("SECRET, should build a SecretSaveDto from the context and the secret")
        void givenSecret_shouldBuildSecretSaveDto() {
            var context = context(FlowField.USERNAME, " bob ", FlowField.PASSWORD, " keep spaces ");

            var result = SaveDtoFactory.create(context, createSecretDto(), FlowType.SECRET);

            assertThat(result).isInstanceOfSatisfying(SecretSaveDto.class, dto -> {
                assertThat(dto.getUsername()).isEqualTo("bob");
                assertThat(dto.getPassword()).isEqualTo(" keep spaces ");
                assertThat(dto.isActive()).isFalse();
            });
        }

        @Test
        @DisplayName("SOURCE, should build a SourceSaveDto from the context and the source")
        void givenSource_shouldBuildSourceSaveDto() {
            var context = context(FlowField.INCOME_SOURCE_ID, PARENT_ID.toString(), FlowField.BALANCE, "7.25");

            var result = SaveDtoFactory.create(context, createSourceDto(), FlowType.SOURCE);

            assertThat(result).isInstanceOfSatisfying(SourceSaveDto.class, dto -> {
                assertThat(dto.getIncomeSourceId()).isEqualTo(PARENT_ID);
                assertThat(dto.getBalance()).isEqualByComparingTo("7.25");
                assertThat(dto.isActive()).isFalse();
            });
        }

        @Test
        @DisplayName("BILL_OPEN, should build a BillOpenDto ignoring the dto")
        void givenBillOpen_shouldBuildBillOpenDto() {
            var context = context(FlowField.OBSERVATION, " note ", FlowField.INSTALLMENT, "3");

            var result = SaveDtoFactory.create(context, null, FlowType.BILL_OPEN);

            assertThat(result).isInstanceOfSatisfying(BillOpenDto.class, dto -> {
                assertThat(dto.getObservation()).isEqualTo("note");
                assertThat(dto.getInstallment()).isEqualTo((short) 3);
                assertThat(dto.isActive()).isTrue();
            });
        }

        @Test
        @DisplayName("BILL_CLOSE, should build a BillCloseDto ignoring the dto")
        void givenBillClose_shouldBuildBillCloseDto() {
            var context = context(FlowField.SOURCE_ID, SOURCE_ID.toString(), FlowField.PAYMENT_DATE, "2024-03-15");

            var result = SaveDtoFactory.create(context, null, FlowType.BILL_CLOSE);

            assertThat(result).isInstanceOfSatisfying(BillCloseDto.class, dto -> {
                assertThat(dto.getSourceId()).isEqualTo(SOURCE_ID);
                assertThat(dto.getPaymentDateCalendarDate()).hasToString("2024-03-15");
            });
        }

        @Test
        @DisplayName("BILL, should build a BillSaveDto from the context and the bill")
        void givenBill_shouldBuildBillSaveDto() {
            var context = context(FlowField.OBSERVATION, " note ", FlowField.SOURCE_ID, SOURCE_ID.toString());

            var result = SaveDtoFactory.create(context, createBillDto(), FlowType.BILL);

            assertThat(result).isInstanceOfSatisfying(BillSaveDto.class, dto -> {
                assertThat(dto.getObservation()).isEqualTo("note");
                assertThat(dto.getSourceId()).isEqualTo(SOURCE_ID);
                assertThat(dto.isActive()).isFalse();
                assertThat(dto.getClosed()).isFalse();
            });
        }

        @Test
        @DisplayName("with a dto of another type, should fail with ClassCastException")
        void givenWrongDtoType_shouldThrowClassCastException() {
            PrimeDto wrong = createBillDto();

            var context = context();

            assertThatThrownBy(() -> SaveDtoFactory.create(context, wrong, FlowType.CATEGORY))
                    .isInstanceOf(ClassCastException.class);
        }
    }
}
