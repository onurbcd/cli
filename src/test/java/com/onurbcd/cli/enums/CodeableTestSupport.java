package com.onurbcd.cli.enums;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

final class CodeableTestSupport {

    private CodeableTestSupport() {
    }

    /** Constant name to code, in declaration order. */
    static <E extends Enum<E> & Codeable> Map<String, String> codesByName(E[] values) {
        var codes = new LinkedHashMap<String, String>();
        Arrays.stream(values).forEach(value -> codes.put(value.name(), value.getCode()));
        return codes;
    }
}
