package com.onurbcd.cli.factory;

import com.onurbcd.cli.dto.PrimeDto;
import com.onurbcd.cli.dto.balance.BalanceDto;
import com.onurbcd.cli.dto.billtype.BillTypeDto;
import com.onurbcd.cli.dto.budget.BudgetDto;
import com.onurbcd.cli.dto.category.CategoryDto;
import com.onurbcd.cli.dto.incomesource.IncomeSourceDto;
import com.onurbcd.cli.dto.secret.SecretDto;
import com.onurbcd.cli.dto.source.SourceDto;
import com.onurbcd.cli.model.*;
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
            case CATEGORY -> CategorySaveFlowParam.of((CategoryDto) dto, params);
            case INCOME_SOURCE -> IncomeSourceSaveFlowParam.of((IncomeSourceDto) dto);
            case SECRET -> SecretSaveFlowParam.of((SecretDto) dto);
            case SOURCE -> SourceSaveFlowParam.of((SourceDto) dto, params);
            case BILL_OPEN -> BillOpenSaveFlowParam.of(params);
        };
    }
}
