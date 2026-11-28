package com.onurbcd.cli.factory;

import com.onurbcd.cli.RecordingFlowBuilder;
import com.onurbcd.cli.RecordingFlowBuilder.Step;
import com.onurbcd.cli.enums.FlowField;
import com.onurbcd.cli.param.flow.BalanceSaveFlowParam;
import com.onurbcd.cli.param.flow.BillCloseSaveFlowParam;
import com.onurbcd.cli.param.flow.BillOpenSaveFlowParam;
import com.onurbcd.cli.param.flow.BillSaveFlowParam;
import com.onurbcd.cli.param.flow.BillTypeSaveFlowParam;
import com.onurbcd.cli.param.flow.BudgetSaveFlowParam;
import com.onurbcd.cli.param.flow.CategorySaveFlowParam;
import com.onurbcd.cli.param.flow.IncomeSourceSaveFlowParam;
import com.onurbcd.cli.param.flow.Paramable;
import com.onurbcd.cli.param.flow.SecretSaveFlowParam;
import com.onurbcd.cli.param.flow.SourceSaveFlowParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class FlowFactoryTest {

    private static final Map<String, List<SelectItem>> ITEMS = new LinkedHashMap<>();

    private RecordingFlowBuilder recorder;

    @BeforeEach
    void setUp() {
        recorder = new RecordingFlowBuilder();
    }

    /**
     * One distinct, stable item list per label, so a swapped accessor in the production code shows up as a diff.
     */
    private static List<SelectItem> items(String label) {
        return ITEMS.computeIfAbsent(label, l -> List.of(SelectItem.of(l, l + "-id")));
    }

    private static Step input(FlowField field, String defaultValue) {
        var settings = new LinkedHashMap<String, Object>();
        settings.put("name", field.getName());

        if (defaultValue != null) {
            settings.put("defaultValue", defaultValue);
        }

        return new Step("string", field.getId(), settings);
    }

    private static Step select(FlowField field, List<SelectItem> items, String defaultValue) {
        var settings = new LinkedHashMap<String, Object>();
        settings.put("name", field.getName());
        settings.put("selectItems", items);

        if (defaultValue != null) {
            settings.put("defaultSelect", defaultValue);
        }

        settings.put("max", items.size());
        return new Step("single", field.getId(), settings);
    }

    private static Step multiSelect(FlowField field, List<SelectItem> items, List<String> defaults) {
        var settings = new LinkedHashMap<String, Object>();
        settings.put("name", field.getName());
        settings.put("selectItems", items);
        settings.put("resultValues", defaults);
        settings.put("max", items.size());
        return new Step("multi", field.getId(), settings);
    }

    private static Step confirm(FlowField field, Boolean defaultValue) {
        var settings = new LinkedHashMap<String, Object>();
        settings.put("name", field.getName());
        settings.put("defaultValue", defaultValue);
        return new Step("confirm", field.getId(), settings);
    }

    private void assertFlow(Paramable params, Step... expectedSteps) {
        var supplier = FlowFactory.create(recorder.builder(), params);

        assertThat(recorder.steps()).as("flow must be lazy").isEmpty();

        var result = supplier.get();

        assertThat(result).isSameAs(recorder.result());
        assertThat(recorder.steps()).usingRecursiveComparison().isEqualTo(List.of(expectedSteps));
        verify(recorder.flow()).run();
        verifyNoMoreInteractions(recorder.flow());
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("CATEGORY, should build name, description and parent steps")
        void givenCategory_shouldBuildCategoryFlow() {
            var params = CategorySaveFlowParam.builder()
                    .name("n").description("d").items(items("items")).parent("p").build();

            assertFlow(params,
                    input(FlowField.NAME, "n"),
                    input(FlowField.DESCRIPTION, "d"),
                    select(FlowField.PARENT_ID, items("items"), "p"));
        }

        @Test
        @DisplayName("CATEGORY without defaults, should omit the default values")
        void givenCategoryWithoutDefaults_shouldOmitDefaults() {
            var params = CategorySaveFlowParam.builder().items(items("items")).build();

            assertFlow(params,
                    input(FlowField.NAME, null),
                    input(FlowField.DESCRIPTION, null),
                    select(FlowField.PARENT_ID, items("items"), null));
        }

        @Test
        @DisplayName("BILL_TYPE, should build name, path and category steps")
        void givenBillType_shouldBuildBillTypeFlow() {
            var params = BillTypeSaveFlowParam.builder()
                    .name("n").path("p").categoryItems(items("categories")).category("c").build();

            assertFlow(params,
                    input(FlowField.NAME, "n"),
                    input(FlowField.PATH, "p"),
                    select(FlowField.CATEGORY_ID, items("categories"), "c"));
        }

        @Test
        @DisplayName("INCOME_SOURCE, should build only the name step")
        void givenIncomeSource_shouldBuildIncomeSourceFlow() {
            var params = IncomeSourceSaveFlowParam.builder().name("n").build();

            assertFlow(params, input(FlowField.NAME, "n"));
        }

        @Test
        @DisplayName("SOURCE, should build name, selectors and balance steps in order")
        void givenSource_shouldBuildSourceFlow() {
            var params = SourceSaveFlowParam.builder()
                    .name("n").incomeSource("is").sourceType("st").currencyType("ct").balance("b")
                    .incomeSourceItems(items("incomeSources"))
                    .sourceTypeItems(items("sourceTypes"))
                    .currencyTypeItems(items("currencyTypes"))
                    .build();

            assertFlow(params,
                    input(FlowField.NAME, "n"),
                    select(FlowField.INCOME_SOURCE_ID, items("incomeSources"), "is"),
                    select(FlowField.SOURCE_TYPE, items("sourceTypes"), "st"),
                    select(FlowField.CURRENCY_TYPE, items("currencyTypes"), "ct"),
                    input(FlowField.BALANCE, "b"));
        }

        @Test
        @DisplayName("SECRET, should build four text steps and a masked password step without default")
        void givenSecret_shouldBuildSecretFlow() {
            var params = SecretSaveFlowParam.builder()
                    .name("n").description("d").link("l").username("u").password("p").build();

            assertFlow(params,
                    input(FlowField.NAME, "n"),
                    input(FlowField.DESCRIPTION, "d"),
                    input(FlowField.LINK, "l"),
                    input(FlowField.USERNAME, "u"),
                    new Step("string", FlowField.PASSWORD.getId(),
                            Map.of("name", FlowField.PASSWORD.getName(), "maskCharacter", '*')));
        }

        @Test
        @DisplayName("BUDGET, should build text, bill type and paid confirmation steps")
        void givenBudget_shouldBuildBudgetFlow() {
            var params = BudgetSaveFlowParam.builder()
                    .name("n").refYear("2024").refMonth("3").billType("bt").amount("9.99").paid(true)
                    .billTypeItems(items("billTypes")).build();

            assertFlow(params,
                    input(FlowField.NAME, "n"),
                    input(FlowField.REF_YEAR, "2024"),
                    input(FlowField.REF_MONTH, "3"),
                    select(FlowField.BILL_TYPE_ID, items("billTypes"), "bt"),
                    input(FlowField.AMOUNT, "9.99"),
                    confirm(FlowField.PAID, true));
        }

        @Test
        @DisplayName("BILL_OPEN, should build steps without default values")
        void givenBillOpen_shouldBuildBillOpenFlow() {
            var params = BillOpenSaveFlowParam.builder()
                    .documentTypeItems(items("documentTypes"))
                    .budgetItems(items("budgets"))
                    .referenceTypeItems(items("referenceTypes"))
                    .filesNames(items("files"))
                    .build();

            assertFlow(params,
                    input(FlowField.REFERENCE_DAY, null),
                    input(FlowField.DOCUMENT_DATE, null),
                    input(FlowField.DUE_DATE, null),
                    input(FlowField.OBSERVATION, null),
                    input(FlowField.INSTALLMENT, null),
                    select(FlowField.DOCUMENT_TYPE, items("documentTypes"), null),
                    select(FlowField.BUDGET, items("budgets"), null),
                    select(FlowField.REFERENCE_TYPE, items("referenceTypes"), null),
                    select(FlowField.DOCUMENT, items("files"), null));
        }

        @Test
        @DisplayName("BILL_CLOSE, should build steps without default values")
        void givenBillClose_shouldBuildBillCloseFlow() {
            var params = BillCloseSaveFlowParam.builder()
                    .billItems(items("bills"))
                    .paymentTypeItems(items("paymentTypes"))
                    .sourceItems(items("sources"))
                    .filesNames(items("files"))
                    .build();

            assertFlow(params,
                    select(FlowField.BILL_ID, items("bills"), null),
                    input(FlowField.PAYMENT_DATE, null),
                    select(FlowField.PAYMENT_TYPE, items("paymentTypes"), null),
                    select(FlowField.SOURCE_ID, items("sources"), null),
                    select(FlowField.RECEIPT, items("files"), null));
        }

        private BalanceSaveFlowParam.BalanceSaveFlowParamBuilder balanceParams() {
            return BalanceSaveFlowParam.builder()
                    .day("day").source("s").category("c").amount("a").code("code").description("d")
                    .balanceType("bt").paymentType("pt")
                    .sourceItems(items("sources"))
                    .categoryItems(items("categories"))
                    .balanceTypeItems(items("balanceTypes"))
                    .paymentTypeItems(items("paymentTypes"))
                    .filesNames(items("files"));
        }

        private List<Step> balanceBaseSteps() {
            return new ArrayList<>(List.of(
                    input(FlowField.DAY, "day"),
                    select(FlowField.SOURCE, items("sources"), "s"),
                    select(FlowField.CATEGORY, items("categories"), "c"),
                    input(FlowField.AMOUNT, "a"),
                    input(FlowField.CODE, "code"),
                    input(FlowField.DESCRIPTION, "d"),
                    select(FlowField.BALANCE_TYPE, items("balanceTypes"), "bt"),
                    select(FlowField.PAYMENT_TYPE, items("paymentTypes"), "pt")));
        }

        @Test
        @DisplayName("BALANCE with linked documents, should add the linked documents multi select before files")
        void givenBalanceWithLinkedDocuments_shouldAddLinkedDocumentsStep() {
            var params = balanceParams()
                    .linkedDocuments(items("linked"))
                    .linkedDocumentsDefault(List.of("linked-id"))
                    .build();
            var expected = balanceBaseSteps();
            expected.add(multiSelect(FlowField.DOCUMENTS_IDS, items("linked"), List.of("linked-id")));
            expected.add(multiSelect(FlowField.DOCUMENTS, items("files"), List.of()));

            assertFlow(params, expected.toArray(Step[]::new));
        }

        @Test
        @DisplayName("BALANCE with empty linked documents, should skip the linked documents step")
        void givenBalanceWithEmptyLinkedDocuments_shouldSkipLinkedDocumentsStep() {
            var params = balanceParams().linkedDocuments(List.of()).build();
            var expected = balanceBaseSteps();
            expected.add(multiSelect(FlowField.DOCUMENTS, items("files"), List.of()));

            assertFlow(params, expected.toArray(Step[]::new));
        }

        @Test
        @DisplayName("BALANCE with null linked documents, should skip the linked documents step")
        void givenBalanceWithNullLinkedDocuments_shouldSkipLinkedDocumentsStep() {
            var params = balanceParams().build();
            var expected = balanceBaseSteps();
            expected.add(multiSelect(FlowField.DOCUMENTS, items("files"), List.of()));

            assertFlow(params, expected.toArray(Step[]::new));
        }

        @ParameterizedTest(name = "[{index}] document item present={0}, receipt item present={1}")
        @CsvSource({"false,false", "true,false", "false,true", "true,true"})
        @DisplayName("BILL, should add linked document/receipt steps only when their items exist")
        void givenBill_shouldAddLinkedStepsOnlyWhenItemsExist(boolean hasDocument, boolean hasReceipt) {
            var documentItem = SelectItem.of("document", "document-id", true, true);
            var receiptItem = SelectItem.of("receipt", "receipt-id", true, true);
            var params = BillSaveFlowParam.builder()
                    .referenceDay("rd").documentDate("dd").dueDate("due").paymentDate("pd")
                    .billDocument("bd").billDocumentItem(hasDocument ? documentItem : null)
                    .receipt("r").receiptItem(hasReceipt ? receiptItem : null)
                    .observation("o").installment("i")
                    .documentType("dt").documentTypeItems(items("documentTypes"))
                    .budget("b").budgetItems(items("budgets"))
                    .referenceType("rt").referenceTypeItems(items("referenceTypes"))
                    .filesNames(items("files"))
                    .paymentType("pt").paymentTypeItems(items("paymentTypes"))
                    .source("s").sourceItems(items("sources"))
                    .build();

            var expected = new ArrayList<>(List.of(
                    input(FlowField.REFERENCE_DAY, "rd"),
                    input(FlowField.DOCUMENT_DATE, "dd"),
                    input(FlowField.DUE_DATE, "due"),
                    input(FlowField.PAYMENT_DATE, "pd")));

            if (hasDocument) {
                expected.add(select(FlowField.LINKED_DOCUMENT, List.of(documentItem), "bd"));
            }

            if (hasReceipt) {
                expected.add(select(FlowField.LINKED_RECEIPT, List.of(receiptItem), "r"));
            }

            expected.addAll(List.of(
                    input(FlowField.OBSERVATION, "o"),
                    input(FlowField.INSTALLMENT, "i"),
                    select(FlowField.DOCUMENT_TYPE, items("documentTypes"), "dt"),
                    select(FlowField.BUDGET, items("budgets"), "b"),
                    select(FlowField.REFERENCE_TYPE, items("referenceTypes"), "rt"),
                    select(FlowField.DOCUMENT, items("files"), null),
                    select(FlowField.RECEIPT, items("files"), null),
                    select(FlowField.PAYMENT_TYPE, items("paymentTypes"), "pt"),
                    select(FlowField.SOURCE_ID, items("sources"), "s")));

            assertFlow(params, expected.toArray(Step[]::new));
        }
    }
}
