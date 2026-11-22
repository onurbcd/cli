package com.onurbcd.cli.param;

import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.persistency.entity.SequenceEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class SequenceParamTest {

    private static final short YEAR = 2024;
    private static final short MONTH = 3;
    private static final short SEQUENCE = 7;
    private static final short TARGET = 9;

    private final SequenceEntity entity = mock(SequenceEntity.class);

    private void stubEntity() {
        when(entity.getSequenceYear()).thenReturn(YEAR);
        when(entity.getSequenceMonth()).thenReturn(MONTH);
    }

    @Nested
    @DisplayName("of(entity)")
    class OfEntity {

        @Test
        void givenEntity_whenOf_shouldCopyYearAndMonthOnly() {
            stubEntity();

            var result = SequenceParam.of(entity);

            assertThat(result).usingRecursiveComparison()
                    .isEqualTo(SequenceParam.of(YEAR, MONTH, null, null, null));
            verify(entity).getSequenceYear();
            verify(entity).getSequenceMonth();
            verifyNoMoreInteractions(entity);
        }
    }

    @Nested
    @DisplayName("of(date)")
    class OfDate {

        @Test
        void givenDate_whenOf_shouldUseItsYearAndMonth() {
            var result = SequenceParam.of(LocalDate.of(2024, 3, 31));

            assertThat(result).usingRecursiveComparison()
                    .isEqualTo(SequenceParam.of(YEAR, MONTH, null, null, null));
        }
    }

    @Nested
    @DisplayName("of(entity, targetSequence)")
    class OfEntityAndTarget {

        @Test
        void givenEntityAndTarget_whenOf_shouldCopyEntitySequenceAndSetTarget() {
            stubEntity();
            when(entity.getSequenceValue()).thenReturn(SEQUENCE);

            var result = SequenceParam.of(entity, TARGET);

            assertThat(result).usingRecursiveComparison()
                    .isEqualTo(SequenceParam.of(YEAR, MONTH, SEQUENCE, TARGET, null));
            verify(entity).getSequenceYear();
            verify(entity).getSequenceMonth();
            verify(entity).getSequenceValue();
            verifyNoMoreInteractions(entity);
        }
    }

    @Nested
    @DisplayName("of(year, month, sequence, targetSequence, balanceType)")
    class OfAllFields {

        @Test
        void givenAllValues_whenOf_shouldSetEveryField() {
            var result = SequenceParam.of(YEAR, MONTH, SEQUENCE, TARGET, BalanceType.INCOME);

            assertThat(result.getYear()).isEqualTo(YEAR);
            assertThat(result.getMonth()).isEqualTo(MONTH);
            assertThat(result.getSequence()).isEqualTo(SEQUENCE);
            assertThat(result.getTargetSequence()).isEqualTo(TARGET);
            assertThat(result.getBalanceType()).isEqualTo(BalanceType.INCOME);
        }
    }
}
