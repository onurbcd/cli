package com.onurbcd.cli.param.flow;

import com.onurbcd.cli.enums.CurrencyType;
import com.onurbcd.cli.enums.FlowType;
import com.onurbcd.cli.enums.SourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.util.List;

import static com.onurbcd.cli.TestFactory.createSelectItems;
import static com.onurbcd.cli.TestFactory.createSourceDto;
import static com.onurbcd.cli.util.EnumUtil.getCodeableItems;
import static org.assertj.core.api.Assertions.assertThat;

class SourceSaveFlowParamTest {

    private static final List<SelectItem> INCOME_SOURCES = createSelectItems("incomeSource");

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("when source is complete, should convert its fields and keep the income sources")
        void givenFullSource_whenOf_shouldConvertEveryField() {
            var result = SourceSaveFlowParam.of(createSourceDto(), SaveFlowParam.source(INCOME_SOURCES));

            var expected = SourceSaveFlowParam.builder()
                    .name("bogus")
                    .incomeSource("income")
                    .sourceType("USABLE")
                    .currencyType("DIGITAL")
                    .balance("100.50")
                    .incomeSourceItems(INCOME_SOURCES)
                    .sourceTypeItems(getCodeableItems(SourceType.values()))
                    .currencyTypeItems(getCodeableItems(CurrencyType.values()))
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
        }

        @Test
        @DisplayName("when source is null, should leave the fields null and keep the item lists")
        void givenNullSource_whenOf_shouldLeaveFieldsNull() {
            var result = SourceSaveFlowParam.of(null, SaveFlowParam.source(INCOME_SOURCES));

            var expected = SourceSaveFlowParam.builder()
                    .incomeSourceItems(INCOME_SOURCES)
                    .sourceTypeItems(getCodeableItems(SourceType.values()))
                    .currencyTypeItems(getCodeableItems(CurrencyType.values()))
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("getType")
    class GetType {

        @Test
        @DisplayName("should be SOURCE")
        void whenGetType_shouldBeSource() {
            assertThat(SourceSaveFlowParam.builder().build().getType()).isEqualTo(FlowType.SOURCE);
        }
    }
}
