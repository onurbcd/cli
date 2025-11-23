package com.onurbcd.eruservice.command;

import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.onurbcd.eruservice.config.property.AdminProperties;
import com.onurbcd.eruservice.dto.bill.BillOpenDto;
import com.onurbcd.eruservice.enums.DocumentType;
import com.onurbcd.eruservice.enums.ReferenceType;
import com.onurbcd.eruservice.helper.ShellHelper;
import com.onurbcd.eruservice.service.BillService;
import com.onurbcd.eruservice.service.BudgetService;
import com.onurbcd.eruservice.util.EnumUtil;
import com.onurbcd.eruservice.util.FileUtil;

import lombok.RequiredArgsConstructor;

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
        var documentTypeItems = EnumUtil.getCodeableItems(DocumentType.values());
        var budgetItems = budgetService.getMonthlyBudget(refYear, refMonth);
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
