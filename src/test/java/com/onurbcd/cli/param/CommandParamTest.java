package com.onurbcd.cli.param;

import com.onurbcd.cli.dto.PrimeDto;
import com.onurbcd.cli.enums.FlowType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;

class CommandParamTest {

    private static final short YEAR = 2024;
    private static final short MONTH = 3;

    @Nested
    @DisplayName("of(id)")
    class OfId {

        @Test
        void givenId_whenOf_shouldSetOnlyId() {
            var result = CommandParam.of(ID);

            var expected = CommandParam.builder().id(ID).build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("of(flowType, year, month)")
    class OfFlowTypeYearMonth {

        @Test
        void givenFlowTypeYearMonth_whenOf_shouldSetOnlyThose() {
            var result = CommandParam.of(FlowType.BILL, YEAR, MONTH);

            var expected = CommandParam.builder().flowType(FlowType.BILL).year(YEAR).month(MONTH).build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("of(flowType, id)")
    class OfFlowTypeId {

        @Test
        void givenFlowTypeAndId_whenOf_shouldSetOnlyThose() {
            var result = CommandParam.of(FlowType.BILL, ID);

            var expected = CommandParam.builder().flowType(FlowType.BILL).id(ID).build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("setDto")
    class SetDto {

        @Test
        void givenDto_whenSetDto_shouldStoreIt() {
            var param = CommandParam.of(ID);
            var dto = new PrimeDto();

            param.setDto(dto);

            assertThat(param.getDto()).isSameAs(dto);
            assertThat(param.getId()).isEqualTo(ID);
        }
    }
}
