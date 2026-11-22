package com.onurbcd.cli.util;

import com.onurbcd.cli.dto.document.DocumentDto;
import com.onurbcd.cli.enums.CurrencyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.onurbcd.cli.TestConstants.DOCUMENT_ID;
import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.SOURCE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParamUtilTest {

    private static final String INPUT = "input";
    private static final String[] DEFAULTS = {"name", "id"};

    static Stream<Arguments> blankOrMissingProperties() {
        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of((Object) new String[0]),
                Arguments.of((Object) new String[]{null, "", "   "}));
    }

    @Nested
    @DisplayName("nullable single value getters")
    class SingleValues {

        @Test
        @DisplayName("getLocalDate formats the date as ISO, null input or null date gives null")
        void shouldFormatLocalDate() {
            assertThat(ParamUtil.getLocalDate(INPUT, s -> LocalDate.of(2024, 3, 5))).isEqualTo("2024-03-05");
            assertThat(ParamUtil.getLocalDate(INPUT, s -> null)).isNull();
            assertThat(ParamUtil.<String>getLocalDate(null, s -> LocalDate.of(2024, 3, 5))).isNull();
        }

        @Test
        @DisplayName("getString returns the mapped text, null input or null text gives null")
        void shouldGetString() {
            assertThat(ParamUtil.getString(INPUT, String::toUpperCase)).isEqualTo("INPUT");
            assertThat(ParamUtil.getString(INPUT, s -> null)).isNull();
            assertThat(ParamUtil.<String>getString(null, String::toUpperCase)).isNull();
        }

        @Test
        @DisplayName("getNullString returns the mapped text, null input or null text gives null")
        void shouldGetNullString() {
            assertThat(ParamUtil.getNullString(INPUT, String::toUpperCase)).isEqualTo("INPUT");
            assertThat(ParamUtil.getNullString(INPUT, s -> null)).isNull();
            assertThat(ParamUtil.<String>getNullString(null, String::toUpperCase)).isNull();
        }

        @Test
        @DisplayName("getBigDecimal returns the plain text of the number, null input or null number gives null")
        void shouldGetBigDecimal() {
            assertThat(ParamUtil.getBigDecimal(INPUT, s -> new BigDecimal("12.50"))).isEqualTo("12.50");
            assertThat(ParamUtil.getBigDecimal(INPUT, s -> null)).isNull();
            assertThat(ParamUtil.<String>getBigDecimal(null, s -> BigDecimal.ONE)).isNull();
        }

        @Test
        @DisplayName("getEnum returns the constant name, null input or null enum gives null")
        void shouldGetEnum() {
            assertThat(ParamUtil.getEnum(INPUT, s -> CurrencyType.DIGITAL)).isEqualTo("DIGITAL");
            assertThat(ParamUtil.getEnum(INPUT, s -> null)).isNull();
            assertThat(ParamUtil.<String>getEnum(null, s -> CurrencyType.DIGITAL)).isNull();
        }

        @Test
        @DisplayName("getShort returns the text of the short, null input or null short gives null")
        void shouldGetShort() {
            assertThat(ParamUtil.getShort(INPUT, s -> (short) 2024)).isEqualTo("2024");
            assertThat(ParamUtil.getShort(INPUT, s -> null)).isNull();
            assertThat(ParamUtil.<String>getShort(null, s -> (short) 1)).isNull();
        }

        @Test
        @DisplayName("getNullShort returns the short itself, null input or null short gives null")
        void shouldGetNullShort() {
            assertThat(ParamUtil.getNullShort(INPUT, s -> (short) 7)).isEqualTo((short) 7);
            assertThat(ParamUtil.getNullShort(INPUT, s -> null)).isNull();
            assertThat(ParamUtil.<String>getNullShort(null, s -> (short) 7)).isNull();
        }

        @Test
        @DisplayName("getUUID returns the textual UUID, null input or null UUID gives null")
        void shouldGetUuid() {
            assertThat(ParamUtil.getUUID(INPUT, s -> ID)).isEqualTo(ID.toString());
            assertThat(ParamUtil.getUUID(INPUT, s -> null)).isNull();
            assertThat(ParamUtil.<String>getUUID(null, s -> ID)).isNull();
        }

        @Test
        @DisplayName("getNullUUID returns the UUID itself, null input or null UUID gives null")
        void shouldGetNullUuid() {
            assertThat(ParamUtil.getNullUUID(INPUT, s -> ID)).isEqualTo(ID);
            assertThat(ParamUtil.getNullUUID(INPUT, s -> null)).isNull();
            assertThat(ParamUtil.<String>getNullUUID(null, s -> ID)).isNull();
        }

        @Test
        @DisplayName("getNullUUIDSet returns the same set, null input or null set gives null")
        void shouldGetNullUuidSet() {
            var ids = Set.of(ID);

            assertThat(ParamUtil.getNullUUIDSet(INPUT, s -> ids)).isSameAs(ids);
            assertThat(ParamUtil.getNullUUIDSet(INPUT, s -> null)).isNull();
            assertThat(ParamUtil.<String>getNullUUIDSet(null, s -> ids)).isNull();
        }
    }

    @Nested
    @DisplayName("getUUIDCollection")
    class UuidCollection {

        @Test
        @DisplayName("should map each UUID to text keeping the order")
        void givenIds_shouldMapToText() {
            var ids = List.of(SOURCE_ID, ID);

            assertThat(ParamUtil.getUUIDCollection(INPUT, s -> ids)).containsExactly(SOURCE_ID.toString(),
                    ID.toString());
            assertThat(ids).containsExactly(SOURCE_ID, ID);
        }

        @Test
        @DisplayName("null input or a null collection gives a new empty list")
        void givenNothing_shouldReturnEmptyList() {
            assertThat(ParamUtil.getUUIDCollection(INPUT, s -> null)).isEmpty();
            assertThat(ParamUtil.<String>getUUIDCollection(null, s -> List.of(ID))).isEmpty();
        }
    }

    @Nested
    @DisplayName("getBoolean")
    class BooleanValues {

        @Test
        @DisplayName("returns the mapped flag")
        void givenFlag_shouldReturnIt() {
            assertThat(ParamUtil.getBoolean(INPUT, s -> false)).isFalse();
            assertThat(ParamUtil.getBoolean(INPUT, s -> true, false)).isTrue();
            assertThat(ParamUtil.getBoolean(INPUT, s -> false, true)).isFalse();
        }

        @Test
        @DisplayName("falls back to true by default, null input or null flag")
        void givenNothing_shouldDefaultToTrue() {
            assertThat(ParamUtil.<String>getBoolean(null, s -> false)).isTrue();
            assertThat(ParamUtil.getBoolean(INPUT, s -> null)).isTrue();
        }

        @Test
        @DisplayName("falls back to the given default, null input or null flag")
        void givenNothing_shouldUseGivenDefault() {
            assertThat(ParamUtil.<String>getBoolean(null, s -> true, false)).isFalse();
            assertThat(ParamUtil.getBoolean(INPUT, s -> null, false)).isFalse();
            assertThat(ParamUtil.<String>getBoolean(null, s -> false, true)).isTrue();
        }
    }

    @Nested
    @DisplayName("getUUIDSet")
    class UuidSet {

        @Test
        @DisplayName("keeps the valid UUIDs, drops invalid, blank and null ones, and removes duplicates")
        void givenMixed_shouldKeepOnlyValid() {
            var values = Arrays.asList(ID.toString(), "invalid", " ", null, ID.toString(), DOCUMENT_ID.toString());

            var result = ParamUtil.getUUIDSet(values);

            assertThat(result).containsExactlyInAnyOrder(ID, DOCUMENT_ID);
        }

        @Test
        @DisplayName("an empty collection gives an empty set")
        void givenEmpty_shouldReturnEmpty() {
            assertThat(ParamUtil.getUUIDSet(List.of())).isEmpty();
        }
    }

    @Nested
    @DisplayName("getSortProps")
    class SortProps {

        @ParameterizedTest
        @MethodSource("com.onurbcd.cli.util.ParamUtilTest#blankOrMissingProperties")
        @DisplayName("without usable properties, should return the defaults themselves")
        void givenNoUsableProperties_shouldReturnDefaults(String[] properties) {
            assertThat(ParamUtil.getSortProps(properties, DEFAULTS)).isSameAs(DEFAULTS);
        }

        @Test
        @DisplayName("should keep only the non-blank properties in order")
        void givenMixed_shouldFilter() {
            var properties = new String[]{"b", null, " ", "a", "", " c "};

            var result = ParamUtil.getSortProps(properties, DEFAULTS);

            assertThat(result).containsExactly("b", "a", " c ");
            assertThat(properties).containsExactly("b", null, " ", "a", "", " c ");
        }

        @Test
        @DisplayName("a single valid property is returned alone")
        void givenOneValid_shouldReturnIt() {
            assertThat(ParamUtil.getSortProps(new String[]{"x"}, DEFAULTS)).containsExactly("x");
        }

        @Test
        @DisplayName("without defaults it fails, even with valid properties")
        void givenNoDefaults_shouldThrow() {
            var properties = new String[]{"a"};

            assertThatThrownBy(() -> ParamUtil.getSortProps(properties, (String[]) null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Default sort properties cannot be null or empty");
            assertThatThrownBy(() -> ParamUtil.getSortProps(properties))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Default sort properties cannot be null or empty");
        }
    }

    @Nested
    @DisplayName("getDocItem")
    class DocItem {

        @Test
        @DisplayName("builds an enabled and selected item named after the document with its id as value")
        void givenDocument_shouldBuildItem() {
            var document = new DocumentDto();
            document.setId(DOCUMENT_ID);
            document.setName("invoice.pdf");

            var result = ParamUtil.getDocItem(INPUT, s -> document);

            assertThat(result.name()).isEqualTo("invoice.pdf");
            assertThat(result.item()).isEqualTo(DOCUMENT_ID.toString());
            assertThat(result.enabled()).isTrue();
            assertThat(result.selected()).isTrue();
        }

        @Test
        @DisplayName("null input or a null document gives null")
        void givenNothing_shouldReturnNull() {
            assertThat(ParamUtil.getDocItem(INPUT, s -> null)).isNull();
            assertThat(ParamUtil.<String>getDocItem(null, s -> new DocumentDto())).isNull();
        }
    }
}
