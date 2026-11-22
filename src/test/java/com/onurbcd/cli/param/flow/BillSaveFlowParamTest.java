package com.onurbcd.cli.param.flow;

import com.onurbcd.cli.dto.bill.BillDto;
import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.FlowType;
import com.onurbcd.cli.enums.PaymentType;
import com.onurbcd.cli.enums.ReferenceType;
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
import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static com.onurbcd.cli.TestConstants.RECEIPT_ID;
import static com.onurbcd.cli.TestConstants.SOURCE_ID;
import static com.onurbcd.cli.TestFactory.createFullBillDto;
import static com.onurbcd.cli.TestFactory.createSelectItems;
import static com.onurbcd.cli.util.EnumUtil.getCodeableItems;
import static org.assertj.core.api.Assertions.assertThat;

class BillSaveFlowParamTest {

    private static final List<SelectItem> BUDGETS = createSelectItems("budget");
    private static final List<SelectItem> SOURCES = createSelectItems("source");

    @TempDir
    Path filesDir;

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("when bill is complete, should convert its fields, documents and enums")
        void givenFullBill_whenOf_shouldConvertEveryField() throws IOException {
            var bill = createFullBillDto();
            var file = Files.createFile(filesDir.resolve("bill.txt"));
            var params = SaveFlowParam.bill(BUDGETS, SOURCES, filesDir.toString());

            var result = BillSaveFlowParam.of(bill, params);

            var expected = BillSaveFlowParam.builder()
                    .referenceDay("2024-03-15")
                    .documentDate("2024-03-10")
                    .dueDate("2024-03-20")
                    .paymentDate("2024-03-18")
                    .billDocument(DOCUMENT_ID.toString())
                    .billDocumentItem(SelectItem.of("doc.pdf", DOCUMENT_ID.toString(), true, true))
                    .receipt(RECEIPT_ID.toString())
                    .receiptItem(SelectItem.of("receipt.pdf", RECEIPT_ID.toString(), true, true))
                    .observation("observation")
                    .installment("2")
                    .documentType("BILLET")
                    .documentTypeItems(getCodeableItems(DocumentType.values()))
                    .budget(PARENT_ID.toString())
                    .budgetItems(BUDGETS)
                    .referenceType(ReferenceType.YEAR.name())
                    .referenceTypeItems(getCodeableItems(ReferenceType.values()))
                    .filesNames(List.of(SelectItem.of("bill.txt", file.toString())))
                    .paymentType("PIX")
                    .paymentTypeItems(getCodeableItems(PaymentType.values()))
                    .source(SOURCE_ID.toString())
                    .sourceItems(SOURCES)
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
        }

        @Test
        @DisplayName("when bill is null, should leave the fields empty and keep the flow items")
        void givenNullBill_whenOf_shouldLeaveFieldsEmpty() throws IOException {
            var file = Files.createFile(filesDir.resolve("bill.txt"));
            var params = SaveFlowParam.bill(BUDGETS, SOURCES, filesDir.toString());

            var result = BillSaveFlowParam.of(null, params);

            var expected = BillSaveFlowParam.builder()
                    .documentTypeItems(getCodeableItems(DocumentType.values()))
                    .budgetItems(BUDGETS)
                    .referenceTypeItems(getCodeableItems(ReferenceType.values()))
                    .filesNames(List.of(SelectItem.of("bill.txt", file.toString())))
                    .paymentTypeItems(getCodeableItems(PaymentType.values()))
                    .sourceItems(SOURCES)
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
        }

        @Test
        @DisplayName("when bill has no documents, should leave the document fields null")
        void givenBillWithoutDocuments_whenOf_shouldLeaveDocumentFieldsNull() throws IOException {
            Files.createFile(filesDir.resolve("bill.txt"));
            var params = SaveFlowParam.bill(BUDGETS, SOURCES, filesDir.toString());

            var result = BillSaveFlowParam.of(new BillDto(), params);

            assertThat(result.getBillDocument()).isNull();
            assertThat(result.getBillDocumentItem()).isNull();
            assertThat(result.getReceipt()).isNull();
            assertThat(result.getReceiptItem()).isNull();
        }
    }

    @Nested
    @DisplayName("getType")
    class GetType {

        @Test
        @DisplayName("should be BILL")
        void whenGetType_shouldBeBill() {
            assertThat(BillSaveFlowParam.builder().build().getType()).isEqualTo(FlowType.BILL);
        }
    }
}
