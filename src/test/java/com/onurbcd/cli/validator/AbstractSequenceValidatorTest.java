package com.onurbcd.cli.validator;

import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.Direction;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.param.SequenceParam;
import com.onurbcd.cli.persistency.repository.SequenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractSequenceValidatorTest {

    private static final short YEAR = 2025;
    private static final short MONTH = 7;

    @Mock
    private SequenceRepository repository;

    private AbstractSequenceValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AbstractSequenceValidator(repository) {
        };
    }

    private static SequenceParam param(Short sequence, Short targetSequence) {
        return SequenceParam.of(YEAR, MONTH, sequence, targetSequence, BalanceType.INCOME);
    }

    private static void assertUnchanged(SequenceParam actual, Short sequence, Short targetSequence) {
        assertThat(actual).usingRecursiveComparison().isEqualTo(param(sequence, targetSequence));
    }

    static Stream<Arguments> rejectedMoves() {
        return Stream.of(
                Arguments.of("up at the first position", Direction.UP, (short) 1, null, ErrorCode.WRONG_DIRETION_UP),
                Arguments.of("down without a target (false)", Direction.DOWN, (short) 2, false,
                        ErrorCode.WRONG_DIRETION_DOWN),
                Arguments.of("down without a target (null)", Direction.DOWN, (short) 2, null,
                        ErrorCode.WRONG_DIRETION_DOWN),
                Arguments.of("up with a missing target (false)", Direction.UP, (short) 2, false,
                        ErrorCode.ENTITY_DOES_NOT_EXIST),
                Arguments.of("up with a missing target (null)", Direction.UP, (short) 2, null,
                        ErrorCode.ENTITY_DOES_NOT_EXIST),
                Arguments.of("no direction with a missing target", null, (short) 1, false,
                        ErrorCode.ENTITY_DOES_NOT_EXIST));
    }

    static Stream<Arguments> acceptedMoves() {
        return Stream.of(
                Arguments.of("up with an existing target", Direction.UP, (short) 2),
                Arguments.of("down with an existing target", Direction.DOWN, (short) 1),
                Arguments.of("down from a high position", Direction.DOWN, (short) 9));
    }

    @Nested
    @DisplayName("validate")
    class Validate {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.onurbcd.cli.validator.AbstractSequenceValidatorTest#rejectedMoves")
        @DisplayName("rejects the move with the matching business error")
        void givenInvalidMove_whenValidate_shouldThrow(String label, Direction direction, short sequence,
                                                       Boolean targetExists, ErrorCode expected) {

            var current = param(sequence, null);
            var target = param(sequence, null);

            if (!(direction == Direction.UP && sequence == 1)) {
                when(repository.existsSequence(target)).thenReturn(targetExists);
            }

            assertThatThrownBy(() -> validator.validate(current, target, direction))
                    .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getError()).isEqualTo(expected));

            if (direction == Direction.UP && sequence == 1) {
                verifyNoInteractions(repository);
            } else {
                verify(repository).existsSequence(target);
                verifyNoMoreInteractions(repository);
            }
            assertUnchanged(current, sequence, null);
            assertUnchanged(target, sequence, null);
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.onurbcd.cli.validator.AbstractSequenceValidatorTest#acceptedMoves")
        @DisplayName("accepts the move when the target sequence exists")
        void givenValidMove_whenValidate_shouldNotThrow(String label, Direction direction, short sequence) {
            var current = param(sequence, null);
            var target = param(sequence, null);
            when(repository.existsSequence(target)).thenReturn(true);

            assertThatCode(() -> validator.validate(current, target, direction)).doesNotThrowAnyException();

            verify(repository).existsSequence(target);
            verifyNoMoreInteractions(repository);
            assertUnchanged(current, sequence, null);
            assertUnchanged(target, sequence, null);
        }
    }

    static Stream<Arguments> rejectedSwaps() {
        return Stream.of(
                Arguments.of("same position", (short) 2, (short) 2, null, ErrorCode.SWAP_SAME_POSITION),
                Arguments.of("target is zero", (short) 2, (short) 0, null, ErrorCode.WRONG_DIRETION_DOWN),
                Arguments.of("target is negative", (short) 2, (short) -1, null, ErrorCode.WRONG_DIRETION_DOWN),
                Arguments.of("target above the maximum", (short) 1, (short) 4, (short) 3,
                        ErrorCode.WRONG_DIRETION_UP));
    }

    static Stream<Arguments> acceptedSwaps() {
        return Stream.of(
                Arguments.of("target is the first position", (short) 3, (short) 1, (short) 3),
                Arguments.of("target is the maximum", (short) 1, (short) 3, (short) 3),
                Arguments.of("target is in the middle", (short) 1, (short) 2, (short) 3));
    }

    @Nested
    @DisplayName("validateSwapPosition")
    class ValidateSwapPosition {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.onurbcd.cli.validator.AbstractSequenceValidatorTest#rejectedSwaps")
        @DisplayName("rejects the swap with the matching business error")
        void givenInvalidSwap_whenValidateSwapPosition_shouldThrow(String label, short sequence, short target,
                                                                   Short max, ErrorCode expected) {

            var param = param(sequence, target);

            if (max != null) {
                when(repository.getMaxSequence(param)).thenReturn(max);
            }

            assertThatThrownBy(() -> validator.validateSwapPosition(param))
                    .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getError()).isEqualTo(expected));

            if (max == null) {
                verifyNoInteractions(repository);
            } else {
                verify(repository).getMaxSequence(param);
                verifyNoMoreInteractions(repository);
            }
            assertUnchanged(param, sequence, target);
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.onurbcd.cli.validator.AbstractSequenceValidatorTest#acceptedSwaps")
        @DisplayName("accepts targets between the first position and the maximum, both included")
        void givenValidSwap_whenValidateSwapPosition_shouldNotThrow(String label, short sequence, short target,
                                                                    short max) {

            var param = param(sequence, target);
            when(repository.getMaxSequence(param)).thenReturn(max);

            assertThatCode(() -> validator.validateSwapPosition(param)).doesNotThrowAnyException();

            verify(repository).getMaxSequence(param);
            verifyNoMoreInteractions(repository);
            assertUnchanged(param, sequence, target);
        }
    }
}
