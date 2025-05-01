package com.onurbcd.eruservice.validation.validator;

import com.onurbcd.eruservice.validation.constraint.MinYear;
import com.onurbcd.eruservice.config.property.AdminProperties;
import com.onurbcd.eruservice.util.SpringContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MinYearValidator implements ConstraintValidator<MinYear, Short> {

    private final AdminProperties config;

    public MinYearValidator() {
        this.config = SpringContext.getBean(AdminProperties.class);
    }

    @Override
    public boolean isValid(Short value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        var result = value >= config.getMinYear();

        if (!result) {
            var hibernateContext = context.unwrap(HibernateConstraintValidatorContext.class);
            hibernateContext.addMessageParameter("value", config.getMinYear());
            hibernateContext.disableDefaultConstraintViolation();
            hibernateContext.buildConstraintViolationWithTemplate("The year cannot be less than {value}")
                    .addConstraintViolation();
        }

        return result;
    }
}
