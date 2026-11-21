package com.onurbcd.cli.util;

import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.param.YearAndMonth;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateUtil {

    public static boolean equalMonth(@Nullable Short refYear, @Nullable Short refMonth, @Nullable Short oRefYear,
                                     @Nullable Short oRefMonth) {

        if (refYear == null || refMonth == null || oRefYear == null || oRefMonth == null) {
            return false;
        }

        return refYear.equals(oRefYear) && refMonth.equals(oRefMonth);
    }

    public static boolean equalDay(@Nullable Short year, @Nullable Short month, @Nullable Short day,
                                   @Nullable Short oYear, @Nullable Short oMonth, @Nullable Short oDay) {

        if (year == null || month == null || day == null || oYear == null || oMonth == null || oDay == null) {
            return false;
        }

        return year.equals(oYear) && month.equals(oMonth) && day.equals(oDay);
    }

    public static LocalDate parseLocalDate(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        try {
            return LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new ApiException(ErrorCode.PARSE_LOCAL_DATE, e);
        }
    }

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
    }

    /**
     * Fills in the missing year and/or month with the current ones, reading the clock only once so both values
     * always come from the same instant (e.g. never year 2024 with month 1 across New Year).
     */
    public static YearAndMonth orCurrentYearAndMonth(@Nullable Short year, @Nullable Short month, Clock clock) {
        if (year != null && month != null) {
            return new YearAndMonth(year, month);
        }

        var today = LocalDate.now(clock);

        return new YearAndMonth(
                year == null ? Short.valueOf((short) today.getYear()) : year,
                month == null ? Short.valueOf((short) today.getMonthValue()) : month);
    }
}
