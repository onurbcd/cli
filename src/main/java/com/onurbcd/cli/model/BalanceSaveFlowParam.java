package com.onurbcd.cli.model;

import com.onurbcd.cli.dto.balance.BalanceDto;
import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.FlowType;
import com.onurbcd.cli.enums.PaymentType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.SelectItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.onurbcd.cli.util.EnumUtil.getCodeableItems;
import static com.onurbcd.cli.util.EnumUtil.getItems;
import static com.onurbcd.cli.util.FileUtil.getFiles;
import static com.onurbcd.cli.util.ParamUtil.*;

@Builder
@Getter
public class BalanceSaveFlowParam implements Paramable {

    private String day;
    private String source;
    private String category;
    private String amount;
    private String code;
    private String description;
    private String balanceType;
    private String paymentType;
    private List<SelectItem> sourceItems;
    private List<SelectItem> categoryItems;
    private List<SelectItem> balanceTypeItems;
    private List<SelectItem> paymentTypeItems;
    private List<SelectItem> filesNames;
    private List<SelectItem> linkedDocuments;
    private List<String> linkedDocumentsDefault;

    public static BalanceSaveFlowParam of(@Nullable BalanceDto balance, SaveFlowParam params) {
        var linkedIds = getUUIDCollection(balance, BalanceDto::getDocumentsIds);

        return BalanceSaveFlowParam.builder()
                .day(getLocalDate(balance, BalanceDto::getDayCalendarDate))
                .source(getString(balance, BalanceDto::getSourceName))
                .category(getString(balance, BalanceDto::getCategoryName))
                .amount(getBigDecimal(balance, BalanceDto::getAmount))
                .code(getString(balance, BalanceDto::getCode))
                .description(getString(balance, BalanceDto::getDescription))
                .balanceType(getEnum(balance, BalanceDto::getBalanceType))
                .sourceItems(params.getSourceItems())
                .categoryItems(params.getCategoryItems())
                .balanceTypeItems(getItems(BalanceType.values()))
                .paymentTypeItems(getCodeableItems(PaymentType.values()))
                .filesNames(getFiles(params.getFilesPath()))
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

    @Override
    public FlowType getType() {
        return FlowType.BALANCE;
    }
}
