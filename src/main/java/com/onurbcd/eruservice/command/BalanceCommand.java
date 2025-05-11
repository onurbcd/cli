package com.onurbcd.eruservice.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onurbcd.eruservice.config.property.AdminProperties;
import com.onurbcd.eruservice.dto.balance.BalanceDto;
import com.onurbcd.eruservice.dto.balance.BalancePatchDto;
import com.onurbcd.eruservice.dto.balance.BalanceSaveDto;
import com.onurbcd.eruservice.dto.filter.BalanceFilter;
import com.onurbcd.eruservice.enums.BalanceType;
import com.onurbcd.eruservice.enums.EruTable;
import com.onurbcd.eruservice.helper.ShellHelper;
import com.onurbcd.eruservice.model.MultipartFile;
import com.onurbcd.eruservice.service.BalanceService;
import com.onurbcd.eruservice.service.CategoryService;
import com.onurbcd.eruservice.service.SourceService;
import com.onurbcd.eruservice.util.DateUtil;
import com.onurbcd.eruservice.util.EnumUtil;
import com.onurbcd.eruservice.util.FileUtil;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static com.onurbcd.eruservice.util.Constant.DESCRIPTION;
import static com.onurbcd.eruservice.util.Constant.DOCUMENTS;
import static com.onurbcd.eruservice.util.Constant.NAME;
import static com.onurbcd.eruservice.util.Constant.NAME_LABEL;

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
        var multipartFiles = FileUtil.filesToMultipartFiles(balanceSaveDto.getFilesNames());
        var returnId = service.save(balanceSaveDto, multipartFiles.toArray(new MultipartFile[0]), id);
        return "Balance with id: '%s' saved with success.".formatted(returnId);
    }

    @ShellMethod(key = "balance-delete", value = "Delete balance by id.")
    public String delete(
            @ShellOption(value = {"id", "-i"}, help = "The balance's id.")
            @NotNull
            UUID id
    ) {
        service.delete(id);
        return "Balance with id: '%s' deleted with success.".formatted(id);
    }

    @ShellMethod(key = "balance-get", value = "Get balance by id.")
    public String get(
            @ShellOption(value = {"id", "-i"}, help = "The balance's id.")
            @NotNull
            UUID id
    ) throws JsonProcessingException {
        return shellHelper.printJson(service.getById(id));
    }

    @ShellMethod(key = "balance-get-all", value = "Get balance's list.")
    public String getAll(
            @ShellOption(value = {"pageNumber", "-n"}, help = "The page's number.", defaultValue = "1")
            @Min(1)
            Integer pageNumber,

            @ShellOption(value = {"pageSize", "-s"}, help = "The page's size.", defaultValue = "10")
            @Min(1)
            Integer pageSize,

            @ShellOption(value = {"direction", "-d"}, help = "The page's sort direction.", defaultValue = "ASC")
            Sort.Direction direction,

            @ShellOption(value = {"property", "-p"}, help = "The page's sort property.", defaultValue = "sequence")
            String property,

            @ShellOption(value = {"active", "-a"}, help = "Filter's active option.", defaultValue = ShellOption.NULL)
            Boolean active,

            @ShellOption(value = {"search", "-f"}, help = "Filter's search option.", defaultValue = ShellOption.NULL)
            String search,

            @ShellOption(value = {"sourceId", "-i"}, help = "Filter's source id.", defaultValue = ShellOption.NULL)
            UUID sourceId,

            @ShellOption(value = {"categoryId", "-c"}, help = "Filter's category id.", defaultValue = ShellOption.NULL)
            UUID categoryId,

            @ShellOption(value = {"balanceType", "-b"}, help = "Filter's balance type.", defaultValue = ShellOption.NULL)
            BalanceType balanceType,

            @ShellOption(value = {"year", "-y"}, help = "Filter's year.", defaultValue = ShellOption.NULL)
            Short year,

            @ShellOption(value = {"month", "-m"}, help = "Filter's month.", defaultValue = ShellOption.NULL)
            Short month,

            @ShellOption(value = {"day", "-g"}, help = "Filter's day.", defaultValue = ShellOption.NULL)
            Short day
    ) {
        return shellHelper.printTable(
                service.getAll(
                        PageRequest.of(pageNumber - 1, pageSize, direction, property),
                        BalanceFilter.of(active, search, sourceId, categoryId, balanceType).and(year, month, day)
                ),
                EruTable.BALANCE
        );
    }

    @ShellMethod(key = "balance-update", value = "Update balance's status by id.")
    public String update(
            @ShellOption(value = {"id", "-i"}, help = "The balance's id.")
            @NotNull
            UUID id,

            @ShellOption(value = {"active", "-a"}, help = "The balance's status.", defaultValue = "false")
            Boolean active
    ) {
        service.update(BalancePatchDto.of(active), id);
        return String.format("Balance with id: '%s' updated with success.", id);
    }

    private BalanceSaveDto runSaveFlow(@Nullable UUID id) {
        var balance = Optional.ofNullable(id).map(service::getById).orElse(null);

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
        var filesNames = FileUtil.getFiles(config.getFilesPath());

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
