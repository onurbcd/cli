package com.onurbcd.eruservice.factory;

import com.onurbcd.eruservice.dto.PrimeDto;
import com.onurbcd.eruservice.dto.PrimeSaveDto;
import com.onurbcd.eruservice.dto.balance.BalanceDto;
import com.onurbcd.eruservice.dto.balance.BalanceSaveDto;
import com.onurbcd.eruservice.dto.billtype.BillTypeDto;
import com.onurbcd.eruservice.dto.billtype.BillTypeSaveDto;
import com.onurbcd.eruservice.enums.FlowType;
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
            default -> null;
        };
    }
}
