package com.onurbcd.cli.dto.bill;

import com.onurbcd.cli.TestValidator;
import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.PaymentType;
import com.onurbcd.cli.enums.ReferenceType;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.shell.jline.tui.component.context.ComponentContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

import static com.onurbcd.cli.TestConstants.DOCUMENT_ID;
import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.SOURCE_ID;
import static com.onurbcd.cli.TestFactory.createBillDto;
import static com.onurbcd.cli.TestFactory.createDocumentDto;
import static org.assertj.core.api.Assertions.assertThat;

class BillSaveDtoTest {

    private static final UUID OTHER_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = TestValidator.create();
    }

    private static BillSaveDto validDto() {
        return BillSaveDto.builder()
                .name("bogus")
                .active(true)
                .referenceDayCalendarDate(LocalDate.of(2024, 3, 15))
                .dueDateCalendarDate(LocalDate.of(2024, 3, 20))
                .documentType(DocumentType.BILLET)
                .budgetId(ID)
                .referenceType(ReferenceType.MONTH)
                .closed(false)
                .paymentType(PaymentType.PIX)
                .sourceId(SOURCE_ID)
                .build();
    }

    @Nested
    @DisplayName("of")
    class Of {

        @TempDir
        Path tempDir;

        @Test
        @DisplayName("with a full context and no bill, should map every field and default active/closed to true")
        void givenFullContextAndNoBill_shouldMapFields() throws IOException {
            var doc = Files.writeString(tempDir.resolve("doc.txt"), "ab");
            var receipt = Files.writeString(tempDir.resolve("receipt.txt"), "abc");
            var context = ComponentContext.empty();
            context.put("referenceDay", "2024-03-15");
            context.put("documentDate", "2024-03-10");
            context.put("dueDate", "2024-03-20");
            context.put("paymentDate", "2024-03-18");
            context.put("documentId", DOCUMENT_ID.toString());
            context.put("document", doc.toString());
            context.put("receiptId", OTHER_ID.toString());
            context.put("receipt", receipt.toString());
            context.put("observation", "  some   obs ");
            context.put("installment", "2");
            context.put("documentType", "BILLET");
            context.put("budget", ID.toString());
            context.put("referenceType", "MONTH");
            context.put("paymentType", "PIX");
            context.put("sourceId", SOURCE_ID.toString());

            var result = BillSaveDto.of(context, null);

            assertThat(result.getName()).isEqualTo("bogus");
            assertThat(result.isActive()).isTrue();
            assertThat(result.getClosed()).isTrue();
            assertThat(result.getReferenceDayCalendarDate()).isEqualTo(LocalDate.of(2024, 3, 15));
            assertThat(result.getDocumentDateCalendarDate()).isEqualTo(LocalDate.of(2024, 3, 10));
            assertThat(result.getDueDateCalendarDate()).isEqualTo(LocalDate.of(2024, 3, 20));
            assertThat(result.getPaymentDateCalendarDate()).isEqualTo(LocalDate.of(2024, 3, 18));
            assertThat(result.getBillDocumentId()).isEqualTo(DOCUMENT_ID);
            assertThat(result.getBillDocumentFile().getOriginalFilename()).isEqualTo("doc.txt");
            assertThat(result.getReceiptId()).isEqualTo(OTHER_ID);
            assertThat(result.getReceiptFile().getOriginalFilename()).isEqualTo("receipt.txt");
            assertThat(result.getObservation()).isEqualTo("some obs");
            assertThat(result.getInstallment()).isEqualTo((short) 2);
            assertThat(result.getDocumentType()).isEqualTo(DocumentType.BILLET);
            assertThat(result.getBudgetId()).isEqualTo(ID);
            assertThat(result.getReferenceType()).isEqualTo(ReferenceType.MONTH);
            assertThat(result.getPaymentType()).isEqualTo(PaymentType.PIX);
            assertThat(result.getSourceId()).isEqualTo(SOURCE_ID);
            assertThat(result.getBalanceId()).isNull();
            result.getBillDocumentFile().getInputStream().close();
            result.getReceiptFile().getInputStream().close();
        }

        @Test
        @DisplayName("with a bill and an empty context, should keep the bill flags and linked documents")
        void givenBillAndEmptyContext_shouldKeepBillValues() {
            var bill = createBillDto();
            bill.setBillDocument(createDocumentDto());
            bill.setReceipt(createDocumentDto());

            var result = BillSaveDto.of(ComponentContext.empty(), bill);

            assertThat(result.isActive()).isFalse();
            assertThat(result.getClosed()).isFalse();
            assertThat(result.getBillDocumentId()).isEqualTo(DOCUMENT_ID);
            assertThat(result.getReceiptId()).isEqualTo(DOCUMENT_ID);
            assertThat(result.getBillDocumentFile()).isNull();
            assertThat(result.getReceiptFile()).isNull();
        }

        @Test
        @DisplayName("with a bill without documents and an empty context, should leave document ids null")
        void givenBillWithoutDocuments_shouldLeaveIdsNull() {
            var result = BillSaveDto.of(ComponentContext.empty(), createBillDto());

            assertThat(result.getBillDocumentId()).isNull();
            assertThat(result.getReceiptId()).isNull();
        }

        @Test
        @DisplayName("with linked document ids in the context, should override the bill ones")
        void givenLinkedIds_shouldOverrideBill() {
            var bill = createBillDto();
            bill.setBillDocument(createDocumentDto());
            bill.setReceipt(createDocumentDto());
            var context = ComponentContext.empty();
            context.put("documentId", OTHER_ID.toString());
            context.put("receiptId", "not-a-uuid");

            var result = BillSaveDto.of(context, bill);

            assertThat(result.getBillDocumentId()).isEqualTo(OTHER_ID);
            assertThat(result.getReceiptId()).isNull();
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("a valid dto has no violations")
        void givenValid_shouldHaveNoViolations() {
            assertThat(TestValidator.messages(validator, validDto())).isEmpty();
        }

        @Test
        @DisplayName("each required field is reported when null")
        void givenNulls_shouldReportEachRequired() {
            var dto = validDto();
            dto.setReferenceDayCalendarDate(null);
            dto.setDueDateCalendarDate(null);
            dto.setDocumentType(null);
            dto.setBudgetId(null);
            dto.setReferenceType(null);
            dto.setClosed(null);
            dto.setPaymentType(null);
            dto.setSourceId(null);

            assertThat(TestValidator.messages(validator, dto)).containsExactlyInAnyOrder(
                    "Reference day is required.",
                    "Due date is required.",
                    "Document type is required.",
                    "Budget is required.",
                    "Reference type is required.",
                    "Closed is required.",
                    "Payment type is required.",
                    "Source id is required.");
        }

        @Test
        @DisplayName("observation of 250 characters is accepted, 251 is rejected")
        void givenObservation_shouldCheckSize() {
            var dto = validDto();
            dto.setObservation("o".repeat(250));
            assertThat(TestValidator.messages(validator, dto)).isEmpty();

            dto.setObservation("o".repeat(251));
            assertThat(TestValidator.messages(validator, dto))
                    .containsExactly("Observation must be less than 250 characters.");
        }

        @Test
        @DisplayName("installment 1 is accepted, 0 is rejected")
        void givenInstallment_shouldCheckMin() {
            var dto = validDto();
            dto.setInstallment((short) 1);
            assertThat(TestValidator.messages(validator, dto)).isEmpty();

            dto.setInstallment((short) 0);
            assertThat(TestValidator.messages(validator, dto))
                    .containsExactly("Installment must be greater than or equal to 1.");
        }
    }
}
