package com.onurbcd.eruservice.util;

import com.onurbcd.eruservice.enums.Error;
import com.onurbcd.eruservice.exception.ApiException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

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
            throw new ApiException(Error.PARSE_LOCAL_DATE, e);
        }
    }

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ISO_LOCAL_DATE) : null;
    }
}
