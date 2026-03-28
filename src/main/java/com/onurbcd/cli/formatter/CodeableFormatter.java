package com.onurbcd.cli.formatter;

import com.onurbcd.cli.enums.Codeable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.shell.table.Formatter;
import org.springframework.stereotype.Component;

@Component
public class CodeableFormatter implements Formatter {

    @Override
    public String[] format(Object value) {
        var formattedValue = StringUtils.EMPTY;

        if (value instanceof Codeable codeable) {
            formattedValue = codeable.getCode();
        }

        return new String[]{formattedValue};
    }
}
