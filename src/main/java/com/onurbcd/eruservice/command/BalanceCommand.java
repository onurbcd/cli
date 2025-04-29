package com.onurbcd.eruservice.command;

import com.onurbcd.eruservice.model.MultipartFile;
import com.onurbcd.eruservice.command.helper.ShellHelper;
import com.onurbcd.eruservice.dto.balance.BalanceDto;
import com.onurbcd.eruservice.dto.balance.BalanceSaveDto;
import com.onurbcd.eruservice.dto.enums.BalanceType;
import com.onurbcd.eruservice.property.AdminProperties;
import com.onurbcd.eruservice.service.BalanceService;
import com.onurbcd.eruservice.service.SourceService;
import com.onurbcd.eruservice.service.impl.CategoryService;
import com.onurbcd.eruservice.util.DateUtil;
import com.onurbcd.eruservice.util.EnumUtil;
import com.onurbcd.eruservice.util.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static com.onurbcd.eruservice.command.CommandConstants.DESCRIPTION;
import static com.onurbcd.eruservice.command.CommandConstants.DOCUMENTS;
import static com.onurbcd.eruservice.command.CommandConstants.NAME;
import static com.onurbcd.eruservice.command.CommandConstants.NAME_LABEL;

@ShellComponent
@ShellCommandGroup("Balance")
@RequiredArgsConstructor
public class BalanceCommand {

    private final BalanceService service;
    private final SourceService sourceService;
    private final CategoryService categoryService;
    private final ComponentFlow.Builder flowBuilder;
    private final ShellHelper shellHelper;
    private final AdminProperties config;

    @ShellMethod(key = "balance-save", value = "Create or update a balance.")
    public String save(
            @ShellOption(value = {"id", "-i"}, help = "The balance's id.", defaultValue = ShellOption.NULL)
            UUID id
    ) {
        var balanceSaveDto = runSaveFlow(id);
        var multipartFiles = FileUtils.filesToMultipartFiles(balanceSaveDto.getFilesNames());
        var returnId = service.save(balanceSaveDto, multipartFiles.toArray(new MultipartFile[0]), id);
        return "Balance with id: '%s' saved with success.".formatted(returnId);
    }

    private BalanceSaveDto runSaveFlow(@Nullable UUID id) {
        var balance = Optional.ofNullable(id).map(i -> (BalanceDto) service.getById(i)).orElse(null);

        var name = Optional.ofNullable(balance).map(BalanceDto::getName).orElse(null);
        var day = Optional.ofNullable(balance).map(BalanceDto::getDayCalendarDate).map(DateUtil::formatDate).orElse(null);
        var source = Optional.ofNullable(balance).map(BalanceDto::getSourceName).orElse(null);
        var category = Optional.ofNullable(balance).map(BalanceDto::getCategoryName).orElse(null);
        var amount = Optional.ofNullable(balance).map(BalanceDto::getAmount).map(BigDecimal::toString).orElse(null);
        var code = Optional.ofNullable(balance).map(BalanceDto::getCode).orElse(null);
        var description = Optional.ofNullable(balance).map(BalanceDto::getDescription).orElse(null);
        var balanceType = Optional.ofNullable(balance).map(BalanceDto::getBalanceType).map(BalanceType::name).orElse(null);

        var sourceItems = sourceService.getItems(null);
        var categoryItems = categoryService.getItems(null);
        var balanceTypeItems = EnumUtil.getItems(BalanceType.values());
        var filesNames = FileUtils.getFiles(config.getFilesPath());

        var result = flowBuilder.clone().reset()
                .withStringInput(NAME).name(NAME_LABEL).defaultValue(name).and()
                .withStringInput("day").name("* Day (yyyy-MM-dd):").defaultValue(day).and()
                .withSingleItemSelector("source").name("* Source:").selectItems(sourceItems).defaultSelect(source).max(sourceItems.size()).and()
                .withSingleItemSelector("category").name("* Category:").selectItems(categoryItems).defaultSelect(category).max(categoryItems.size()).and()
                .withStringInput("amount").name("* Amount:").defaultValue(amount).and()
                .withStringInput("code").name("* Code:").defaultValue(code).and()
                .withStringInput(DESCRIPTION).name("Description:").defaultValue(description).and()
                .withSingleItemSelector("balanceType").name("* Balance Type").selectItems(balanceTypeItems).defaultSelect(balanceType).max(balanceTypeItems.size()).and()
                .withMultiItemSelector(DOCUMENTS).name("Documents:").selectItems(filesNames).max(filesNames.size()).and()
                .build().run().getContext();

        return BalanceSaveDto.of(
                result.get(NAME, String.class),
                Optional.ofNullable(balance).map(BalanceDto::isActive).orElse(Boolean.TRUE),
                Optional.ofNullable(balance).map(BalanceDto::getSequence).orElse(null),
                result.get("day", String.class),
                result.get("source", String.class),
                result.get("category", String.class),
                result.get("amount", String.class),
                result.get("code", String.class),
                result.get(DESCRIPTION, String.class),
                result.get("balanceType", String.class),
                Optional.ofNullable(balance).map(BalanceDto::getDocumentsIds).orElse(null),
                result.get(DOCUMENTS)
        );
    }
}
