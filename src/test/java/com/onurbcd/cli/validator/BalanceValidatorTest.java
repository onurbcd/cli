package com.onurbcd.cli.validator;

import com.onurbcd.cli.TestClock;
import com.onurbcd.cli.dto.balance.BalanceSaveDto;
import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.persistency.entity.Balance;
import com.onurbcd.cli.persistency.entity.Day;
import com.onurbcd.cli.persistency.entity.Source;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.SOURCE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BalanceValidatorTest {

    private static final LocalDate TODAY = TestClock.DEFAULT_DATE;
    private static final LocalDate DAY = LocalDate.of(2025, 3, 15);
    private static final short SEQUENCE = 3;
    private static final UUID OTHER_SOURCE_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");

    private final BalanceValidator validator = new BalanceValidator(TestClock.DEFAULT);

    private static BalanceSaveDto dto(LocalDate date) {
        return BalanceSaveDto.builder()
                .sequence(SEQUENCE)
                .dayCalendarDate(date)
                .sourceId(SOURCE_ID)
                .balanceType(BalanceType.INCOME)
                .build();
    }

    private static Balance balance(LocalDate date) {
        var day = new Day();
        day.setCalendarYear((short) date.getYear());
        day.setCalendarMonth((short) date.getMonthValue());
        day.setCalendarDayInMonth((short) date.getDayOfMonth());

        return Balance.builder()
                .id(ID)
                .sequence(SEQUENCE)
                .day(day)
                .source(Source.builder().id(SOURCE_ID).build())
                .balanceType(BalanceType.INCOME)
                .build();
    }

    static Stream<Arguments> changedFields() {
        return Stream.of(
                Arguments.of("sequence", (Consumer<BalanceSaveDto>) d -> d.setSequence((short) 4),
                        ErrorCode.SEQUENCE_CHANGED),
                Arguments.of("null sequence", (Consumer<BalanceSaveDto>) d -> d.setSequence(null),
                        ErrorCode.SEQUENCE_CHANGED),
                Arguments.of("year", (Consumer<BalanceSaveDto>) d -> d.setDayCalendarDate(DAY.plusYears(1)),
                        ErrorCode.DAY_CHANGED),
                Arguments.of("month", (Consumer<BalanceSaveDto>) d -> d.setDayCalendarDate(DAY.plusMonths(1)),
                        ErrorCode.DAY_CHANGED),
                Arguments.of("day", (Consumer<BalanceSaveDto>) d -> d.setDayCalendarDate(DAY.plusDays(1)),
                        ErrorCode.DAY_CHANGED),
                Arguments.of("source", (Consumer<BalanceSaveDto>) d -> d.setSourceId(OTHER_SOURCE_ID),
                        ErrorCode.DAY_SOURCE_CHANGED),
                Arguments.of("balance type", (Consumer<BalanceSaveDto>) d -> d.setBalanceType(BalanceType.OUTCOME),
                        ErrorCode.DAY_BALANCE_TYPE_CHANGED));
    }

    @Nested
    @DisplayName("validate a new balance (no id)")
    class Create {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.onurbcd.cli.validator.BalanceValidatorTest#pastOrPresentDays")
        @DisplayName("accepts a day that is not in the future")
        void givenDayNotInFuture_shouldNotThrow(String label, LocalDate date) {
            var dto = dto(date);

            assertThatCode(() -> validator.validate(dto, null, null)).doesNotThrowAnyException();

            assertThat(dto).usingRecursiveComparison().isEqualTo(dto(date));
        }

        @Test
        @DisplayName("rejects a day after today")
        void givenFutureDay_shouldThrowDayInFuture() {
            var dto = dto(TODAY.plusDays(1));

            assertThatThrownBy(() -> validator.validate(dto, null, null))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.DAY_IN_FUTURE));
        }

        @Test
        @DisplayName("ignores the existing-entity checks, even when an entity is given")
        void givenEntityWithoutId_shouldOnlyCheckTheDay() {
            var dto = dto(TODAY);
            dto.setSequence((short) 9);

            assertThatCode(() -> validator.validate(dto, balance(DAY), null)).doesNotThrowAnyException();
        }
    }

    static Stream<Arguments> pastOrPresentDays() {
        return Stream.of(
                Arguments.of("today", TODAY),
                Arguments.of("yesterday", TODAY.minusDays(1)),
                Arguments.of("a year ago", TODAY.minusYears(1)));
    }

    @Nested
    @DisplayName("validate an existing balance (with id)")
    class Update {

        @Test
        @DisplayName("rejects an id that has no entity")
        void givenIdWithoutEntity_shouldThrowNotFound() {
            var dto = dto(DAY);

            assertThatThrownBy(() -> validator.validate(dto, null, ID))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.ENTITY_DOES_NOT_EXIST);
                        assertThat(e.getMessage()).isEqualTo(ErrorCode.ENTITY_DOES_NOT_EXIST.format(ID));
                    });
        }

        @Test
        @DisplayName("accepts an unchanged balance, even if its day is in the future")
        void givenUnchangedBalance_shouldNotThrow() {
            var futureDay = TODAY.plusYears(5);
            var dto = dto(futureDay);
            var balance = balance(futureDay);

            assertThatCode(() -> validator.validate(dto, balance, ID)).doesNotThrowAnyException();

            assertThat(dto).usingRecursiveComparison().isEqualTo(dto(futureDay));
            assertThat(balance).usingRecursiveComparison().isEqualTo(balance(futureDay));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.onurbcd.cli.validator.BalanceValidatorTest#changedFields")
        @DisplayName("rejects a changed immutable field with the matching error")
        void givenChangedField_shouldThrow(String label, Consumer<BalanceSaveDto> change, ErrorCode expected) {
            var dto = dto(DAY);
            change.accept(dto);

            assertThatThrownBy(() -> validator.validate(dto, balance(DAY), ID))
                    .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getError()).isEqualTo(expected));
        }

        @Test
        @DisplayName("reports the current and the new sequence when it changed")
        void givenChangedSequence_shouldReportBothValues() {
            var dto = dto(DAY);
            dto.setSequence((short) 4);

            assertThatThrownBy(() -> validator.validate(dto, balance(DAY), ID))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.SEQUENCE_CHANGED);
                        assertThat(e.getMessage()).isEqualTo(ErrorCode.SEQUENCE_CHANGED.format(SEQUENCE, (short) 4));
                    });
        }

        @Test
        @DisplayName("checks the sequence before the day, source and balance type")
        void givenEverythingChanged_shouldReportTheSequenceFirst() {
            var dto = dto(DAY.plusDays(1));
            dto.setSequence((short) 4);
            dto.setSourceId(OTHER_SOURCE_ID);
            dto.setBalanceType(BalanceType.OUTCOME);

            assertThatThrownBy(() -> validator.validate(dto, balance(DAY), ID))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.SEQUENCE_CHANGED));
        }
    }
}
