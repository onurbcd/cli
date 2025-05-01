package com.onurbcd.eruservice.persistency.param;

import com.onurbcd.eruservice.enums.BalanceType;
import com.onurbcd.eruservice.persistency.entity.SequenceEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class SequenceParam {

    private Short year;
    private Short month;
    private Short sequence;
    private Short targetSequence;
    private BalanceType balanceType;

    public static SequenceParam of(SequenceEntity entity) {
        return new SequenceParam(entity.getSequenceYear(), entity.getSequenceMonth(), null, null, null);
    }

    public static SequenceParam of(LocalDate date) {
        return new SequenceParam((short) date.getYear(), (short) date.getMonthValue(), null, null, null);
    }

    public static SequenceParam of(SequenceEntity entity, Short targetSequence) {
        return new SequenceParam(entity.getSequenceYear(), entity.getSequenceMonth(), entity.getSequenceValue(),
                targetSequence, null);
    }

    public static SequenceParam of(Short year, Short month, Short sequence, Short targetSequence,
                                   BalanceType balanceType) {

        return new SequenceParam(year, month, sequence, targetSequence, balanceType);
    }
}
