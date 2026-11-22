package com.onurbcd.cli.param.flow;

import com.onurbcd.cli.dto.balance.BalanceDto;
import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.FlowType;
import com.onurbcd.cli.enums.PaymentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.onurbcd.cli.TestConstants.DOCUMENT_ID;
import static com.onurbcd.cli.TestFactory.createBalanceDto;
import static com.onurbcd.cli.TestFactory.createSelectItems;
import static com.onurbcd.cli.util.EnumUtil.getCodeableItems;
import static org.assertj.core.api.Assertions.assertThat;

class BalanceSaveFlowParamTest {

    private static final List<SelectItem> SOURCES = createSelectItems("source");
    private static final List<SelectItem> CATEGORIES = createSelectItems("category");

    @TempDir
    Path filesDir;

    private List<SelectItem> expectedFiles() throws IOException {
        var file = Files.createFile(filesDir.resolve("doc.txt"));
        Files.createDirectory(filesDir.resolve("ignored-dir"));
        return List.of(SelectItem.of("doc.txt", file.toString()));
    }

    private SaveFlowParam params() {
        return SaveFlowParam.balance(SOURCES, CATEGORIES, filesDir.toString());
    }

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("when balance is complete, should convert its fields and mark its documents as linked")
        void givenFullBalance_whenOf_shouldConvertEveryField() throws IOException {
            var balance = createBalanceDto();
            var files = expectedFiles();

            var result = BalanceSaveFlowParam.of(balance, params());

            var expected = BalanceSaveFlowParam.builder()
                    .day("2024-03-15")
                    .source("source")
                    .category("category")
                    .amount("12.50")
                    .code("CODE")
                    .description("description")
                    .balanceType("INCOME")
                    .sourceItems(SOURCES)
                    .categoryItems(CATEGORIES)
                    .balanceTypeItems(getCodeableItems(BalanceType.values()))
                    .paymentTypeItems(getCodeableItems(PaymentType.values()))
                    .filesNames(files)
                    .linkedDocuments(List.of(SelectItem.of("doc.pdf", DOCUMENT_ID.toString(), true, true)))
                    .linkedDocumentsDefault(List.of(DOCUMENT_ID.toString()))
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
        }

        @Test
        @DisplayName("when balance is null, should leave the fields empty and keep the flow items")
        void givenNullBalance_whenOf_shouldLeaveFieldsEmpty() throws IOException {
            var files = expectedFiles();

            var result = BalanceSaveFlowParam.of(null, params());

            var expected = BalanceSaveFlowParam.builder()
                    .sourceItems(SOURCES)
                    .categoryItems(CATEGORIES)
                    .balanceTypeItems(getCodeableItems(BalanceType.values()))
                    .paymentTypeItems(getCodeableItems(PaymentType.values()))
                    .filesNames(files)
                    .linkedDocuments(List.of())
                    .linkedDocumentsDefault(List.of())
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
        }

        @Test
        @DisplayName("when balance has no documents, should return empty linked documents")
        void givenBalanceWithoutDocuments_whenOf_shouldReturnEmptyLinkedDocuments() throws IOException {
            var balance = new BalanceDto();
            expectedFiles();

            var result = BalanceSaveFlowParam.of(balance, params());

            assertThat(result.getLinkedDocuments()).isEmpty();
            assertThat(result.getLinkedDocumentsDefault()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getType")
    class GetType {

        @Test
        @DisplayName("should be BALANCE")
        void whenGetType_shouldBeBalance() {
            assertThat(BalanceSaveFlowParam.builder().build().getType()).isEqualTo(FlowType.BALANCE);
        }
    }
}
