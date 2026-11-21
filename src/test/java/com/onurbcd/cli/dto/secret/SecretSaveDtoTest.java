package com.onurbcd.cli.dto.secret;

import com.onurbcd.cli.TestValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.shell.jline.tui.component.context.ComponentContext;

import java.util.Set;
import java.util.stream.Collectors;

import static com.onurbcd.cli.TestFactory.createSecretDto;
import static org.assertj.core.api.Assertions.assertThat;

class SecretSaveDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = TestValidator.create();
    }

    private static ComponentContext<?> completeContext() {
        var context = ComponentContext.empty();
        context.put("name", "  Bank   Login ");
        context.put("description", "  Main   account ");
        context.put("link", " https://example.com/login ");
        context.put("username", "  john   doe ");
        context.put("password", "  p4ss   word ");
        return context;
    }

    private static SecretSaveDto validDto() {
        return SecretSaveDto.builder()
                .name("Bank Login")
                .active(true)
                .description("Main account")
                .link("https://example.com/login")
                .username("john")
                .password("s3cret")
                .build();
    }

    private static Set<String> messages(SecretSaveDto dto) {
        return validator.validate(dto).stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
    }

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("without a secret, should normalize text fields, keep the password as typed and default to active")
        void givenNoSecret_whenOf_shouldBuildActiveDto() {
            var result = SecretSaveDto.of(completeContext(), null);

            assertThat(result).usingRecursiveComparison().isEqualTo(
                    SecretSaveDto.builder()
                            .name("Bank Login")
                            .active(true)
                            .description("Main account")
                            .link("https://example.com/login")
                            .username("john doe")
                            .password("  p4ss   word ")
                            .build());
        }

        @Test
        @DisplayName("with an inactive secret, should keep it inactive")
        void givenInactiveSecret_whenOf_shouldKeepInactive() {
            var result = SecretSaveDto.of(completeContext(), createSecretDto());

            assertThat(result.isActive()).isFalse();
        }

        @Test
        @DisplayName("with an active secret, should keep it active")
        void givenActiveSecret_whenOf_shouldKeepActive() {
            var secret = createSecretDto();
            secret.setActive(true);

            var result = SecretSaveDto.of(completeContext(), secret);

            assertThat(result.isActive()).isTrue();
        }

        @Test
        @DisplayName("with an empty context, should leave every text field null")
        void givenEmptyContext_whenOf_shouldLeaveFieldsNull() {
            var result = SecretSaveDto.of(ComponentContext.empty(), null);

            assertThat(result).usingRecursiveComparison().isEqualTo(
                    SecretSaveDto.builder().active(true).build());
        }

        @Test
        @DisplayName("with blank normalized fields, should turn them into null")
        void givenBlankFields_whenOf_shouldTurnIntoNull() {
            var context = ComponentContext.empty();
            context.put("name", "   ");
            context.put("description", "");
            context.put("link", " ");
            context.put("username", "  ");

            var result = SecretSaveDto.of(context, null);

            assertThat(result).usingRecursiveComparison().isEqualTo(
                    SecretSaveDto.builder().active(true).build());
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("a fully valid dto has no violations")
        void givenValidDto_shouldHaveNoViolations() {
            assertThat(messages(validDto())).isEmpty();
        }

        @Test
        @DisplayName("optional description and link may be absent")
        void givenNoOptionalFields_shouldHaveNoViolations() {
            var dto = validDto();
            dto.setDescription(null);
            dto.setLink(null);

            assertThat(messages(dto)).isEmpty();
        }

        @Test
        @DisplayName("a password with leading, trailing and repeated spaces is valid")
        void givenPasswordWithSpaces_shouldHaveNoViolations() {
            var dto = validDto();
            dto.setPassword("  p4ss   word ");

            assertThat(messages(dto)).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   "})
        @DisplayName("a null, empty or blank password is rejected")
        void givenMissingPassword_shouldBeRejected(String password) {
            var dto = validDto();
            dto.setPassword(password);

            assertThat(messages(dto)).containsExactly("Password is required.");
        }

        @Test
        @DisplayName("a missing username is rejected")
        void givenNoUsername_shouldBeRejected() {
            var dto = validDto();
            dto.setUsername(null);

            assertThat(messages(dto)).containsExactly("Username is required.");
        }

        @ParameterizedTest
        @ValueSource(ints = {2, 51})
        @DisplayName("a username outside 3..50 characters is rejected")
        void givenUsernameOutOfRange_shouldBeRejected(int length) {
            var dto = validDto();
            dto.setUsername("u".repeat(length));

            assertThat(messages(dto)).containsExactly("Username must be between 3 and 50 characters.");
        }

        @ParameterizedTest
        @ValueSource(ints = {3, 50})
        @DisplayName("a username on the 3..50 boundaries is accepted")
        void givenUsernameOnBoundary_shouldBeAccepted(int length) {
            var dto = validDto();
            dto.setUsername("u".repeat(length));

            assertThat(messages(dto)).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(ints = {4, 251})
        @DisplayName("a description outside 5..250 characters is rejected")
        void givenDescriptionOutOfRange_shouldBeRejected(int length) {
            var dto = validDto();
            dto.setDescription("d".repeat(length));

            assertThat(messages(dto)).containsExactly("Description must be between 5 and 250 characters.");
        }

        @ParameterizedTest
        @ValueSource(ints = {5, 250})
        @DisplayName("a description on the 5..250 boundaries is accepted")
        void givenDescriptionOnBoundary_shouldBeAccepted(int length) {
            var dto = validDto();
            dto.setDescription("d".repeat(length));

            assertThat(messages(dto)).isEmpty();
        }

        @Test
        @DisplayName("a link that is not a URL is rejected")
        void givenInvalidLink_shouldBeRejected() {
            var dto = validDto();
            dto.setLink("not a valid url");

            assertThat(messages(dto)).containsExactly("Link must be a valid URL.");
        }

        @Test
        @DisplayName("a link shorter than 7 characters is rejected")
        void givenShortLink_shouldBeRejected() {
            var dto = validDto();
            dto.setLink("a.b");

            assertThat(messages(dto)).contains("Link must be between 7 and 2048 characters.");
        }

        @Test
        @DisplayName("a link longer than 2048 characters is rejected")
        void givenLongLink_shouldBeRejected() {
            var dto = validDto();
            dto.setLink("https://example.com/" + "a".repeat(2048));

            assertThat(messages(dto)).contains("Link must be between 7 and 2048 characters.");
        }

        @Test
        @DisplayName("a missing name is rejected")
        void givenNoName_shouldBeRejected() {
            var dto = validDto();
            dto.setName(null);

            assertThat(messages(dto)).containsExactly("Name is required.");
        }
    }
}
