package com.onurbcd.cli.param.flow;

import com.onurbcd.cli.enums.FlowType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.util.List;

import static com.onurbcd.cli.TestFactory.createSelectItems;
import static org.assertj.core.api.Assertions.assertThat;

class SaveFlowParamTest {

    private static final String FILES_PATH = "/tmp/files";

    private static final List<SelectItem> SOURCES = createSelectItems("source");
    private static final List<SelectItem> CATEGORIES = createSelectItems("category");
    private static final List<SelectItem> BILL_TYPES = createSelectItems("billType");
    private static final List<SelectItem> INCOME_SOURCES = createSelectItems("incomeSource");
    private static final List<SelectItem> BUDGETS = createSelectItems("budget");
    private static final List<SelectItem> BILLS = createSelectItems("bill");

    @Nested
    @DisplayName("noArgs")
    class NoArgs {

        @Test
        @DisplayName("should set only the type")
        void givenType_whenNoArgs_shouldSetOnlyType() {
            var result = SaveFlowParam.noArgs(FlowType.SECRET);

            assertThat(result.getType()).isEqualTo(FlowType.SECRET);
            assertThat(result).usingRecursiveComparison()
                    .ignoringFields("type")
                    .isEqualTo(SaveFlowParam.noArgs(FlowType.BILL));
        }
    }

    @Nested
    @DisplayName("balance")
    class Balance {

        @Test
        @DisplayName("should set the balance type, sources, categories and files path")
        void givenItems_whenBalance_shouldSetTheirFields() {
            var result = SaveFlowParam.balance(SOURCES, CATEGORIES, FILES_PATH);

            assertThat(result.getType()).isEqualTo(FlowType.BALANCE);
            assertThat(result.getSourceItems()).isSameAs(SOURCES);
            assertThat(result.getCategoryItems()).isSameAs(CATEGORIES);
            assertThat(result.getFilesPath()).isEqualTo(FILES_PATH);
            assertThat(result.getBillTypeItems()).isNull();
            assertThat(result.getIncomeSourceItems()).isNull();
            assertThat(result.getBudgetItems()).isNull();
            assertThat(result.getBillItems()).isNull();
        }
    }

    @Nested
    @DisplayName("billType")
    class BillType {

        @Test
        @DisplayName("should set the bill type and categories")
        void givenCategories_whenBillType_shouldSetTheirFields() {
            var result = SaveFlowParam.billType(CATEGORIES);

            assertThat(result.getType()).isEqualTo(FlowType.BILL_TYPE);
            assertThat(result.getCategoryItems()).isSameAs(CATEGORIES);
            assertThat(result.getSourceItems()).isNull();
            assertThat(result.getFilesPath()).isNull();
            assertThat(result.getBillTypeItems()).isNull();
            assertThat(result.getIncomeSourceItems()).isNull();
            assertThat(result.getBudgetItems()).isNull();
            assertThat(result.getBillItems()).isNull();
        }
    }

    @Nested
    @DisplayName("budget")
    class Budget {

        @Test
        @DisplayName("should set the budget type and bill types")
        void givenBillTypes_whenBudget_shouldSetTheirFields() {
            var result = SaveFlowParam.budget(BILL_TYPES);

            assertThat(result.getType()).isEqualTo(FlowType.BUDGET);
            assertThat(result.getBillTypeItems()).isSameAs(BILL_TYPES);
            assertThat(result.getSourceItems()).isNull();
            assertThat(result.getCategoryItems()).isNull();
            assertThat(result.getFilesPath()).isNull();
            assertThat(result.getIncomeSourceItems()).isNull();
            assertThat(result.getBudgetItems()).isNull();
            assertThat(result.getBillItems()).isNull();
        }
    }

    @Nested
    @DisplayName("category")
    class Category {

        @Test
        @DisplayName("should set the category type and categories")
        void givenCategories_whenCategory_shouldSetTheirFields() {
            var result = SaveFlowParam.category(CATEGORIES);

            assertThat(result.getType()).isEqualTo(FlowType.CATEGORY);
            assertThat(result.getCategoryItems()).isSameAs(CATEGORIES);
            assertThat(result.getSourceItems()).isNull();
            assertThat(result.getFilesPath()).isNull();
            assertThat(result.getBillTypeItems()).isNull();
            assertThat(result.getIncomeSourceItems()).isNull();
            assertThat(result.getBudgetItems()).isNull();
            assertThat(result.getBillItems()).isNull();
        }
    }

    @Nested
    @DisplayName("source")
    class Source {

        @Test
        @DisplayName("should set the source type and income sources")
        void givenIncomeSources_whenSource_shouldSetTheirFields() {
            var result = SaveFlowParam.source(INCOME_SOURCES);

            assertThat(result.getType()).isEqualTo(FlowType.SOURCE);
            assertThat(result.getIncomeSourceItems()).isSameAs(INCOME_SOURCES);
            assertThat(result.getSourceItems()).isNull();
            assertThat(result.getCategoryItems()).isNull();
            assertThat(result.getFilesPath()).isNull();
            assertThat(result.getBillTypeItems()).isNull();
            assertThat(result.getBudgetItems()).isNull();
            assertThat(result.getBillItems()).isNull();
        }
    }

    @Nested
    @DisplayName("billOpen")
    class BillOpen {

        @Test
        @DisplayName("should set the bill open type, budgets and files path")
        void givenItems_whenBillOpen_shouldSetTheirFields() {
            var result = SaveFlowParam.billOpen(BUDGETS, FILES_PATH);

            assertThat(result.getType()).isEqualTo(FlowType.BILL_OPEN);
            assertThat(result.getBudgetItems()).isSameAs(BUDGETS);
            assertThat(result.getFilesPath()).isEqualTo(FILES_PATH);
            assertThat(result.getSourceItems()).isNull();
            assertThat(result.getCategoryItems()).isNull();
            assertThat(result.getBillTypeItems()).isNull();
            assertThat(result.getIncomeSourceItems()).isNull();
            assertThat(result.getBillItems()).isNull();
        }
    }

    @Nested
    @DisplayName("billClose")
    class BillClose {

        @Test
        @DisplayName("should set the bill close type, bills, sources and files path")
        void givenItems_whenBillClose_shouldSetTheirFields() {
            var result = SaveFlowParam.billClose(BILLS, SOURCES, FILES_PATH);

            assertThat(result.getType()).isEqualTo(FlowType.BILL_CLOSE);
            assertThat(result.getBillItems()).isSameAs(BILLS);
            assertThat(result.getSourceItems()).isSameAs(SOURCES);
            assertThat(result.getFilesPath()).isEqualTo(FILES_PATH);
            assertThat(result.getCategoryItems()).isNull();
            assertThat(result.getBillTypeItems()).isNull();
            assertThat(result.getIncomeSourceItems()).isNull();
            assertThat(result.getBudgetItems()).isNull();
        }
    }

    @Nested
    @DisplayName("bill")
    class Bill {

        @Test
        @DisplayName("should set the bill type, budgets, sources and files path")
        void givenItems_whenBill_shouldSetTheirFields() {
            var result = SaveFlowParam.bill(BUDGETS, SOURCES, FILES_PATH);

            assertThat(result.getType()).isEqualTo(FlowType.BILL);
            assertThat(result.getBudgetItems()).isSameAs(BUDGETS);
            assertThat(result.getSourceItems()).isSameAs(SOURCES);
            assertThat(result.getFilesPath()).isEqualTo(FILES_PATH);
            assertThat(result.getCategoryItems()).isNull();
            assertThat(result.getBillTypeItems()).isNull();
            assertThat(result.getIncomeSourceItems()).isNull();
            assertThat(result.getBillItems()).isNull();
        }
    }
}
