package com.onurbcd.cli.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.shell.standard.ShellOption;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParamUtil {

    public static <T> String getLocalDate(@Nullable T input, Function<T, LocalDate> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .map(DateUtil::formatDate)
                .orElse(ShellOption.NULL);
    }

    public static <T> String getString(@Nullable T input, Function<T, String> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .orElse(ShellOption.NULL);
    }

    @Nullable
    public static <T> String getNullString(@Nullable T input, Function<T, String> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .orElse(null);
    }

    public static <T> String getBigDecimal(@Nullable T input, Function<T, BigDecimal> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .map(BigDecimal::toString)
                .orElse(ShellOption.NULL);
    }

    public static <T> String getEnum(@Nullable T input, Function<T, ? extends Enum<?>> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .map(Enum::name)
                .orElse(ShellOption.NULL);
    }

    public static <T> List<String> getUUIDCollection(@Nullable T input, Function<T, Collection<UUID>> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .map(ids -> ids.stream().map(UUID::toString).toList())
                .orElseGet(ArrayList::new);
    }

    public static <T> Boolean getBoolean(@Nullable T input, Function<T, Boolean> fn) {
        return getBoolean(input, fn, Boolean.TRUE);
    }

    public static <T> Boolean getBoolean(@Nullable T input, Function<T, Boolean> fn, Boolean orElse) {
        return Optional.ofNullable(input)
                .map(fn)
                .orElse(orElse);
    }

    public static <T> String getShort(@Nullable T input, Function<T, Short> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .map(s -> Short.toString(s))
                .orElse(ShellOption.NULL);
    }

    public static <T> Short getNullShort(@Nullable T input, Function<T, Short> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .orElse(null);
    }

    public static Set<UUID> getUUIDSet(Collection<String> collection) {
        return collection.stream()
                .map(Converter::toUUID)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static <T> Set<UUID> getNullUUIDSet(@Nullable T input, Function<T, Set<UUID>> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .orElse(null);
    }

    public static String[] getSortProps(String[] properties, String... defaultProperties) {
        if (defaultProperties == null || defaultProperties.length == 0) {
            throw new IllegalArgumentException("Default sort properties cannot be null or empty");
        }

        if (properties != null && properties.length > 0) {
            return Arrays.stream(properties)
                    .filter(p -> p != null && !p.trim().isEmpty())
                    .toArray(String[]::new);
        }

        return defaultProperties;
    }
}
