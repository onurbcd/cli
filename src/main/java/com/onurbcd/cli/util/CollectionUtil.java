package com.onurbcd.cli.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CollectionUtil {

    @Nullable
    public static <T, R> R getValue(Collection<T> collection, Predicate<? super T> predicate,
                                    Function<? super T, ? extends R> func) {

        return collection.stream().filter(predicate).findFirst().map(func).orElse(null);
    }

    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty() || collection.stream().allMatch(Objects::isNull) ||
                collection.stream().allMatch(isObjectEmpty());
    }

    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }

    private static <T> Predicate<T> isObjectEmpty() {
        return object -> {
            try {
                for (var field : FieldUtils.getAllFields(object.getClass())) {
                    if (!Objects.isNull(FieldUtils.readField(field, object, true))) {
                        return false;
                    }
                }
            } catch (IllegalAccessException e) {
                return true;
            }

            return true;
        };
    }
}
