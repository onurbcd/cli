package com.onurbcd.cli.model;

import com.onurbcd.cli.dto.source.SourceDto;
import com.onurbcd.cli.enums.CurrencyType;
import com.onurbcd.cli.enums.FlowType;
import com.onurbcd.cli.enums.SourceType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.SelectItem;

import java.util.List;

import static com.onurbcd.cli.util.EnumUtil.getCodeableItems;
import static com.onurbcd.cli.util.ParamUtil.*;

@Builder
@Getter
public class SourceSaveFlowParam implements Paramable {

    private String name;
    private String incomeSource;
    private String sourceType;
    private String currencyType;
    private String balance;
    private List<SelectItem> incomeSourceItems;
    private List<SelectItem> sourceTypeItems;
    private List<SelectItem> currencyTypeItems;

    public static SourceSaveFlowParam of(@Nullable SourceDto source, SaveFlowParam params) {
        return SourceSaveFlowParam.builder()
                .name(getString(source, SourceDto::getName))
                .incomeSource(getString(source, SourceDto::getIncomeSourceName))
                .sourceType(getEnum(source, SourceDto::getSourceType))
                .currencyType(getEnum(source, SourceDto::getCurrencyType))
                .balance(getBigDecimal(source, SourceDto::getBalance))
                .incomeSourceItems(params.getIncomeSourceItems())
                .sourceTypeItems(getCodeableItems(SourceType.values()))
                .currencyTypeItems(getCodeableItems(CurrencyType.values()))
                .build();
    }

    @Override
    public FlowType getType() {
        return FlowType.SOURCE;
    }
}
