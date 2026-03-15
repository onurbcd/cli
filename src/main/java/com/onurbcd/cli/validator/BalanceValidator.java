package com.onurbcd.cli.validator;

import com.onurbcd.cli.annotation.Validator;
import com.onurbcd.cli.dto.balance.BalanceSaveDto;
import com.onurbcd.cli.enums.Error;
import com.onurbcd.cli.persistency.entity.Balance;
import com.onurbcd.cli.util.DateUtil;
import com.onurbcd.cli.util.NumberUtil;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.UUID;

@Validator
public class BalanceValidator {

    public void validate(BalanceSaveDto dto, @Nullable Balance balance, @Nullable UUID id) {
        Action.checkIf(id == null || balance != null).orElseThrowNotFound(id);
        validateSequence(dto, balance, id);
        validateDay(dto, balance, id);
        validateSource(dto, balance, id);
        validateBalanceType(dto, balance, id);
    }

    private void validateSequence(BalanceSaveDto dto, @Nullable Balance balance, @Nullable UUID id) {
        var sequence = balance != null ? balance.getSequence() : null;

        Action.checkIf(id == null || NumberUtil.equals(dto.getSequence(), sequence))
                .orElseThrow(Error.SEQUENCE_CHANGED, sequence, dto.getSequence());
    }

    private void validateDay(BalanceSaveDto dto, @Nullable Balance balance, @Nullable UUID id) {
        Action.checkIf(id == null || equalDay(dto, balance)).orElseThrow(Error.DAY_CHANGED);
        Action.checkIf(id != null || dayNotFuture(dto.getDayCalendarDate())).orElseThrow(Error.DAY_IN_FUTURE);
    }

    private boolean equalDay(BalanceSaveDto dto, @Nullable Balance balance) {
        if (balance == null) {
            return false;
        }

        return DateUtil.equalDay(dto.getYear(), dto.getMonth(), dto.getDay(), balance.getSequenceYear(),
                balance.getSequenceMonth(), balance.getDayInMonth());
    }

    private boolean dayNotFuture(LocalDate dayCalendarDate) {
        var localDateNow = LocalDate.now();
        var isEqual = dayCalendarDate.isEqual(localDateNow);
        var isBefore = dayCalendarDate.isBefore(localDateNow);
        return isEqual || isBefore;
    }

    private void validateSource(BalanceSaveDto dto, @Nullable Balance balance, @Nullable UUID id) {
        var sourceId = balance != null ? balance.getSource().getId() : null;
        Action.checkIf(id == null || dto.getSourceId().equals(sourceId)).orElseThrow(Error.DAY_SOURCE_CHANGED);
    }

    private void validateBalanceType(BalanceSaveDto dto, @Nullable Balance balance, @Nullable UUID id) {
        var balanceType = balance != null ? balance.getBalanceType() : null;
        Action.checkIf(id == null || dto.getBalanceType() == balanceType).orElseThrow(Error.DAY_BALANCE_TYPE_CHANGED);
    }
}
