package com.onurbcd.cli.validator;

import com.onurbcd.cli.TestValidator;
import com.onurbcd.cli.annotation.MinYear;
import com.onurbcd.cli.config.property.AdminProperties;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintViolationBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinYearValidatorTest {

    private static final short MIN_YEAR = 2000;
    private static final String MESSAGE = "The year cannot be less than {value}";

    private final MinYearValidator validator = new MinYearValidator(properties());

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private HibernateConstraintValidatorContext hibernateContext;

    @Mock
    private HibernateConstraintViolationBuilder violationBuilder;

    private record Target(@MinYear Short year) {
    }

    private static AdminProperties properties() {
        var properties = new AdminProperties();
        properties.setMinYear(MIN_YEAR);
        return properties;
    }

    @Test
    @DisplayName("null is valid and the context is not used")
    void givenNull_shouldBeValid() {
        assertThat(validator.isValid(null, context)).isTrue();

        verifyNoInteractions(context);
    }

    @ParameterizedTest(name = "[{index}] year {0}")
    @ValueSource(ints = {MIN_YEAR, MIN_YEAR + 1, 3000})
    @DisplayName("a year from the configured minimum on is valid and the context is not used")
    void givenYearFromMinimum_shouldBeValid(int year) {
        assertThat(validator.isValid((short) year, context)).isTrue();

        verifyNoInteractions(context);
    }

    @ParameterizedTest(name = "[{index}] year {0}")
    @ValueSource(ints = {MIN_YEAR - 1, 1})
    @DisplayName("a year before the configured minimum is invalid and replaces the default violation")
    void givenYearBeforeMinimum_shouldBeInvalidAndBuildCustomViolation(int year) {
        when(context.unwrap(HibernateConstraintValidatorContext.class)).thenReturn(hibernateContext);
        when(hibernateContext.buildConstraintViolationWithTemplate(MESSAGE)).thenReturn(violationBuilder);

        var result = validator.isValid((short) year, context);

        assertThat(result).isFalse();
        verify(context).unwrap(HibernateConstraintValidatorContext.class);
        verify(hibernateContext).addMessageParameter("value", MIN_YEAR);
        verify(hibernateContext).disableDefaultConstraintViolation();
        verify(hibernateContext).buildConstraintViolationWithTemplate(MESSAGE);
        verify(violationBuilder).addConstraintViolation();
        verifyNoMoreInteractions(context, hibernateContext, violationBuilder);
    }

    @Test
    @DisplayName("through a real validator, the violation message carries the configured minimum")
    void givenRealValidator_shouldInterpolateTheMinimumYear() {
        var validatorFactory = TestValidator.create();

        assertThat(TestValidator.messages(validatorFactory, new Target((short) (MIN_YEAR - 1))))
                .isEqualTo(Set.of("The year cannot be less than " + MIN_YEAR));
        assertThat(TestValidator.messages(validatorFactory, new Target(MIN_YEAR))).isEmpty();
    }
}
