package com.onurbcd.eruservice.enums;

import com.onurbcd.eruservice.exception.ApiException;
import com.onurbcd.eruservice.util.NumberUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.function.BinaryOperator;

@RequiredArgsConstructor
@Getter
public enum BalanceOperation {

    INSERT_INCOME(Operation.INSERT, BalanceType.INCOME, NumberUtil::add),
    INSERT_OUTCOME(Operation.INSERT, BalanceType.OUTCOME, NumberUtil::subtract),
    DELETE_INCOME(Operation.DELETE, BalanceType.INCOME, NumberUtil::subtract),
    DELETE_OUTCOME(Operation.DELETE, BalanceType.OUTCOME, NumberUtil::add),
    UPDATE_INCOME(Operation.UPDATE, BalanceType.INCOME, NumberUtil::add),
    UPDATE_OUTCOME(Operation.UPDATE, BalanceType.OUTCOME, NumberUtil::subtract);

    private final Operation operation;
    private final BalanceType balanceType;
    private final BinaryOperator<BigDecimal> func;

    public static BalanceOperation from(Operation operation, BalanceType balanceType) {
        return Arrays
                .stream(BalanceOperation.values())
                .filter(balanceOperation -> balanceOperation.operation == operation &&
                        balanceOperation.balanceType == balanceType)
                .findFirst()
                .orElseThrow(new ApiException(Error.BALANCE_OPERATION_MISSING, operation.name(), balanceType.name()));
    }
}
