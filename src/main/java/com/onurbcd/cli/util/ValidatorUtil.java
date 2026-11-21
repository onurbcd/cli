package com.onurbcd.cli.util;

import com.onurbcd.cli.dto.PrimeSaveDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidatorUtil {

    @Nullable
    public static <T extends PrimeSaveDto> String validate(Validator validator, T dto) {
        var violations = validator.validate(dto);

        if (!violations.isEmpty()) {
            return violations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("\n"));
        }

        return null;
    }
}
