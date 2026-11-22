package com.onurbcd.cli.param.flow;

import com.onurbcd.cli.enums.FlowType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.onurbcd.cli.TestFactory.createIncomeSourceDto;
import static org.assertj.core.api.Assertions.assertThat;

class IncomeSourceSaveFlowParamTest {

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("when income source is present, should convert its name")
        void givenIncomeSource_whenOf_shouldConvertName() {
            var result = IncomeSourceSaveFlowParam.of(createIncomeSourceDto());

            assertThat(result.getName()).isEqualTo("bogus");
        }

        @Test
        @DisplayName("when income source is null, should leave the name null")
        void givenNullIncomeSource_whenOf_shouldLeaveNameNull() {
            var result = IncomeSourceSaveFlowParam.of(null);

            assertThat(result.getName()).isNull();
        }
    }

    @Nested
    @DisplayName("getType")
    class GetType {

        @Test
        @DisplayName("should be INCOME_SOURCE")
        void whenGetType_shouldBeIncomeSource() {
            assertThat(IncomeSourceSaveFlowParam.builder().build().getType()).isEqualTo(FlowType.INCOME_SOURCE);
        }
    }
}
