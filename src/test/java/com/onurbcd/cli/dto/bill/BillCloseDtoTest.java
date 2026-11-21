package com.onurbcd.cli.dto.bill;

import com.onurbcd.cli.TestValidator;
import com.onurbcd.cli.enums.PaymentType;
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
import static com.onurbcd.cli.TestConstants.SOURCE_ID;
import static org.assertj.core.api.Assertions.assertThat;

class BillCloseDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = TestValidator.create();
    }

    private static BillCloseDto validDto() {
        return BillCloseDto.builder()
                .name("bogus")
                .active(true)
                .billId(ID)
                .paymentDateCalendarDate(LocalDate.of(2024, 3, 15))
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
        @DisplayName("with a full context, should map every field")
        void givenFullContext_shouldMapFields() throws IOException {
            var file = Files.writeString(tempDir.resolve("receipt.txt"), "abc");
            var context = ComponentContext.empty();
            context.put("billId", ID.toString());
            context.put("paymentDate", "2024-03-15");
            context.put("paymentType", "PIX");
            context.put("sourceId", SOURCE_ID.toString());
            context.put("receipt", file.toString());

            var result = BillCloseDto.of(context);

            assertThat(result.getName()).isEqualTo("bogus");
            assertThat(result.isActive()).isNull();
            assertThat(result.getBillId()).isEqualTo(ID);
            assertThat(result.getPaymentDateCalendarDate()).isEqualTo(LocalDate.of(2024, 3, 15));
            assertThat(result.getPaymentType()).isEqualTo(PaymentType.PIX);
            assertThat(result.getSourceId()).isEqualTo(SOURCE_ID);
            assertThat(result.getMultipartFile().getOriginalFilename()).isEqualTo("receipt.txt");
            assertThat(result.getMultipartFile().getSize()).isEqualTo(3L);
            result.getMultipartFile().getInputStream().close();
        }

        @Test
        @DisplayName("with an empty context, should leave every field null")
        void givenEmptyContext_shouldLeaveNulls() {
            var result = BillCloseDto.of(ComponentContext.empty());

            assertThat(result).usingRecursiveComparison()
                    .isEqualTo(BillCloseDto.builder().name("bogus").build());
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
            dto.setBillId(null);
            dto.setPaymentDateCalendarDate(null);
            dto.setPaymentType(null);
            dto.setSourceId(null);

            assertThat(TestValidator.messages(validator, dto)).containsExactlyInAnyOrder(
                    "Bill id is required.",
                    "Payment date is required.",
                    "Payment type is required.",
                    "Source id is required.");
        }
    }
}
