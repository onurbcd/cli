package com.onurbcd.cli.param.flow;

import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.FlowType;
import com.onurbcd.cli.enums.ReferenceType;
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

class BillOpenSaveFlowParamTest {

    @TempDir
    Path filesDir;

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("should take budgets from the params and build document types, reference types and files")
        void givenParams_whenOf_shouldBuildEveryItemList() throws IOException {
            var budgets = createSelectItems("budget");
            var file = Files.createFile(filesDir.resolve("bill.txt"));
            var params = SaveFlowParam.billOpen(budgets, filesDir.toString());

            var result = BillOpenSaveFlowParam.of(params);

            var expected = BillOpenSaveFlowParam.builder()
                    .budgetItems(budgets)
                    .documentTypeItems(getCodeableItems(DocumentType.values()))
                    .referenceTypeItems(getCodeableItems(ReferenceType.values()))
                    .filesNames(List.of(SelectItem.of("bill.txt", file.toString())))
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("getType")
    class GetType {

        @Test
        @DisplayName("should be BILL_OPEN")
        void whenGetType_shouldBeBillOpen() {
            assertThat(BillOpenSaveFlowParam.builder().build().getType()).isEqualTo(FlowType.BILL_OPEN);
        }
    }
}
