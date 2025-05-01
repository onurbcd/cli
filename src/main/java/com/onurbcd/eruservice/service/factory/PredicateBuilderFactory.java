package com.onurbcd.eruservice.service.factory;

import com.onurbcd.eruservice.persistency.predicate.BasePredicateBuilder;
import com.onurbcd.eruservice.enums.Error;
import com.onurbcd.eruservice.exception.ApiException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.InvocationTargetException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PredicateBuilderFactory {

    public static <P extends BasePredicateBuilder> BasePredicateBuilder init(Class<P> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException |
                 InstantiationException | InvocationTargetException | ExceptionInInitializerError e) {

            throw new ApiException(Error.INTERNAL_SERVER_ERROR, e.toString());
        }
    }
}
