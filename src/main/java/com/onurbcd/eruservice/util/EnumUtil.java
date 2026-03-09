package com.onurbcd.eruservice.util;

import com.onurbcd.eruservice.enums.Codeable;
import com.onurbcd.eruservice.enums.Error;
import com.onurbcd.eruservice.exception.ApiException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.SelectItem;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnumUtil {

    public static <T extends Enum<T>> List<SelectItem> getItems(T[] values) {
        return Arrays
                .stream(values)
                .map(value -> SelectItem.of(value.name(), value.name()))
                .sorted(Comparator.comparing(SelectItem::name))
                .toList();
    }

    public static <T extends Enum<T> & Codeable> List<SelectItem> getCodeableItems(T[] values) {
        return Arrays
                .stream(values)
                .map(value -> SelectItem.of(value.getCode(), value.name()))
                .sorted(Comparator.comparing(SelectItem::name))
                .toList();
    }

    @Nullable
    public static <T extends Enum<T>> T valueOf(Class<T> enumClass, @Nullable String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }

        try {
            return Enum.valueOf(enumClass, name);
        } catch (IllegalArgumentException e) {
            throw new ApiException(Error.INVALID_ENUM_VALUE, e);
        }
    }
}
