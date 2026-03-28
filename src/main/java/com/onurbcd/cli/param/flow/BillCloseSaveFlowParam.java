package com.onurbcd.cli.param.flow;

import com.onurbcd.cli.enums.FlowType;
import com.onurbcd.cli.enums.PaymentType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.shell.component.flow.SelectItem;

import java.util.List;

import static com.onurbcd.cli.util.EnumUtil.getCodeableItems;
import static com.onurbcd.cli.util.FileUtil.getFiles;

@Builder
@Getter
public class BillCloseSaveFlowParam implements Paramable {

    private List<SelectItem> billItems;
    private List<SelectItem> paymentTypeItems;
    private List<SelectItem> sourceItems;
    private List<SelectItem> filesNames;

    public static BillCloseSaveFlowParam of(SaveFlowParam params) {
        return BillCloseSaveFlowParam.builder()
                .billItems(params.getBillItems())
                .paymentTypeItems(getCodeableItems(PaymentType.values()))
                .sourceItems(params.getSourceItems())
                .filesNames(getFiles(params.getFilesPath()))
                .build();
    }

    @Override
    public FlowType getType() {
        return FlowType.BILL_CLOSE;
    }
}
