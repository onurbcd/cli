package com.onurbcd.cli.factory;

import com.onurbcd.cli.dto.PrimeDto;
import com.onurbcd.cli.dto.PrimeSaveDto;
import com.onurbcd.cli.dto.balance.BalanceDto;
import com.onurbcd.cli.dto.balance.BalanceSaveDto;
import com.onurbcd.cli.dto.billtype.BillTypeDto;
import com.onurbcd.cli.dto.billtype.BillTypeSaveDto;
import com.onurbcd.cli.dto.budget.BudgetDto;
import com.onurbcd.cli.dto.budget.BudgetSaveDto;
import com.onurbcd.cli.enums.FlowType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.context.ComponentContext;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SaveDtoFactory {

    public static PrimeSaveDto create(ComponentContext<?> context, @Nullable PrimeDto dto, FlowType type) {
        return switch (type) {
            case BALANCE -> BalanceSaveDto.of(context, (BalanceDto) dto);
            case BILL_TYPE -> BillTypeSaveDto.of(context, (BillTypeDto) dto);
            case BUDGET -> BudgetSaveDto.of(context, (BudgetDto) dto);
            default -> null;
        };
    }
}
