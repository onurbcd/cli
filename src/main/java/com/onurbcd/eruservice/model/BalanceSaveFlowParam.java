package com.onurbcd.eruservice.model;

import com.onurbcd.eruservice.dto.balance.BalanceDto;
import com.onurbcd.eruservice.enums.BalanceType;
import com.onurbcd.eruservice.util.EnumUtil;
import com.onurbcd.eruservice.util.FileUtil;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.SelectItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.onurbcd.eruservice.util.FlowParamUtil.*;

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

        var linkedIds = getUUIDCollection(balance, BalanceDto::getDocumentsIds);

        return BalanceSaveFlowParam.builder()
                .day(getLocalDate(balance, BalanceDto::getDayCalendarDate))
                .source(getString(balance, BalanceDto::getSourceName))
                .category(getString(balance, BalanceDto::getCategoryName))
                .amount(getBigDecimal(balance, BalanceDto::getAmount))
                .code(getString(balance, BalanceDto::getCode))
                .description(getString(balance, BalanceDto::getDescription))
                .balanceType(getEnum(balance, BalanceDto::getBalanceType))
                .sourceItems(sourceItems)
                .categoryItems(categoryItems)
                .balanceTypeItems(EnumUtil.getItems(BalanceType.values()))
                .filesNames(FileUtil.getFiles(filesPath))
                .linkedDocuments(getLinkedDocuments(balance, linkedIds))
                .linkedDocumentsDefault(linkedIds)
                .build();
    }

    private static List<SelectItem> getLinkedDocuments(@Nullable BalanceDto balance, List<String> linkedIds) {
        return Optional.ofNullable(balance)
                .map(BalanceDto::getDocuments)
                .map(docs -> docs.stream()
                        .map(doc -> {
                            var id = doc.getId().toString();
                            return SelectItem.of(doc.getName(), id, true, linkedIds.contains(id));
                        })
                        .toList())
                .orElseGet(ArrayList::new);
    }
}
