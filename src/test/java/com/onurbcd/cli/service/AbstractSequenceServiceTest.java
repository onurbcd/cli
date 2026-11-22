package com.onurbcd.cli.service;

import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.Direction;
import com.onurbcd.cli.param.SequenceParam;
import com.onurbcd.cli.persistency.repository.SequenceRepository;
import com.onurbcd.cli.validator.SequenceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractSequenceServiceTest {

    private static final short YEAR = 2024;
    private static final short MONTH = 3;
    private static final short MAX = Short.MAX_VALUE;
    private static final BalanceType TYPE = BalanceType.INCOME;

    /**
     * Param values at the moment of a repository call (the service mutates and reuses the same instance).
     */
    record Snapshot(Short year, Short month, Short sequence, Short targetSequence, BalanceType balanceType) {
    }

    @Mock
    private SequenceRepository repository;
    @Mock
    private SequenceValidator validator;
    @Captor
    private ArgumentCaptor<SequenceParam> paramCaptor;

    private final List<Snapshot> updates = new ArrayList<>();
    private AbstractSequenceService service;

    static class TestSequenceService extends AbstractSequenceService {

        TestSequenceService(SequenceRepository repository, SequenceValidator validator) {
            super(repository, validator);
        }
    }

    @BeforeEach
    void setUp() {
        service = new TestSequenceService(repository, validator);
    }

    private void recordUpdates() {
        doAnswer(invocation -> {
            var param = invocation.<SequenceParam>getArgument(0);
            updates.add(snapshot(param));
            return null;
        }).when(repository).updateSequence(any(SequenceParam.class));
    }

    private static Snapshot snapshot(SequenceParam param) {
        return new Snapshot(param.getYear(), param.getMonth(), param.getSequence(), param.getTargetSequence(),
                param.getBalanceType());
    }

    private static Snapshot snap(int sequence, int target) {
        return new Snapshot(YEAR, MONTH, (short) sequence, (short) target, TYPE);
    }

    private static SequenceParam param(Integer sequence, Integer target) {
        return SequenceParam.of(YEAR, MONTH, sequence == null ? null : sequence.shortValue(),
                target == null ? null : target.shortValue(), TYPE);
    }

    @Nested
    @DisplayName("getNextSequence")
    class GetNextSequence {

        @Test
        @DisplayName("when there is no sequence yet, should return 1")
        void givenNoMax_shouldReturnOne() {
            var param = param(null, null);
            when(repository.getMaxSequence(param)).thenReturn(null);

            var result = service.getNextSequence(param);

            assertThat(result).isEqualTo((short) 1);
            verify(repository).getMaxSequence(param);
            verifyNoMoreInteractions(repository, validator);
        }

        @Test
        @DisplayName("when a max sequence exists, should return it plus one")
        void givenMax_shouldReturnMaxPlusOne() {
            var param = param(null, null);
            when(repository.getMaxSequence(param)).thenReturn((short) 5);

            var result = service.getNextSequence(param);

            assertThat(result).isEqualTo((short) 6);
            verify(repository).getMaxSequence(param);
            verifyNoMoreInteractions(repository, validator);
        }
    }

    @Nested
    @DisplayName("swapSequence")
    class SwapSequence {

        static Stream<Arguments> directions() {
            return Stream.of(
                    Arguments.of(Direction.UP, 4),
                    Arguments.of(Direction.DOWN, 6));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("directions")
        @DisplayName("should validate, move current out of the way, move the target and then current into the target slot")
        void givenDirection_shouldSwapThroughMaxSequence(Direction direction, int targetSequence) {
            recordUpdates();
            var current = param(5, null);

            service.swapSequence(current, direction);

            verify(validator).validate(same(current), paramCaptor.capture(), eq(direction));
            assertThat(snapshot(paramCaptor.getValue())).isEqualTo(snap(targetSequence, 5));
            assertThat(updates).containsExactly(snap(5, MAX), snap(targetSequence, 5), snap(MAX, targetSequence));
            assertThat(snapshot(current)).isEqualTo(snap(MAX, targetSequence));
            verifyUpdateCountAndNothingElse(3);
        }

        @Test
        @DisplayName("when validation fails, should propagate and not touch the repository")
        void givenValidationFailure_shouldPropagateWithoutUpdates() {
            var current = param(5, null);
            var failure = new IllegalStateException("invalid");
            doThrow(failure).when(validator).validate(any(), any(), any());

            assertThatThrownBy(() -> service.swapSequence(current, Direction.UP)).isSameAs(failure);

            verify(validator).validate(same(current), paramCaptor.capture(), eq(Direction.UP));
            assertThat(snapshot(paramCaptor.getValue())).isEqualTo(snap(4, 5));
            assertThat(snapshot(current)).isEqualTo(new Snapshot(YEAR, MONTH, (short) 5, null, TYPE));
            verifyNoMoreInteractions(repository, validator);
        }
    }

    @Nested
    @DisplayName("updateNextSequences")
    class UpdateNextSequences {

        @Test
        @DisplayName("when there are no next sequences, should not update anything")
        void givenNoNextSequences_shouldNotUpdate() {
            var param = param(5, null);
            when(repository.countNextSequences(param)).thenReturn(0L);

            service.updateNextSequences(param);

            assertThat(snapshot(param)).isEqualTo(new Snapshot(YEAR, MONTH, (short) 5, null, TYPE));
            verify(repository).countNextSequences(param);
            verifyNoMoreInteractions(repository, validator);
        }

        @Test
        @DisplayName("should shift each following sequence up by one, one update per sequence")
        void givenNextSequences_shouldShiftEachOne() {
            recordUpdates();
            var param = param(5, null);
            when(repository.countNextSequences(param)).thenReturn(3L);

            service.updateNextSequences(param);

            assertThat(updates).containsExactly(snap(6, 5), snap(7, 6), snap(8, 7));
            assertThat(snapshot(param)).isEqualTo(snap(8, 7));
            verify(repository).countNextSequences(param);
            verifyUpdateCountAndNothingElse(3);
        }
    }

    @Nested
    @DisplayName("swapPosition")
    class SwapPosition {

        static Stream<Arguments> positions() {
            return Stream.of(
                    Arguments.of("moving up several positions", 5, 2,
                            List.of(snap(5, MAX), snap(4, 5), snap(3, 4), snap(2, 3), snap(MAX, 2))),
                    Arguments.of("moving up one position", 3, 2, List.of(snap(3, MAX), snap(2, 3), snap(MAX, 2))),
                    Arguments.of("moving down several positions", 2, 4,
                            List.of(snap(2, MAX), snap(3, 2), snap(4, 3), snap(MAX, 4))),
                    Arguments.of("moving down one position", 2, 3, List.of(snap(2, MAX), snap(3, 2), snap(MAX, 3))),
                    Arguments.of("same position", 3, 3, List.of(snap(3, MAX), snap(MAX, 3))));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("positions")
        @DisplayName("should validate and move the positions in between through the max sequence")
        void givenPositions_shouldUpdateInOrder(String label, int sequence, int target, List<Snapshot> expected) {
            recordUpdates();
            var param = param(sequence, target);

            service.swapPosition(param);

            assertThat(updates).containsExactlyElementsOf(expected);
            assertThat(snapshot(param)).isEqualTo(snap(sequence, target));
            verify(validator).validateSwapPosition(param);
            verifyUpdateCountAndNothingElse(expected.size());
        }

        @Test
        @DisplayName("when validation fails, should propagate and not touch the repository")
        void givenValidationFailure_shouldPropagateWithoutUpdates() {
            var param = param(5, 2);
            var failure = new IllegalStateException("invalid");
            doThrow(failure).when(validator).validateSwapPosition(param);

            assertThatThrownBy(() -> service.swapPosition(param)).isSameAs(failure);

            verify(validator).validateSwapPosition(param);
            verifyNoMoreInteractions(repository, validator);
        }
    }

    private void verifyUpdateCountAndNothingElse(int updateCount) {
        verify(repository, times(updateCount)).updateSequence(any(SequenceParam.class));
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(validator);
    }
}
