package com.onurbcd.cli.validator;

import com.onurbcd.cli.annotation.MinYear;
import com.onurbcd.cli.config.property.AdminProperties;
import com.onurbcd.cli.helper.ContextHelper;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MinYearValidator implements ConstraintValidator<MinYear, Short> {

    private final AdminProperties config;

    public MinYearValidator() {
        this.config = ContextHelper.getBean(AdminProperties.class);
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
