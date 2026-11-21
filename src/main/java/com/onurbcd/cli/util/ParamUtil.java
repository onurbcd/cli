package com.onurbcd.cli.util;

import com.onurbcd.cli.dto.document.DocumentDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParamUtil {

    @Nullable
    public static <T> String getLocalDate(@Nullable T input, Function<T, LocalDate> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .map(DateUtil::formatDate)
                .orElse(null);
    }

    @Nullable
    public static <T> String getString(@Nullable T input, Function<T, String> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .orElse(null);
    }

    @Nullable
    public static <T> String getNullString(@Nullable T input, Function<T, String> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .orElse(null);
    }

    @Nullable
    public static <T> String getBigDecimal(@Nullable T input, Function<T, BigDecimal> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .map(BigDecimal::toString)
                .orElse(null);
    }

    @Nullable
    public static <T> String getEnum(@Nullable T input, Function<T, ? extends Enum<?>> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .map(Enum::name)
                .orElse(null);
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

    @Nullable
    public static <T> String getShort(@Nullable T input, Function<T, Short> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .map(s -> Short.toString(s))
                .orElse(null);
    }

    @Nullable
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

    @Nullable
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
            var filtered = Arrays.stream(properties)
                    .filter(p -> p != null && !p.trim().isEmpty())
                    .toArray(String[]::new);

            if (filtered.length > 0) {
                return filtered;
            }
        }

        return defaultProperties;
    }

    @Nullable
    public static <T> String getUUID(@Nullable T input, Function<T, UUID> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .map(UUID::toString)
                .orElse(null);
    }

    public static <T> UUID getNullUUID(@Nullable T input, Function<T, UUID> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .orElse(null);
    }

    @Nullable
    public static <T> SelectItem getDocItem(@Nullable T input, Function<T, DocumentDto> fn) {
        return Optional.ofNullable(input)
                .map(fn)
                .map(doc -> SelectItem.of(doc.getName(), doc.getId().toString(), true, true))
                .orElse(null);
    }
}
