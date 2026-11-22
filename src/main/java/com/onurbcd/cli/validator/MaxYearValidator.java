package com.onurbcd.cli.validator;

import com.onurbcd.cli.annotation.MaxYear;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.time.Clock;
import java.time.LocalDate;

public class MaxYearValidator implements ConstraintValidator<MaxYear, Short> {

    private final Clock clock;

    public MaxYearValidator(Clock clock) {
        this.clock = clock;
    }

    @Override
    public boolean isValid(Short value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        var currentYear = LocalDate.now(clock).getYear();
        var result = value <= currentYear;

        if (!result) {
            var hibernateContext = context.unwrap(HibernateConstraintValidatorContext.class);
            hibernateContext.addMessageParameter("value", currentYear);
            hibernateContext.disableDefaultConstraintViolation();
            hibernateContext.buildConstraintViolationWithTemplate("The year cannot be greater than {value}")
                    .addConstraintViolation();
        }

        return result;
    }
}
