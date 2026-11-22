package com.onurbcd.cli.dto;

import com.onurbcd.cli.TestValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class PrimeSaveDtoTest {

    private static final String REQUIRED = "Name is required.";
    private static final String SIZE = "Name must be between 3 and 50 characters.";

    private static Validator validator;

    @SuperBuilder
    private static final class TestSaveDto extends PrimeSaveDto {
    }

    @BeforeAll
    static void setUpValidator() {
        validator = TestValidator.create();
    }

    private static Set<String> messages(String name) {
        return validator.validate(TestSaveDto.builder().name(name).build()).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    @Test
    @DisplayName("the all-args constructor stores name and active")
    void givenNameAndActive_whenConstructed_shouldExposeThem() {
        var dto = new PrimeSaveDto("Prime", true) { };

        assertThat(dto.getName()).isEqualTo("Prime");
        assertThat(dto.isActive()).isTrue();
        assertThat(dto).isInstanceOf(Dtoable.class);
    }

    @Test
    @DisplayName("the setter replaces the name")
    void givenName_whenSetName_shouldReturnNewName() {
        var dto = TestSaveDto.builder().name("Old").build();

        dto.setName("New");

        assertThat(dto.getName()).isEqualTo("New");
    }

    @Nested
    @DisplayName("isActive")
    class IsActive {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @NullSource
        @DisplayName("returns the flag given to the builder")
        void givenBuilderActive_whenIsActive_shouldReturnSameValue(Boolean active) {
            assertThat(TestSaveDto.builder().active(active).build().isActive()).isEqualTo(active);
        }

        @Test
        @DisplayName("reflects the value given to the setter")
        void givenSetter_whenIsActive_shouldReturnNewValue() {
            var dto = TestSaveDto.builder().active(true).build();

            dto.setActive(false);

            assertThat(dto.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("name validation")
    class NameValidation {

        @ParameterizedTest
        @ValueSource(strings = {"abc", "Valid name", "12345678901234567890123456789012345678901234567890"})
        @DisplayName("accepts names between 3 and 50 characters")
        void givenValidName_shouldHaveNoViolations(String name) {
            assertThat(messages(name)).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("rejects null, empty and blank names as required")
        void givenMissingName_shouldReportRequired(String name) {
            assertThat(messages(name)).contains(REQUIRED);
        }

        @ParameterizedTest
        @ValueSource(strings = {"ab", "123456789012345678901234567890123456789012345678901"})
        @DisplayName("rejects names shorter than 3 or longer than 50 characters")
        void givenOutOfRangeName_shouldReportSize(String name) {
            assertThat(messages(name)).containsExactly(SIZE);
        }
    }
}
