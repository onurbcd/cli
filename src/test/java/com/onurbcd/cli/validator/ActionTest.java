package com.onurbcd.cli.validator;

import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActionTest {

    private static final ErrorCode ERROR = ErrorCode.SEQUENCE_CHANGED;

    private static void assertRejects(Action action) {
        assertThatThrownBy(() -> action.orElseThrow(ERROR, (short) 1, (short) 2))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getError()).isEqualTo(ERROR);
                    assertThat(e.getMessage()).isEqualTo(ERROR.format((short) 1, (short) 2));
                });
    }

    private static void assertAccepts(Action action) {
        assertThatCode(() -> action.orElseThrow(ERROR, (short) 1, (short) 2)).doesNotThrowAnyException();
    }

    @Nested
    @DisplayName("checkIf / checkIfNot")
    class Booleans {

        @Test
        void givenTrue_whenCheckIf_shouldAccept() {
            assertAccepts(Action.checkIf(true));
        }

        @Test
        void givenFalse_whenCheckIf_shouldReject() {
            assertRejects(Action.checkIf(false));
        }

        @Test
        void givenTrue_whenCheckIfNot_shouldReject() {
            assertRejects(Action.checkIfNot(true));
        }

        @Test
        void givenFalse_whenCheckIfNot_shouldAccept() {
            assertAccepts(Action.checkIfNot(false));
        }
    }

    @Nested
    @DisplayName("checkIfNotNull")
    class NotNull {

        @Test
        void givenValue_shouldAccept() {
            assertAccepts(Action.checkIfNotNull("value"));
        }

        @Test
        void givenNull_shouldReject() {
            assertRejects(Action.checkIfNotNull(null));
        }
    }

    @Nested
    @DisplayName("checkIfNotEmpty(Collection)")
    class NotEmptyCollection {

        @Test
        void givenNull_shouldReject() {
            assertRejects(Action.checkIfNotEmpty((List<String>) null));
        }

        @Test
        void givenEmpty_shouldReject() {
            assertRejects(Action.checkIfNotEmpty(List.of()));
        }

        @Test
        void givenElements_shouldAccept() {
            assertAccepts(Action.checkIfNotEmpty(List.of("a")));
        }
    }

    @Nested
    @DisplayName("checkIfNotEmpty(array)")
    class NotEmptyArray {

        @Test
        void givenNull_shouldReject() {
            assertRejects(Action.checkIfNotEmpty((String[]) null));
        }

        @Test
        void givenEmpty_shouldReject() {
            assertRejects(Action.checkIfNotEmpty(new String[0]));
        }

        @Test
        void givenElements_shouldAccept() {
            assertAccepts(Action.checkIfNotEmpty(new String[]{"a"}));
        }
    }

    @Nested
    @DisplayName("checkIfNotBlank")
    class NotBlank {

        @Test
        void givenNull_shouldReject() {
            assertRejects(Action.checkIfNotBlank(null));
        }

        @Test
        void givenEmpty_shouldReject() {
            assertRejects(Action.checkIfNotBlank(""));
        }

        @Test
        void givenSpaces_shouldReject() {
            assertRejects(Action.checkIfNotBlank("   "));
        }

        @Test
        void givenText_shouldAccept() {
            assertAccepts(Action.checkIfNotBlank("a"));
        }
    }

    @Nested
    @DisplayName("orElseThrow")
    class OrElseThrow {

        @Test
        @DisplayName("an error without arguments keeps its plain message")
        void givenNoArgs_shouldUseErrorMessage() {
            assertThatThrownBy(() -> Action.checkIf(false).orElseThrow(ErrorCode.DAY_CHANGED))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.DAY_CHANGED);
                        assertThat(e.getMessage()).isEqualTo(ErrorCode.DAY_CHANGED.format());
                    });
        }
    }

    @Nested
    @DisplayName("orElseThrowNotFound")
    class OrElseThrowNotFound {

        @Test
        @DisplayName("when the condition fails, throws ENTITY_DOES_NOT_EXIST with the arguments")
        void givenFailedCondition_shouldThrowNotFound() {
            assertThatThrownBy(() -> Action.checkIf(false).orElseThrowNotFound(ID))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.ENTITY_DOES_NOT_EXIST);
                        assertThat(e.getMessage()).isEqualTo(ErrorCode.ENTITY_DOES_NOT_EXIST.format(ID));
                    });
        }

        @Test
        @DisplayName("when the condition holds, does not throw")
        void givenSatisfiedCondition_shouldNotThrow() {
            assertThatCode(() -> Action.checkIf(true).orElseThrowNotFound(ID)).doesNotThrowAnyException();
        }
    }
}
