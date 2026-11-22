package com.onurbcd.cli.helper;

import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.PrimeDto;
import com.onurbcd.cli.enums.CurrencyType;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.formatter.BigDecimalFormatter;
import com.onurbcd.cli.formatter.CodeableFormatter;
import com.onurbcd.cli.formatter.LocalDateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ShellHelperTest {

    private static final String MESSAGE = "message";
    private static final String ESC = "\u001B";
    private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final String PAGE_TABLE = """
            ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┳━━━━━━┳━━━━━━┓
            ┃                 Id                 ┃ Name ┃Active┃
            ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━╋━━━━━━╋━━━━━━┫
            ┃11111111-1111-1111-1111-111111111111┃Salary┃true  ┃
            ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┻━━━━━━┻━━━━━━┛

            Number Of Elements: 1
            Current Page: 3
            Total Elements: 25
            Total Pages: 25""";
    private static final String SOURCE_TABLE = """
            ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┳━━━━━━┳━━━━━━┳━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┳━━━━━━━━━━━━━━━━━━┳━━━━━━━━━━━━━━━━━━┳━━━━━━━━━━━━━┳━━━━━━━━┓
            ┃                 Id                 ┃ Name ┃Active┃          Income Source Id          ┃Income Source Name┃   Source Type    ┃Currency Type┃Balance ┃
            ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━╋━━━━━━╋━━━━━━╋━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━╋━━━━━━━━━━━━━━━━━━╋━━━━━━━━━━━━━━━━━━╋━━━━━━━━━━━━━╋━━━━━━━━┫
            ┃11111111-1111-1111-1111-111111111111┃Wallet┃true  ┃11111111-1111-1111-1111-111111111111┃Salary            ┃Dinheiro (espécie)┃Digital      ┃1,234.50┃
            ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┻━━━━━━┻━━━━━━┻━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┻━━━━━━━━━━━━━━━━━━┻━━━━━━━━━━━━━━━━━━┻━━━━━━━━━━━━━┻━━━━━━━━┛
            """;
    private static final String DAY_TABLE = """
            ┏━━━━━━━━━━━━━┳━━━━━━━━━━━━━━┓
            ┃Calendar Year┃Calendar Month┃
            ┣━━━━━━━━━━━━━╋━━━━━━━━━━━━━━┫
            ┃         2026┃            11┃
            ┣━━━━━━━━━━━━━╋━━━━━━━━━━━━━━┫
            ┃            5┃             1┃
            ┗━━━━━━━━━━━━━┻━━━━━━━━━━━━━━┛
            """;

    private ShellHelper helper;

    @BeforeEach
    void setUp() {
        var numberFormat = NumberFormat.getInstance(Locale.US);
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);

        helper = new ShellHelper(
                JsonMapper.builder().build(),
                new LocalDateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                new BigDecimalFormatter(numberFormat),
                new CodeableFormatter());
    }

    @Getter
    @AllArgsConstructor
    public static class SourceRow {
        private final UUID id;
        private final String name;
        private final Boolean active;
        private final UUID incomeSourceId;
        private final String incomeSourceName;
        private final CurrencyType sourceType;
        private final CurrencyType currencyType;
        private final BigDecimal balance;
    }

    @Getter
    @AllArgsConstructor
    public static class DayRow {
        private final Integer calendarYear;
        private final Integer calendarMonth;
    }

    private static PrimeDto dto() {
        var dto = new PrimeDto();
        dto.setId(ID);
        dto.setName("Salary");
        dto.setActive(true);
        return dto;
    }

    static Stream<Arguments> colors() {
        BiFunction<ShellHelper, String, String> error = ShellHelper::error;
        BiFunction<ShellHelper, String, String> success = ShellHelper::success;
        BiFunction<ShellHelper, String, String> warning = ShellHelper::warning;
        return Stream.of(
                Arguments.of("error", error, ESC + "[31m"),
                Arguments.of("success", success, ESC + "[32m"),
                Arguments.of("warning", warning, ESC + "[33m"));
    }

    @Nested
    @DisplayName("printJson")
    class PrintJson {

        @Test
        @DisplayName("when dto is given, should return pretty printed JSON")
        void givenDto_whenPrintJson_shouldReturnPrettyJson() {
            var result = helper.printJson(dto());

            assertThat(result).isEqualToNormalizingNewlines("""
                    {
                      "active" : true,
                      "createdDate" : null,
                      "id" : "11111111-1111-1111-1111-111111111111",
                      "lastModifiedDate" : null,
                      "name" : "Salary"
                    }""");
        }
    }

    @Nested
    @DisplayName("printTable(Page, EruTable)")
    class PrintPageTable {

        @Test
        @DisplayName("when page is given, should render the table followed by the page info")
        void givenPage_whenPrintTable_shouldRenderTableAndPageInfo() {
            var rows = List.<Dtoable>of(dto());
            var page = new PageImpl<>(rows, PageRequest.of(2, 1), 25);

            var result = helper.printTable(page, EruTable.INCOME_SOURCE);

            assertThat(result).isEqualToNormalizingNewlines(PAGE_TABLE);
        }
    }

    @Nested
    @DisplayName("printTable(Iterable, EruTable)")
    class PrintIterableTable {

        @Test
        @DisplayName("when rows are given, should format cells and align headers and numbers")
        void givenRows_whenPrintTable_shouldRenderTable() {
            var rows = List.of(new SourceRow(ID, "Wallet", true, ID, "Salary", CurrencyType.MONEY,
                    CurrencyType.DIGITAL, new BigDecimal("1234.5")));

            var result = helper.printTable(rows, EruTable.SOURCE);

            assertThat(result).isEqualToNormalizingNewlines(SOURCE_TABLE);
        }

        @Test
        @DisplayName("when cells are numbers, should align them to the right")
        void givenNumbers_whenPrintTable_shouldAlignRight() {
            var rows = List.of(new DayRow(2026, 11), new DayRow(5, 1));

            var result = helper.printTable(rows, EruTable.DAY);

            assertThat(result).isEqualToNormalizingNewlines(DAY_TABLE);
        }
    }

    @Nested
    @DisplayName("colored messages")
    class ColoredMessages {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.onurbcd.cli.helper.ShellHelperTest#colors")
        @DisplayName("should wrap the message in the ANSI color and reset codes")
        void givenMessage_whenColor_shouldWrapInAnsiCodes(String label, BiFunction<ShellHelper, String, String> color,
                                                          String prefix) {
            var result = color.apply(helper, MESSAGE);

            assertThat(result).isEqualTo(prefix + MESSAGE + ESC + "[0m");
        }
    }
}
