package com.onurbcd.cli.param.flow;

import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.FlowType;
import com.onurbcd.cli.enums.ReferenceType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.shell.component.flow.SelectItem;

import java.util.List;

import static com.onurbcd.cli.util.EnumUtil.getCodeableItems;
import static com.onurbcd.cli.util.FileUtil.getFiles;

@Builder
@Getter
public class BillOpenSaveFlowParam implements Paramable {

    private List<SelectItem> budgetItems;
    private List<SelectItem> documentTypeItems;
    private List<SelectItem> referenceTypeItems;
    private List<SelectItem> filesNames;

    public static BillOpenSaveFlowParam of(SaveFlowParam params) {
        return BillOpenSaveFlowParam.builder()
                .budgetItems(params.getBudgetItems())
                .documentTypeItems(getCodeableItems(DocumentType.values()))
                .referenceTypeItems(getCodeableItems(ReferenceType.values()))
                .filesNames(getFiles(params.getFilesPath()))
                .build();
    }

    @Override
    public FlowType getType() {
        return FlowType.BILL_OPEN;
    }
}
