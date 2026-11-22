package com.onurbcd.cli.util;

import com.onurbcd.cli.dto.PrimeSaveDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidatorUtilTest {

    @Mock
    private Validator validator;

    @Mock
    private PrimeSaveDto dto;

    @Mock
    private ConstraintViolation<PrimeSaveDto> first;

    @Mock
    private ConstraintViolation<PrimeSaveDto> second;

    @Test
    @DisplayName("without violations, should return null")
    void givenNoViolations_whenValidate_shouldReturnNull() {
        when(validator.validate(dto)).thenReturn(Set.of());

        var result = ValidatorUtil.validate(validator, dto);

        assertThat(result).isNull();
        verify(validator).validate(dto);
        verifyNoMoreInteractions(validator, dto);
    }

    @Test
    @DisplayName("with one violation, should return its message")
    void givenOneViolation_whenValidate_shouldReturnMessage() {
        when(first.getMessage()).thenReturn("Name is required.");
        when(validator.validate(dto)).thenReturn(Set.of(first));

        var result = ValidatorUtil.validate(validator, dto);

        assertThat(result).isEqualTo("Name is required.");
        verify(validator).validate(dto);
        verify(first).getMessage();
        verifyNoMoreInteractions(validator, dto, first);
    }

    @Test
    @DisplayName("with several violations, should join the messages with a line feed")
    void givenSeveralViolations_whenValidate_shouldJoinMessages() {
        var violations = new LinkedHashSet<ConstraintViolation<PrimeSaveDto>>();
        violations.add(first);
        violations.add(second);
        when(first.getMessage()).thenReturn("first");
        when(second.getMessage()).thenReturn("second");
        when(validator.validate(dto)).thenReturn(violations);

        var result = ValidatorUtil.validate(validator, dto);

        assertThat(result).isEqualTo("first\nsecond");
        verify(validator).validate(dto);
        verify(first).getMessage();
        verify(second).getMessage();
        verifyNoMoreInteractions(validator, dto, first, second);
    }
}
