package com.onurbcd.cli.formatter;

import com.onurbcd.cli.enums.Codeable;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.shell.jline.tui.table.Formatter;
import org.springframework.stereotype.Component;

@Component
public class CodeableFormatter implements Formatter {

    @Override
    public String[] format(@Nullable Object value) {
        var formattedValue = StringUtils.EMPTY;

        if (value instanceof Codeable codeable) {
            formattedValue = codeable.getCode();
        }

        return new String[]{formattedValue};
    }
}
