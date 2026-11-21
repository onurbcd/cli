package com.onurbcd.cli.dto.balance;

import com.onurbcd.cli.TestValidator;
import com.onurbcd.cli.dto.PrimeSaveDto;
import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.enums.PaymentType;
import com.onurbcd.cli.exception.ApiException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.shell.jline.tui.component.context.ComponentContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.onurbcd.cli.TestConstants.CATEGORY_ID;
import static com.onurbcd.cli.TestConstants.DOCUMENT_ID;
import static com.onurbcd.cli.TestConstants.SOURCE_ID;
import static com.onurbcd.cli.TestFactory.createBalanceDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BalanceSaveDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = TestValidator.create();
    }

    private static BalanceSaveDto validDto() {
        return BalanceSaveDto.builder()
                .name("bogus")
                .active(true)
                .sequence((short) 1)
                .dayCalendarDate(LocalDate.of(2024, 3, 15))
                .sourceId(SOURCE_ID)
                .categoryId(CATEGORY_ID)
                .amount(new BigDecimal("10.00"))
                .code("code")
                .description("description")
                .balanceType(BalanceType.INCOME)
                .paymentType(PaymentType.CASH)
                .build();
    }

    private static Set<String> messages(BalanceSaveDto dto) {
        return validator.validate(dto).stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
    }

    private static ComponentContext<?> completeContext() {
        var context = ComponentContext.empty();
        context.put("day", "2024-03-15");
        context.put("source", SOURCE_ID.toString());
        context.put("category", CATEGORY_ID.toString());
        context.put("amount", "12.50");
        context.put("code", "  CODE   X ");
        context.put("description", "  some   text ");
        context.put("balanceType", "OUTCOME");
        context.put("paymentType", "PIX");
        return context;
    }

    @Nested
    @DisplayName("of")
    class Of {

        @TempDir
        Path tempDir;

        @Test
        @DisplayName("without a balance, should map the context and default to active")
        void givenNoBalance_shouldMapContext() {
            var result = BalanceSaveDto.of(completeContext(), null);

            assertThat(result).isInstanceOf(PrimeSaveDto.class);
            assertThat(result).usingRecursiveComparison().isEqualTo(
                    BalanceSaveDto.builder()
                            .name("bogus")
                            .active(true)
                            .dayCalendarDate(LocalDate.of(2024, 3, 15))
                            .sourceId(SOURCE_ID)
                            .categoryId(CATEGORY_ID)
                            .amount(new BigDecimal("12.50"))
                            .code("CODE X")
                            .description("some text")
                            .balanceType(BalanceType.OUTCOME)
                            .paymentType(PaymentType.PIX)
                            .multipartFiles(List.of())
                            .build());
        }

        @Test
        @DisplayName("with a balance and an empty context, should keep its active flag, sequence and documents")
        void givenBalanceAndEmptyContext_shouldKeepBalanceValues() {
            var balance = createBalanceDto();

            var result = BalanceSaveDto.of(ComponentContext.empty(), balance);

            assertThat(result.isActive()).isFalse();
            assertThat(result.getSequence()).isEqualTo((short) 3);
            assertThat(result.getDocumentsIds()).containsExactly(DOCUMENT_ID);
            assertThat(result.getDayCalendarDate()).isNull();
            assertThat(result.getAmount()).isNull();
            assertThat(result.getBalanceType()).isNull();
            assertThat(result.getPaymentType()).isNull();
            assertThat(result.getMultipartFiles()).isEmpty();
        }

        @Test
        @DisplayName("with documents ids in the context, should use them instead of the balance ones")
        void givenDocumentsIdsInContext_shouldOverrideBalance() {
            var other = UUID.fromString("55555555-5555-5555-5555-555555555555");
            var context = ComponentContext.empty();
            context.put("documentsIds", List.of(other.toString(), "not-a-uuid"));

            var result = BalanceSaveDto.of(context, createBalanceDto());

            assertThat(result.getDocumentsIds()).containsExactly(other);
        }

        @Test
        @DisplayName("with an empty documents ids list, should clear the documents ids")
        void givenEmptyDocumentsIds_shouldBeEmptySet() {
            var context = ComponentContext.empty();
            context.put("documentsIds", List.of());

            var result = BalanceSaveDto.of(context, createBalanceDto());

            assertThat(result.getDocumentsIds()).isEmpty();
        }

        @Test
        @DisplayName("with files in the context, should convert them into multipart files")
        void givenFiles_shouldBuildMultipartFiles() throws IOException {
            var file = Files.writeString(tempDir.resolve("note.txt"), "hello");
            var context = ComponentContext.empty();
            context.put("documents", List.of(file.toString()));

            var result = BalanceSaveDto.of(context, null);

            assertThat(result.getMultipartFiles()).hasSize(1);
            var multipart = result.getMultipartFiles().getFirst();
            assertThat(multipart.getOriginalFilename()).isEqualTo("note.txt");
            assertThat(multipart.getSize()).isEqualTo(5L);
            assertThat(multipart.getInputStream()).isNotNull();
            multipart.getInputStream().close();
        }

        @Test
        @DisplayName("with a malformed day, should fail with the parse error code")
        void givenInvalidDay_shouldThrow() {
            var context = ComponentContext.empty();
            context.put("day", "15/03/2024");

            assertThatThrownBy(() -> BalanceSaveDto.of(context, null))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.PARSE_LOCAL_DATE));
        }
    }

    @Nested
    @DisplayName("date parts")
    class DateParts {

        @Test
        @DisplayName("should return year, month and day of the calendar date")
        void givenDate_shouldReturnParts() {
            var dto = validDto();
            dto.setDayCalendarDate(LocalDate.of(2025, 11, 28));

            assertThat(dto.getYear()).isEqualTo((short) 2025);
            assertThat(dto.getMonth()).isEqualTo((short) 11);
            assertThat(dto.getDay()).isEqualTo((short) 28);
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("a valid dto has no violations")
        void givenValid_shouldHaveNoViolations() {
            assertThat(messages(validDto())).isEmpty();
        }

        @Test
        @DisplayName("optional fields may be null")
        void givenOptionalNull_shouldHaveNoViolations() {
            var dto = validDto();
            dto.setSequence(null);
            dto.setCode(null);
            dto.setDescription(null);

            assertThat(messages(dto)).isEmpty();
        }

        @Test
        @DisplayName("sequence below 1 is rejected, 1 is accepted")
        void givenSequence_shouldCheckMin() {
            var dto = validDto();
            dto.setSequence((short) 0);

            assertThat(messages(dto)).containsExactly("Sequence must be greater than 0.");
        }

        @Test
        @DisplayName("each required field is reported when null")
        void givenNulls_shouldReportEachRequired() {
            var dto = validDto();
            dto.setDayCalendarDate(null);
            dto.setSourceId(null);
            dto.setCategoryId(null);
            dto.setAmount(null);
            dto.setBalanceType(null);
            dto.setPaymentType(null);

            assertThat(messages(dto)).containsExactlyInAnyOrder(
                    "Day calendar date is required.",
                    "Source is required.",
                    "Category is required.",
                    "Amount is required.",
                    "Balance type is required.",
                    "Payment type is required.");
        }

        @ParameterizedTest
        @ValueSource(strings = {"-999999999999999", "999999999999999"})
        @DisplayName("amount on the limits is accepted")
        void givenAmountOnLimit_shouldBeAccepted(String amount) {
            var dto = validDto();
            dto.setAmount(new BigDecimal(amount));

            assertThat(messages(dto)).isEmpty();
        }

        @Test
        @DisplayName("amount below the minimum is rejected")
        void givenAmountBelowMin_shouldBeRejected() {
            var dto = validDto();
            dto.setAmount(new BigDecimal("-1000000000000000"));

            assertThat(messages(dto)).containsExactly("Amount must be greater than -999999999999999");
        }

        @Test
        @DisplayName("amount above the maximum is rejected")
        void givenAmountAboveMax_shouldBeRejected() {
            var dto = validDto();
            dto.setAmount(new BigDecimal("1000000000000000"));

            assertThat(messages(dto)).containsExactly("Amount must be less than 999999999999999");
        }

        @Test
        @DisplayName("code of 150 characters is accepted, 151 is rejected")
        void givenCode_shouldCheckSize() {
            var dto = validDto();
            dto.setCode("c".repeat(150));
            assertThat(messages(dto)).isEmpty();

            dto.setCode("c".repeat(151));
            assertThat(messages(dto)).containsExactly("Code must be less than 150 characters.");
        }

        @Test
        @DisplayName("description of 250 characters is accepted, 251 is rejected")
        void givenDescription_shouldCheckSize() {
            var dto = validDto();
            dto.setDescription("d".repeat(250));
            assertThat(messages(dto)).isEmpty();

            dto.setDescription("d".repeat(251));
            assertThat(messages(dto)).containsExactly("Description must be less than 250 characters.");
        }
    }
}
