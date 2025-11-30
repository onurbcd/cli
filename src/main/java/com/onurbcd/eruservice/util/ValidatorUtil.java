package com.onurbcd.eruservice.util;

import java.util.stream.Collectors;

import org.springframework.lang.Nullable;

import com.onurbcd.eruservice.dto.PrimeSaveDto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidatorUtil {

    @Nullable
    public static <T extends PrimeSaveDto> String validate(T dto) {
        var violations = Validation
                .buildDefaultValidatorFactory()
                .getValidator()
                .validate(dto);

        if (!violations.isEmpty()) {
            return violations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("\n"));
        }

        return null;
    }
}
