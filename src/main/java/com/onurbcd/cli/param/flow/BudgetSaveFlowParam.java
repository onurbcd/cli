package com.onurbcd.cli.param.flow;

import com.onurbcd.cli.dto.budget.BudgetDto;
import com.onurbcd.cli.enums.FlowType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.SelectItem;

import java.util.List;

import static com.onurbcd.cli.util.ParamUtil.*;

@Builder
@Getter
public class BudgetSaveFlowParam implements Paramable {

    private String name;
    private String refYear;
    private String refMonth;
    private String billType;
    private String amount;
    private Boolean paid;
    private List<SelectItem> billTypeItems;

    public static BudgetSaveFlowParam of(@Nullable BudgetDto budgetDto, SaveFlowParam params) {
        return BudgetSaveFlowParam.builder()
                .name(getString(budgetDto, BudgetDto::getName))
                .refYear(getShort(budgetDto, BudgetDto::getRefYear))
                .refMonth(getShort(budgetDto, BudgetDto::getRefMonth))
                .billType(getString(budgetDto, BudgetDto::getBillTypeName))
                .amount(getBigDecimal(budgetDto, BudgetDto::getAmount))
                .paid(getBoolean(budgetDto, BudgetDto::getPaid, Boolean.FALSE))
                .billTypeItems(params.getBillTypeItems())
                .build();
    }

    @Override
    public FlowType getType() {
        return FlowType.BUDGET;
    }
}
