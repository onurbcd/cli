package com.onurbcd.cli.service;

import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.param.UpdateSourceBalance;
import com.onurbcd.cli.persistency.entity.Balance;
import com.onurbcd.cli.persistency.entity.Source;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static com.onurbcd.cli.TestConstants.SOURCE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class BalanceSourceServiceTest {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal THIRTY = new BigDecimal("30");

    @Mock
    private SourceService sourceService;
    @Captor
    private ArgumentCaptor<UpdateSourceBalance> paramCaptor;
    @InjectMocks
    private BalanceSourceService service;

    private final Source source = Source.builder().id(SOURCE_ID).balance(HUNDRED).build();

    private Balance balance(BalanceType type, String amount) {
        return Balance.builder().source(source).balanceType(type).amount(new BigDecimal(amount)).build();
    }

    /**
     * Income adds the value to the source balance, outcome subtracts it (100 +/- 30).
     */
    static Stream<Arguments> typesAndResultsForAddingTheValue() {
        return Stream.of(
                Arguments.of(BalanceType.INCOME, new BigDecimal("130")),
                Arguments.of(BalanceType.OUTCOME, new BigDecimal("70")));
    }

    /**
     * Delete is the inverse of insert: income subtracts the value, outcome adds it (100 -/+ 30).
     */
    static Stream<Arguments> typesAndResultsForDelete() {
        return Stream.of(
                Arguments.of(BalanceType.INCOME, new BigDecimal("70")),
                Arguments.of(BalanceType.OUTCOME, new BigDecimal("130")));
    }

    @Nested
    @DisplayName("save")
    class Save {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.onurbcd.cli.service.BalanceSourceServiceTest#typesAndResultsForAddingTheValue")
        @DisplayName("without a current amount, should insert the full amount")
        void givenNoCurrentAmount_shouldInsertFullAmount(BalanceType type, BigDecimal expected) {
            var newBalance = balance(type, "30");

            service.save(newBalance, null);

            verify(sourceService).updateBalance(paramCaptor.capture());
            var param = paramCaptor.getValue();
            assertThat(param.getSource()).isSameAs(source);
            assertThat(param.getValue()).isEqualTo(THIRTY);
            assertThat(param.getFunc().apply(HUNDRED, THIRTY)).isEqualTo(expected);
            verifyNoMoreInteractions(sourceService);
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.onurbcd.cli.service.BalanceSourceServiceTest#typesAndResultsForAddingTheValue")
        @DisplayName("with a smaller current amount, should update with the positive difference")
        void givenSmallerCurrentAmount_shouldUpdateWithDifference(BalanceType type, BigDecimal expected) {
            var newBalance = balance(type, "50");

            service.save(newBalance, new BigDecimal("20"));

            verify(sourceService).updateBalance(paramCaptor.capture());
            var param = paramCaptor.getValue();
            assertThat(param.getSource()).isSameAs(source);
            assertThat(param.getValue()).isEqualTo(THIRTY);
            assertThat(param.getFunc().apply(HUNDRED, THIRTY)).isEqualTo(expected);
            verifyNoMoreInteractions(sourceService);
        }

        @Test
        @DisplayName("with a greater current amount, should update with the negative difference")
        void givenGreaterCurrentAmount_shouldUpdateWithNegativeDifference() {
            var newBalance = balance(BalanceType.INCOME, "5");

            service.save(newBalance, new BigDecimal("8"));

            verify(sourceService).updateBalance(paramCaptor.capture());
            var param = paramCaptor.getValue();
            assertThat(param.getValue()).isEqualByComparingTo("-3");
            assertThat(param.getFunc().apply(HUNDRED, param.getValue())).isEqualByComparingTo("97");
            verifyNoMoreInteractions(sourceService);
        }

        @Test
        @DisplayName("with an equal current amount (even with a different scale), should not touch the source")
        void givenEqualCurrentAmount_shouldNotUpdate() {
            var newBalance = balance(BalanceType.INCOME, "10.00");

            service.save(newBalance, new BigDecimal("10.0"));

            verifyNoMoreInteractions(sourceService);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.onurbcd.cli.service.BalanceSourceServiceTest#typesAndResultsForDelete")
        @DisplayName("should revert the full amount from the source")
        void givenBalance_shouldRevertAmount(BalanceType type, BigDecimal expected) {
            var existing = balance(type, "30");

            service.delete(existing);

            verify(sourceService).updateBalance(paramCaptor.capture());
            var param = paramCaptor.getValue();
            assertThat(param.getSource()).isSameAs(source);
            assertThat(param.getValue()).isEqualTo(THIRTY);
            assertThat(param.getFunc().apply(HUNDRED, THIRTY)).isEqualTo(expected);
            verifyNoMoreInteractions(sourceService);
        }
    }
}
