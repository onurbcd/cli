package com.onurbcd.cli.util;

import com.onurbcd.cli.enums.Codeable;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class EnumUtilTest {

    private enum Sample implements Codeable {
        ZULU("c-code"),
        ALPHA("z-code"),
        MIKE("a-code");

        private final String code;

        Sample(String code) {
            this.code = code;
        }

        @Override
        public String getCode() {
            return code;
        }
    }

    private enum Nothing {
    }

    @Nested
    @DisplayName("getItems")
    class GetItems {

        @Test
        @DisplayName("should map each constant to its name and sort by name")
        void givenValues_shouldSortByName() {
            var result = EnumUtil.getItems(new Sample[]{Sample.ZULU, Sample.ALPHA, Sample.MIKE});

            assertThat(result)
                    .extracting(SelectItem::name, SelectItem::item)
                    .containsExactly(tuple("ALPHA", "ALPHA"), tuple("MIKE", "MIKE"), tuple("ZULU", "ZULU"));
        }

        @Test
        @DisplayName("should return an empty list for no values")
        void givenNoValues_shouldReturnEmpty() {
            assertThat(EnumUtil.getItems(Nothing.values())).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCodeableItems")
    class GetCodeableItems {

        @Test
        @DisplayName("should map each constant to its code and sort by the item name")
        void givenValues_shouldSortByItemName() {
            var result = EnumUtil.getCodeableItems(Sample.values());

            assertThat(result)
                    .extracting(SelectItem::name, SelectItem::item)
                    .containsExactly(tuple("a-code", "MIKE"), tuple("c-code", "ZULU"), tuple("z-code", "ALPHA"));
        }
    }

    @Nested
    @DisplayName("valueOf")
    class ValueOf {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t"})
        @DisplayName("blank names return null")
        void givenBlank_shouldReturnNull(String name) {
            assertThat(EnumUtil.valueOf(Sample.class, name)).isNull();
        }

        @Test
        @DisplayName("a known name returns the constant")
        void givenKnownName_shouldReturnConstant() {
            assertThat(EnumUtil.valueOf(Sample.class, "MIKE")).isSameAs(Sample.MIKE);
        }

        @ParameterizedTest
        @ValueSource(strings = {"UNKNOWN", "mike"})
        @DisplayName("an unknown or differently-cased name fails with INVALID_ENUM_VALUE")
        void givenUnknownName_shouldThrow(String name) {
            assertThatThrownBy(() -> EnumUtil.valueOf(Sample.class, name))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.INVALID_ENUM_VALUE);
                        assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
                    });
        }
    }
}
