package com.onurbcd.cli.param.flow;

import com.onurbcd.cli.dto.billtype.BillTypeDto;
import com.onurbcd.cli.enums.FlowType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.util.List;

import static com.onurbcd.cli.TestFactory.createBillTypeDto;
import static com.onurbcd.cli.TestFactory.createSelectItems;
import static org.assertj.core.api.Assertions.assertThat;

class BillTypeSaveFlowParamTest {

    private static final List<SelectItem> CATEGORIES = createSelectItems("category");

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("when bill type is complete, should convert its fields and keep the categories")
        void givenFullBillType_whenOf_shouldConvertEveryField() {
            var result = BillTypeSaveFlowParam.of(createBillTypeDto(), SaveFlowParam.billType(CATEGORIES));

            var expected = BillTypeSaveFlowParam.builder()
                    .name("bogus")
                    .path("bills/water")
                    .categoryItems(CATEGORIES)
                    .category("category")
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
        }

        @Test
        @DisplayName("when bill type is null, should leave the fields null and keep the categories")
        void givenNullBillType_whenOf_shouldLeaveFieldsNull() {
            var result = BillTypeSaveFlowParam.of(null, SaveFlowParam.billType(CATEGORIES));

            var expected = BillTypeSaveFlowParam.builder().categoryItems(CATEGORIES).build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
        }

        @Test
        @DisplayName("when bill type has no category, should leave the category null")
        void givenBillTypeWithoutCategory_whenOf_shouldLeaveCategoryNull() {
            var result = BillTypeSaveFlowParam.of(new BillTypeDto(), SaveFlowParam.billType(CATEGORIES));

            assertThat(result.getCategory()).isNull();
        }
    }

    @Nested
    @DisplayName("getType")
    class GetType {

        @Test
        @DisplayName("should be BILL_TYPE")
        void whenGetType_shouldBeBillType() {
            assertThat(BillTypeSaveFlowParam.builder().build().getType()).isEqualTo(FlowType.BILL_TYPE);
        }
    }
}
