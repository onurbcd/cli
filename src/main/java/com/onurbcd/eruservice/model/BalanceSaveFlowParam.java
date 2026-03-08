package com.onurbcd.eruservice.model;

import com.onurbcd.eruservice.dto.balance.BalanceDto;
import com.onurbcd.eruservice.enums.BalanceType;
import com.onurbcd.eruservice.util.DateUtil;
import com.onurbcd.eruservice.util.EnumUtil;
import com.onurbcd.eruservice.util.FileUtil;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.SelectItem;
import org.springframework.shell.standard.ShellOption;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private List<SelectItem> linkedDocuments;
    private List<String> linkedDocumentsDefault;

    public static BalanceSaveFlowParam of(@Nullable BalanceDto balance, List<SelectItem> sourceItems,
                                          List<SelectItem> categoryItems, String filesPath) {

        var linkedIds = Optional.ofNullable(balance)
                .map(BalanceDto::getDocumentsIds)
                .map(ids -> ids.stream().map(UUID::toString).collect(Collectors.toSet()))
                .orElse(Collections.emptySet());

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
                .linkedDocuments(Optional.ofNullable(balance)
                        .map(BalanceDto::getDocuments)
                        .map(docs -> docs.stream()
                                .map(doc -> {
                                    var id = doc.getId().toString();
                                    return SelectItem.of(doc.getName(), id, true, linkedIds.contains(id));
                                })
                                .toList())
                        .orElse(List.of()))
                .linkedDocumentsDefault(Optional.ofNullable(balance)
                        .map(BalanceDto::getDocumentsIds)
                        .map(ids -> ids.stream().map(UUID::toString).toList())
                        .orElse(List.of()))
                .build();
    }
}
