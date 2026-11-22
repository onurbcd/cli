package com.onurbcd.cli.formatter;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.shell.jline.tui.table.Formatter;

import java.math.BigDecimal;
import java.text.NumberFormat;

@RequiredArgsConstructor
public class BigDecimalFormatter implements Formatter {

    private final NumberFormat numberFormat;

    @Override
    public String[] format(@Nullable Object value) {
        var formattedValue = StringUtils.EMPTY;

        if (value instanceof BigDecimal bigDecimal) {
            formattedValue = numberFormat.format(bigDecimal);
        }

        return new String[]{formattedValue};
    }
}
