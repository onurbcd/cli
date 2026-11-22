package com.onurbcd.cli.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Modifier;
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

    /**
     * A collection is empty when it is {@code null}, has no elements, or every element is "empty": {@code null} or an
     * object whose instance fields are all {@code null} (e.g. the all-null DTO a left join projection returns).
     */
    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.stream().allMatch(CollectionUtil::isObjectEmpty);
    }

    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }

    private static boolean isObjectEmpty(@Nullable Object object) {
        if (object == null) {
            return true;
        }

        // JDK classes live in named modules whose private state cannot be reflected on (InaccessibleObjectException);
        // a non-null String, UUID, etc. is a value, never an empty holder.
        if (object.getClass().getModule().isNamed()) {
            return false;
        }

        try {
            for (var field : FieldUtils.getAllFields(object.getClass())) {
                // Static fields (serialVersionUID, constants, JaCoCo's $jacocoData) are not part of the instance state.
                if (!Modifier.isStatic(field.getModifiers()) &&
                        !Objects.isNull(FieldUtils.readField(field, object, true))) {
                    return false;
                }
            }
        } catch (IllegalAccessException _) {
            return true;
        }

        return true;
    }
}
