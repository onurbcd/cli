package com.onurbcd.eruservice.persistency.param;

import com.onurbcd.eruservice.enums.BalanceType;
import org.junit.jupiter.api.Test;

import static com.onurbcd.eruservice.TestConstants.MONTH;
import static com.onurbcd.eruservice.TestConstants.SEQUENCE;
import static com.onurbcd.eruservice.TestConstants.TARGET_SEQUENCE;
import static com.onurbcd.eruservice.TestConstants.YEAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SequenceParamTest {

    @Test
    void givenYearAndMonth_shouldReturnSequenceParam() {
        var sequenceParam = SequenceParam.of(YEAR, MONTH);
        assertNotNull(sequenceParam);
        assertEquals(YEAR, sequenceParam.getYear());
        assertEquals(MONTH, sequenceParam.getMonth());
        assertNull(sequenceParam.getSequence());
        assertNull(sequenceParam.getTargetSequence());
        assertNull(sequenceParam.getBalanceType());
    }

    @Test
    void givenYearAndMonthAndSequenceAndTargetSequence_shouldReturnSequenceParam() {
        var sequenceParam = SequenceParam.of(YEAR, MONTH, SEQUENCE, TARGET_SEQUENCE);
        assertNotNull(sequenceParam);
        assertEquals(YEAR, sequenceParam.getYear());
        assertEquals(MONTH, sequenceParam.getMonth());
        assertEquals(SEQUENCE, sequenceParam.getSequence());
        assertEquals(TARGET_SEQUENCE, sequenceParam.getTargetSequence());
        assertNull(sequenceParam.getBalanceType());
    }

    @Test
    void givenYearAndMonthAndSequenceAndTargetSequenceAndBalanceType_shouldReturnSequenceParam() {
        var sequenceParam = SequenceParam.of(YEAR, MONTH, SEQUENCE, TARGET_SEQUENCE, BalanceType.INCOME);
        assertNotNull(sequenceParam);
        assertEquals(YEAR, sequenceParam.getYear());
        assertEquals(MONTH, sequenceParam.getMonth());
        assertEquals(SEQUENCE, sequenceParam.getSequence());
        assertEquals(TARGET_SEQUENCE, sequenceParam.getTargetSequence());
        assertEquals(BalanceType.INCOME, sequenceParam.getBalanceType());
    }
}
