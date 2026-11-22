package com.onurbcd.cli.param.flow;

import com.onurbcd.cli.enums.FlowType;
import com.onurbcd.cli.enums.PaymentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.onurbcd.cli.TestFactory.createSelectItems;
import static com.onurbcd.cli.util.EnumUtil.getCodeableItems;
import static org.assertj.core.api.Assertions.assertThat;

class BillCloseSaveFlowParamTest {

    @TempDir
    Path filesDir;

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("should take bills and sources from the params and build payment types and files")
        void givenParams_whenOf_shouldBuildEveryItemList() throws IOException {
            var bills = createSelectItems("bill");
            var sources = createSelectItems("source");
            var file = Files.createFile(filesDir.resolve("receipt.txt"));
            var params = SaveFlowParam.billClose(bills, sources, filesDir.toString());

            var result = BillCloseSaveFlowParam.of(params);

            var expected = BillCloseSaveFlowParam.builder()
                    .billItems(bills)
                    .paymentTypeItems(getCodeableItems(PaymentType.values()))
                    .sourceItems(sources)
                    .filesNames(List.of(SelectItem.of("receipt.txt", file.toString())))
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("getType")
    class GetType {

        @Test
        @DisplayName("should be BILL_CLOSE")
        void whenGetType_shouldBeBillClose() {
            assertThat(BillCloseSaveFlowParam.builder().build().getType()).isEqualTo(FlowType.BILL_CLOSE);
        }
    }
}
