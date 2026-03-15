package com.onurbcd.cli.validator;

import com.onurbcd.cli.enums.Error;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.util.CollectionUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

public class Action {

    private final boolean checkConditionNotTrue;

    private Action(boolean checkConditionNotTrue) {
        this.checkConditionNotTrue = checkConditionNotTrue;
    }

    public void orElseThrow(Error error, Object... args) {
        if (checkConditionNotTrue) {
            throw new ApiException(error, args);
        }
    }

    public void orElseThrowNotFound(Object... args) {
        if (checkConditionNotTrue) {
            throw ApiException.notFound(args);
        }
    }

    public static Action checkIf(boolean checkCondition) {
        return new Action(!checkCondition);
    }

    public static Action checkIfNot(boolean checkCondition) {
        return new Action(checkCondition);
    }

    public static <T> Action checkIfNotNull(T value) {
        return new Action(value == null);
    }

    public static <T> Action checkIfNotEmpty(Collection<T> collection) {
        return new Action(CollectionUtil.isEmpty(collection));
    }

    public static <T> Action checkIfNotEmpty(T[] array) {
        return new Action(array == null || array.length == 0);
    }

    public static Action checkIfNotBlank(String value) {
        return new Action(StringUtils.isBlank(value));
    }
}
