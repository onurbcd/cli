package com.onurbcd.eruservice.factory;

import com.onurbcd.eruservice.dto.PrimeDto;
import com.onurbcd.eruservice.dto.balance.BalanceDto;
import com.onurbcd.eruservice.dto.billtype.BillTypeDto;
import com.onurbcd.eruservice.dto.budget.BudgetDto;
import com.onurbcd.eruservice.model.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SaveFlowParamFactory {

    public static Paramable create(@Nullable PrimeDto dto, SaveFlowParam params) {
        return switch (params.getType()) {
            case BALANCE -> BalanceSaveFlowParam.of((BalanceDto) dto, params);
            case BILL_TYPE -> BillTypeSaveFlowParam.of((BillTypeDto) dto, params);
            case BUDGET -> BudgetSaveFlowParam.of((BudgetDto) dto, params);
            default -> throw new IllegalArgumentException("Unsupported FlowType: " + params.getType().name());
        };
    }
}
