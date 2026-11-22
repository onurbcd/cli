package com.onurbcd.cli.validator;

import com.onurbcd.cli.TestClock;
import com.onurbcd.cli.TestValidator;
import com.onurbcd.cli.annotation.MaxYear;
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

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaxYearValidatorTest {

    private static final int CURRENT_YEAR = 2025;
    private static final String MESSAGE = "The year cannot be greater than {value}";

    private final MaxYearValidator validator = new MaxYearValidator(TestClock.fixedAt(LocalDate.of(CURRENT_YEAR, 7, 10)));

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private HibernateConstraintValidatorContext hibernateContext;

    @Mock
    private HibernateConstraintViolationBuilder violationBuilder;

    private record Target(@MaxYear Short year) {
    }

    @Test
    @DisplayName("null is valid and the context is not used")
    void givenNull_shouldBeValid() {
        assertThat(validator.isValid(null, context)).isTrue();

        verifyNoInteractions(context);
    }

    @ParameterizedTest(name = "[{index}] year {0}")
    @ValueSource(ints = {1900, CURRENT_YEAR - 1, CURRENT_YEAR})
    @DisplayName("a year up to the current one is valid and the context is not used")
    void givenYearUpToCurrent_shouldBeValid(int year) {
        assertThat(validator.isValid((short) year, context)).isTrue();

        verifyNoInteractions(context);
    }

    @ParameterizedTest(name = "[{index}] year {0}")
    @ValueSource(ints = {CURRENT_YEAR + 1, 3000})
    @DisplayName("a year after the current one is invalid and replaces the default violation")
    void givenFutureYear_shouldBeInvalidAndBuildCustomViolation(int year) {
        when(context.unwrap(HibernateConstraintValidatorContext.class)).thenReturn(hibernateContext);
        when(hibernateContext.buildConstraintViolationWithTemplate(MESSAGE)).thenReturn(violationBuilder);

        var result = validator.isValid((short) year, context);

        assertThat(result).isFalse();
        verify(context).unwrap(HibernateConstraintValidatorContext.class);
        verify(hibernateContext).addMessageParameter("value", CURRENT_YEAR);
        verify(hibernateContext).disableDefaultConstraintViolation();
        verify(hibernateContext).buildConstraintViolationWithTemplate(MESSAGE);
        verify(violationBuilder).addConstraintViolation();
        verifyNoMoreInteractions(context, hibernateContext, violationBuilder);
    }

    @Test
    @DisplayName("through a real validator, the violation message carries the current year")
    void givenRealValidator_shouldInterpolateTheCurrentYear() {
        var validatorFactory = TestValidator.create();

        assertThat(TestValidator.messages(validatorFactory, new Target((short) (CURRENT_YEAR + 1))))
                .isEqualTo(Set.of("The year cannot be greater than " + CURRENT_YEAR));
        assertThat(TestValidator.messages(validatorFactory, new Target((short) CURRENT_YEAR))).isEmpty();
    }
}
