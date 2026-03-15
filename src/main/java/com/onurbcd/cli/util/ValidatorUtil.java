package com.onurbcd.cli.util;

import com.onurbcd.cli.dto.PrimeSaveDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidatorUtil {

    @Nullable
    public static <T extends PrimeSaveDto> String validate(T dto) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var violations = factory
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
}
