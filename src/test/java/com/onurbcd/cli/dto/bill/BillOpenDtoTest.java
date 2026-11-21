package com.onurbcd.cli.dto.bill;

import com.onurbcd.cli.TestValidator;
import com.onurbcd.cli.enums.DocumentType;
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

import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;

class BillOpenDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = TestValidator.create();
    }

    private static BillOpenDto validDto() {
        return BillOpenDto.builder()
                .name("bogus")
                .active(true)
                .referenceDayCalendarDate(LocalDate.of(2024, 3, 15))
                .dueDateCalendarDate(LocalDate.of(2024, 3, 20))
                .documentType(DocumentType.BILLET)
                .budgetId(ID)
                .referenceType(ReferenceType.MONTH)
                .build();
    }

    @Nested
    @DisplayName("of")
    class Of {

        @TempDir
        Path tempDir;

        @Test
        @DisplayName("with a full context, should map every field and default to active")
        void givenFullContext_shouldMapFields() throws IOException {
            var file = Files.writeString(tempDir.resolve("bill.txt"), "abcd");
            var context = ComponentContext.empty();
            context.put("referenceDay", "2024-03-15");
            context.put("documentDate", "2024-03-10");
            context.put("dueDate", "2024-03-20");
            context.put("observation", "  some   obs ");
            context.put("installment", "3");
            context.put("documentType", "BILLET");
            context.put("budget", ID.toString());
            context.put("referenceType", "MONTH");
            context.put("document", file.toString());

            var result = BillOpenDto.of(context);

            assertThat(result.getName()).isEqualTo("bogus");
            assertThat(result.isActive()).isTrue();
            assertThat(result.getReferenceDayCalendarDate()).isEqualTo(LocalDate.of(2024, 3, 15));
            assertThat(result.getDocumentDateCalendarDate()).isEqualTo(LocalDate.of(2024, 3, 10));
            assertThat(result.getDueDateCalendarDate()).isEqualTo(LocalDate.of(2024, 3, 20));
            assertThat(result.getObservation()).isEqualTo("some obs");
            assertThat(result.getInstallment()).isEqualTo((short) 3);
            assertThat(result.getDocumentType()).isEqualTo(DocumentType.BILLET);
            assertThat(result.getBudgetId()).isEqualTo(ID);
            assertThat(result.getReferenceType()).isEqualTo(ReferenceType.MONTH);
            assertThat(result.getMultipartFile().getOriginalFilename()).isEqualTo("bill.txt");
            assertThat(result.getMultipartFile().getSize()).isEqualTo(4L);
            result.getMultipartFile().getInputStream().close();
        }

        @Test
        @DisplayName("with an empty context, should leave every field null except name and active")
        void givenEmptyContext_shouldLeaveNulls() {
            var result = BillOpenDto.of(ComponentContext.empty());

            assertThat(result).usingRecursiveComparison()
                    .isEqualTo(BillOpenDto.builder().name("bogus").active(true).build());
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

            assertThat(TestValidator.messages(validator, dto)).containsExactlyInAnyOrder(
                    "Reference day is required.",
                    "Due date is required.",
                    "Document type is required.",
                    "Budget is required.",
                    "Reference type is required.");
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
