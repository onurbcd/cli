package com.onurbcd.eruservice.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.SelectItem;
import org.springframework.shell.standard.ShellOption;

import com.onurbcd.eruservice.dto.source.SourceDto;
import com.onurbcd.eruservice.enums.CurrencyType;
import com.onurbcd.eruservice.enums.SourceType;
import com.onurbcd.eruservice.util.EnumUtil;

import lombok.Builder;
import lombok.Getter;

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
        return SourceSaveFlowParam
                .builder()
                .name(Optional.ofNullable(source).map(SourceDto::getName).orElse(ShellOption.NULL))
                .incomeSource(Optional.ofNullable(source).map(SourceDto::getIncomeSourceName).orElse(ShellOption.NULL))
                .sourceType(Optional.ofNullable(source).map(SourceDto::getSourceType).map(SourceType::name)
                        .orElse(ShellOption.NULL))
                .currencyType(Optional.ofNullable(source).map(SourceDto::getCurrencyType).map(CurrencyType::name)
                        .orElse(ShellOption.NULL))
                .balance(Optional.ofNullable(source).map(SourceDto::getBalance).map(BigDecimal::toString)
                        .orElse(ShellOption.NULL))
                .incomeSourceItems(incomeSourceItems)
                .sourceTypeItems(EnumUtil.getItems(SourceType.values()))
                .currencyTypeItems(EnumUtil.getItems(CurrencyType.values()))
                .build();
    }
}
