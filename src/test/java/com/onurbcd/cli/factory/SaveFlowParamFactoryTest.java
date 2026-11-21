package com.onurbcd.cli.factory;

import com.onurbcd.cli.dto.PrimeDto;
import com.onurbcd.cli.enums.FlowType;
import com.onurbcd.cli.param.flow.BalanceSaveFlowParam;
import com.onurbcd.cli.param.flow.BillCloseSaveFlowParam;
import com.onurbcd.cli.param.flow.BillOpenSaveFlowParam;
import com.onurbcd.cli.param.flow.BillSaveFlowParam;
import com.onurbcd.cli.param.flow.BillTypeSaveFlowParam;
import com.onurbcd.cli.param.flow.BudgetSaveFlowParam;
import com.onurbcd.cli.param.flow.CategorySaveFlowParam;
import com.onurbcd.cli.param.flow.IncomeSourceSaveFlowParam;
import com.onurbcd.cli.param.flow.SaveFlowParam;
import com.onurbcd.cli.param.flow.SecretSaveFlowParam;
import com.onurbcd.cli.param.flow.SourceSaveFlowParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

class SaveFlowParamFactoryTest {

    private static final List<SelectItem> SOURCES = List.of(SelectItem.of("source", "source-id"));
    private static final List<SelectItem> CATEGORIES = List.of(SelectItem.of("category", "category-id"));
    private static final List<SelectItem> BILL_TYPES = List.of(SelectItem.of("billType", "billType-id"));
    private static final List<SelectItem> INCOME_SOURCES = List.of(SelectItem.of("incomeSource", "incomeSource-id"));
    private static final List<SelectItem> BUDGETS = List.of(SelectItem.of("budget", "budget-id"));
    private static final List<SelectItem> BILLS = List.of(SelectItem.of("bill", "bill-id"));

    @TempDir
    private Path filesDir;

    private List<SelectItem> expectedFiles;

    @BeforeEach
    void setUp() throws IOException {
        var file = Files.createFile(filesDir.resolve("receipt.pdf"));
        expectedFiles = List.of(SelectItem.of("receipt.pdf", file.toString()));
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("BALANCE, should build a BalanceSaveFlowParam")
        void givenBalance_shouldBuildBalanceParam() {
            var params = SaveFlowParam.balance(SOURCES, CATEGORIES, filesDir.toString());

            var result = SaveFlowParamFactory.create(createBalanceDto(), params);

            assertThat(result).isInstanceOfSatisfying(BalanceSaveFlowParam.class, p -> {
                assertThat(p.getType()).isEqualTo(FlowType.BALANCE);
                assertThat(p.getSource()).isEqualTo("source");
                assertThat(p.getCategory()).isEqualTo("category");
                assertThat(p.getSourceItems()).isSameAs(SOURCES);
                assertThat(p.getCategoryItems()).isSameAs(CATEGORIES);
                assertThat(p.getFilesNames()).usingRecursiveComparison().isEqualTo(expectedFiles);
            });
        }

        @Test
        @DisplayName("BILL_TYPE, should build a BillTypeSaveFlowParam")
        void givenBillType_shouldBuildBillTypeParam() {
            var params = SaveFlowParam.billType(CATEGORIES);

            var result = SaveFlowParamFactory.create(createBillTypeDto(), params);

            assertThat(result).isInstanceOfSatisfying(BillTypeSaveFlowParam.class, p -> {
                assertThat(p.getType()).isEqualTo(FlowType.BILL_TYPE);
                assertThat(p.getPath()).isEqualTo("bills/water");
                assertThat(p.getCategoryItems()).isSameAs(CATEGORIES);
            });
        }

        @Test
        @DisplayName("BUDGET, should build a BudgetSaveFlowParam")
        void givenBudget_shouldBuildBudgetParam() {
            var params = SaveFlowParam.budget(BILL_TYPES);

            var result = SaveFlowParamFactory.create(createBudgetDto(), params);

            assertThat(result).isInstanceOfSatisfying(BudgetSaveFlowParam.class, p -> {
                assertThat(p.getType()).isEqualTo(FlowType.BUDGET);
                assertThat(p.getRefYear()).isEqualTo("2024");
                assertThat(p.getBillTypeItems()).isSameAs(BILL_TYPES);
            });
        }

        @Test
        @DisplayName("CATEGORY, should build a CategorySaveFlowParam")
        void givenCategory_shouldBuildCategoryParam() {
            var params = SaveFlowParam.category(CATEGORIES);

            var result = SaveFlowParamFactory.create(createCategoryDto(), params);

            assertThat(result).isInstanceOfSatisfying(CategorySaveFlowParam.class, p -> {
                assertThat(p.getType()).isEqualTo(FlowType.CATEGORY);
                assertThat(p.getDescription()).isEqualTo("description");
                assertThat(p.getItems()).isSameAs(CATEGORIES);
            });
        }

        @Test
        @DisplayName("INCOME_SOURCE, should build an IncomeSourceSaveFlowParam ignoring the flow params")
        void givenIncomeSource_shouldBuildIncomeSourceParam() {
            var result = SaveFlowParamFactory.create(createIncomeSourceDto(),
                    SaveFlowParam.noArgs(FlowType.INCOME_SOURCE));

            assertThat(result).isInstanceOfSatisfying(IncomeSourceSaveFlowParam.class, p -> {
                assertThat(p.getType()).isEqualTo(FlowType.INCOME_SOURCE);
                assertThat(p.getName()).isEqualTo("bogus");
            });
        }

        @Test
        @DisplayName("SECRET, should build a SecretSaveFlowParam ignoring the flow params")
        void givenSecret_shouldBuildSecretParam() {
            var result = SaveFlowParamFactory.create(createSecretDto(), SaveFlowParam.noArgs(FlowType.SECRET));

            assertThat(result).isInstanceOfSatisfying(SecretSaveFlowParam.class, p -> {
                assertThat(p.getType()).isEqualTo(FlowType.SECRET);
                assertThat(p.getUsername()).isEqualTo("user");
            });
        }

        @Test
        @DisplayName("SOURCE, should build a SourceSaveFlowParam")
        void givenSource_shouldBuildSourceParam() {
            var params = SaveFlowParam.source(INCOME_SOURCES);

            var result = SaveFlowParamFactory.create(createSourceDto(), params);

            assertThat(result).isInstanceOfSatisfying(SourceSaveFlowParam.class, p -> {
                assertThat(p.getType()).isEqualTo(FlowType.SOURCE);
                assertThat(p.getIncomeSource()).isEqualTo("income");
                assertThat(p.getIncomeSourceItems()).isSameAs(INCOME_SOURCES);
            });
        }

        @Test
        @DisplayName("BILL_OPEN, should build a BillOpenSaveFlowParam without a dto")
        void givenBillOpen_shouldBuildBillOpenParam() {
            var params = SaveFlowParam.billOpen(BUDGETS, filesDir.toString());

            var result = SaveFlowParamFactory.create(null, params);

            assertThat(result).isInstanceOfSatisfying(BillOpenSaveFlowParam.class, p -> {
                assertThat(p.getType()).isEqualTo(FlowType.BILL_OPEN);
                assertThat(p.getBudgetItems()).isSameAs(BUDGETS);
                assertThat(p.getFilesNames()).usingRecursiveComparison().isEqualTo(expectedFiles);
            });
        }

        @Test
        @DisplayName("BILL_CLOSE, should build a BillCloseSaveFlowParam without a dto")
        void givenBillClose_shouldBuildBillCloseParam() {
            var params = SaveFlowParam.billClose(BILLS, SOURCES, filesDir.toString());

            var result = SaveFlowParamFactory.create(null, params);

            assertThat(result).isInstanceOfSatisfying(BillCloseSaveFlowParam.class, p -> {
                assertThat(p.getType()).isEqualTo(FlowType.BILL_CLOSE);
                assertThat(p.getBillItems()).isSameAs(BILLS);
                assertThat(p.getSourceItems()).isSameAs(SOURCES);
                assertThat(p.getFilesNames()).usingRecursiveComparison().isEqualTo(expectedFiles);
            });
        }

        @Test
        @DisplayName("BILL, should build a BillSaveFlowParam")
        void givenBill_shouldBuildBillParam() {
            var params = SaveFlowParam.bill(BUDGETS, SOURCES, filesDir.toString());

            var result = SaveFlowParamFactory.create(createBillDto(), params);

            assertThat(result).isInstanceOfSatisfying(BillSaveFlowParam.class, p -> {
                assertThat(p.getType()).isEqualTo(FlowType.BILL);
                assertThat(p.getReferenceDay()).isEqualTo("2024-03-15");
                assertThat(p.getBudgetItems()).isSameAs(BUDGETS);
                assertThat(p.getSourceItems()).isSameAs(SOURCES);
                assertThat(p.getFilesNames()).usingRecursiveComparison().isEqualTo(expectedFiles);
            });
        }

        @Test
        @DisplayName("with a dto of another type, should fail with ClassCastException")
        void givenWrongDtoType_shouldThrowClassCastException() {
            PrimeDto wrong = createBillDto();
            var params = SaveFlowParam.category(CATEGORIES);

            assertThatThrownBy(() -> SaveFlowParamFactory.create(wrong, params))
                    .isInstanceOf(ClassCastException.class);
        }
    }
}
