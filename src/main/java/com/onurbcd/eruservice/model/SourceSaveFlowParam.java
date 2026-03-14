package com.onurbcd.eruservice.model;

import com.onurbcd.eruservice.dto.source.SourceDto;
import com.onurbcd.eruservice.enums.CurrencyType;
import com.onurbcd.eruservice.enums.SourceType;
import com.onurbcd.eruservice.util.EnumUtil;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.SelectItem;

import java.util.List;

import static com.onurbcd.eruservice.util.ParamUtil.*;

@Builder
@Getter
public class SourceSaveFlowParam {

    private String name;
    private String incomeSource;
    private String sourceType;
    private String currencyType;
    private String balance;
    private List<SelectItem> incomeSourceItems;
    private List<SelectItem> sourceTypeItems;
    private List<SelectItem> currencyTypeItems;

    public static SourceSaveFlowParam of(@Nullable SourceDto source, List<SelectItem> incomeSourceItems) {
        return SourceSaveFlowParam.builder()
                .name(getString(source, SourceDto::getName))
                .incomeSource(getString(source, SourceDto::getIncomeSourceName))
                .sourceType(getEnum(source, SourceDto::getSourceType))
                .currencyType(getEnum(source, SourceDto::getCurrencyType))
                .balance(getBigDecimal(source, SourceDto::getBalance))
                .incomeSourceItems(incomeSourceItems)
                .sourceTypeItems(EnumUtil.getItems(SourceType.values()))
                .currencyTypeItems(EnumUtil.getItems(CurrencyType.values()))
                .build();
    }
}
