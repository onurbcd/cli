package com.onurbcd.eruservice.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.SelectItem;
import org.springframework.shell.standard.ShellOption;

import com.onurbcd.eruservice.dto.balance.BalanceDto;
import com.onurbcd.eruservice.enums.BalanceType;
import com.onurbcd.eruservice.util.DateUtil;
import com.onurbcd.eruservice.util.EnumUtil;
import com.onurbcd.eruservice.util.FileUtil;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BalanceSaveFlowParam {

    private String day;
    private String source;
    private String category;
    private String amount;
    private String code;
    private String description;
    private String balanceType;
    private List<SelectItem> sourceItems;
    private List<SelectItem> categoryItems;
    private List<SelectItem> balanceTypeItems;
    private List<SelectItem> filesNames;

    public static BalanceSaveFlowParam of(@Nullable BalanceDto balance, List<SelectItem> sourceItems,
            List<SelectItem> categoryItems, String filesPath) {
        return BalanceSaveFlowParam
                .builder()
                .day(Optional.ofNullable(balance).map(BalanceDto::getDayCalendarDate).map(DateUtil::formatDate)
                        .orElse(ShellOption.NULL))
                .source(Optional.ofNullable(balance).map(BalanceDto::getSourceName).orElse(ShellOption.NULL))
                .category(Optional.ofNullable(balance).map(BalanceDto::getCategoryName).orElse(ShellOption.NULL))
                .amount(Optional.ofNullable(balance).map(BalanceDto::getAmount).map(BigDecimal::toString)
                        .orElse(ShellOption.NULL))
                .code(Optional.ofNullable(balance).map(BalanceDto::getCode).orElse(ShellOption.NULL))
                .description(Optional.ofNullable(balance).map(BalanceDto::getDescription).orElse(ShellOption.NULL))
                .balanceType(Optional.ofNullable(balance).map(BalanceDto::getBalanceType).map(BalanceType::name)
                        .orElse(ShellOption.NULL))
                .sourceItems(sourceItems)
                .categoryItems(categoryItems)
                .balanceTypeItems(EnumUtil.getItems(BalanceType.values()))
                .filesNames(FileUtil.getFiles(filesPath))
                .build();
    }
}
