package com.onurbcd.cli.formatter;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.shell.jline.tui.table.Formatter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
public class LocalDateTimeFormatter implements Formatter {

    private final DateTimeFormatter dateTimeFormatter;

    @Override
    public String[] format(@Nullable Object value) {
        var formattedValue = StringUtils.EMPTY;

        if (value instanceof LocalDateTime localDateTime) {
            formattedValue = localDateTime.format(dateTimeFormatter);
        }

        return new String[]{formattedValue};
    }
}
