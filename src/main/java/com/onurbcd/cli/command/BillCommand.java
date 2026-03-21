package com.onurbcd.cli.command;

import com.onurbcd.cli.config.property.AdminProperties;
import com.onurbcd.cli.dto.bill.BillOpenDto;
import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.Error;
import com.onurbcd.cli.enums.ReferenceType;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.service.BillService;
import com.onurbcd.cli.service.BudgetService;
import com.onurbcd.cli.util.DateUtil;
import com.onurbcd.cli.util.EnumUtil;
import com.onurbcd.cli.util.FileUtil;
import com.onurbcd.cli.validator.Action;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@ShellCommandGroup("Bill")
@RequiredArgsConstructor
public class BillCommand {

    private final BillService service;
    private final BudgetService budgetService;
    private final ComponentFlow.Builder flowBuilder;
    private final ShellHelper shellHelper;
    private final AdminProperties config;

    @ShellMethod(key = "bill-open", value = "Open a bill.")
    public String openBill(
            @ShellOption(value = {"year", "-y"}, help = "The reference year.", defaultValue = ShellOption.NULL)
            Short year,

            @ShellOption(value = {"month", "-m"}, help = "The reference month.", defaultValue = ShellOption.NULL)
            Short month
    ) {
        var billOpenDto = runOpenBillFlow(year, month);
        // var multipartFiles = FileUtil.filesToMultipartFiles(billOpenDto.getFilesNames());
        // var returnId = service.openBill(billOpenDto, multipartFiles.toArray(new MultipartFile[0]));
        // return "Bill with id: '%s' opened with success.".formatted(returnId);
        return "Bill opened with success.";
    }

    private BillOpenDto runOpenBillFlow(@Nullable Short refYear, @Nullable Short refMonth) {
        var year = DateUtil.orCurrentYear(refYear);
        var month = DateUtil.orCurrentMonth(refMonth);
        var budgetItems = budgetService.getMonthlyBudget(year, month);
        Action.checkIfNotEmpty(budgetItems).orElseThrow(Error.BUDGET_REQUIRED, month, year);
        var documentTypeItems = EnumUtil.getCodeableItems(DocumentType.values());
        var referenceTypeItems = EnumUtil.getCodeableItems(ReferenceType.values());
        var filesNames = FileUtil.getFiles(config.getFilesPath());

        var context = flowBuilder.clone().reset()
                .withStringInput("referenceDay").name("* Reference Day (yyyy-MM-dd):").and()
                .withStringInput("documentDate").name("Document Date (yyyy-MM-dd):").and()
                .withStringInput("dueDate").name("* Due Date (yyyy-MM-dd):").and()
                .withStringInput("observation").name("Observation:").and()
                .withStringInput("installment").name("Installment:").and()
                .withSingleItemSelector("documentType").name("* Document Type").selectItems(documentTypeItems).max(documentTypeItems.size()).and()
                .withSingleItemSelector("budget").name("* Budget:").selectItems(budgetItems).max(budgetItems.size()).and()
                .withSingleItemSelector("referenceType").name("* Reference Type").selectItems(referenceTypeItems).max(referenceTypeItems.size()).and()
                .withSingleItemSelector("document").name("* Document").selectItems(filesNames).max(filesNames.size()).and()
                .build().run().getContext();

        var billOpenDto = BillOpenDto.of(
                context.get("referenceDay", String.class),
                context.get("documentDate", String.class),
                context.get("dueDate", String.class),
                context.get("observation", String.class),
                context.get("installment", String.class),
                context.get("documentType", String.class),
                context.get("budget", String.class),
                context.get("referenceType", String.class),
                context.get("document", String.class)
        );

        return billOpenDto;
    }
}
