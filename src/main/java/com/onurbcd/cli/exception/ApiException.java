package com.onurbcd.cli.exception;

import com.onurbcd.cli.enums.Error;
import lombok.Getter;

import java.io.Serial;
import java.util.function.Supplier;

@Getter
public class ApiException extends RuntimeException implements Supplier<ApiException> {

    @Serial
    private static final long serialVersionUID = -4588973589702747798L;

    private final Error error;

    public ApiException(Error error, Object... args) {
        super(error.format(args));
        this.error = error;
    }

    public ApiException(Error error, Throwable cause) {
        super(error.format(cause.getMessage()), cause);
        this.error = error;
    }

    public static ApiException notFound(Object... args) {
        return new ApiException(Error.ENTITY_DOES_NOT_EXIST, args);
    }

    @Override
    public ApiException get() {
        return this;
    }
}
