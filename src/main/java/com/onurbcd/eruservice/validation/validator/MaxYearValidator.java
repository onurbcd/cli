package com.onurbcd.eruservice.validation.validator;

import com.onurbcd.eruservice.annotation.MaxYear;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class MaxYearValidator implements ConstraintValidator<MaxYear, Short> {

    @Override
    public boolean isValid(Short value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        var currentYear = LocalDateTime.now().getYear();
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
