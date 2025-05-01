package com.onurbcd.eruservice.exception;

import com.onurbcd.eruservice.enums.Error;
import lombok.Getter;

import java.io.Serial;
import java.util.function.Supplier;

@Getter
public class ApiException extends RuntimeException implements Supplier<ApiException> {

    @Serial
    private static final long serialVersionUID = -4588973589702747798L;

    private final Error error;

    public ApiException(Error error, String errorMessage) {
        super(errorMessage);
        this.error = error;
    }

    public ApiException(Error error, Object... args) {
        this(error, error.format(args));
    }

    public static ApiException notFound(Object... args) {
        return new ApiException(Error.ENTITY_DOES_NOT_EXIST, args);
    }

    @Override
    public ApiException get() {
        return this;
    }
}
