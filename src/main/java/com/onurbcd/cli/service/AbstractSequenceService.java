package com.onurbcd.cli.service;

import com.onurbcd.cli.enums.Direction;
import com.onurbcd.cli.param.SequenceParam;
import com.onurbcd.cli.persistency.repository.SequenceRepository;
import com.onurbcd.cli.validator.SequenceValidator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractSequenceService implements SequenceService {

    private final SequenceRepository repository;

    private final SequenceValidator validationService;

    @Override
    public Short getNextSequence(SequenceParam sequenceParam) {
        var maxSequence = repository.getMaxSequence(sequenceParam);
        return maxSequence == null ? 1 : (short) (maxSequence + 1);
    }

    @Override
    public void swapSequence(SequenceParam currentParam, Direction direction) {
        var targetParam = getTargetParam(currentParam, direction);
        validationService.validate(currentParam, targetParam, direction);
        currentParam.setTargetSequence(Short.MAX_VALUE);
        repository.updateSequence(currentParam);
        repository.updateSequence(targetParam);
        currentParam.setSequence(Short.MAX_VALUE);
        currentParam.setTargetSequence(targetParam.getSequence());
        repository.updateSequence(currentParam);
    }

    @Override
    public void updateNextSequences(SequenceParam sequenceParam) {
        long numberOfSequencesToUpdate = repository.countNextSequences(sequenceParam);

        for (long i = 0; i < numberOfSequencesToUpdate; i++) {
            sequenceParam.setTargetSequence(sequenceParam.getSequence());
            sequenceParam.setSequence((short) (sequenceParam.getSequence() + 1));
            repository.updateSequence(sequenceParam);
        }
    }

    @Override
    public void swapPosition(SequenceParam param) {
        validationService.validateSwapPosition(param);
        // change current to max
        var paramMax = SequenceParam.of(param.getYear(), param.getMonth(), param.getSequence(), Short.MAX_VALUE,
                param.getBalanceType());
        repository.updateSequence(paramMax);
        // change middle positions
        swapMiddlePositions(param);

        // change max to target
        var paramTarget = SequenceParam.of(param.getYear(), param.getMonth(), Short.MAX_VALUE,
                param.getTargetSequence(), param.getBalanceType());

        repository.updateSequence(paramTarget);
    }

    private SequenceParam getTargetParam(SequenceParam currentParam, Direction direction) {
        var targetSequence = Direction.UP.equals(direction)
                ? (short) (currentParam.getSequence() - 1)
                : (short) (currentParam.getSequence() + 1);

        return SequenceParam.of(currentParam.getYear(), currentParam.getMonth(), targetSequence,
                currentParam.getSequence(), currentParam.getBalanceType());
    }

    private void swapMiddlePositions(SequenceParam param) {
        if (param.getSequence().compareTo(param.getTargetSequence()) > 0) {
            swapMiddlePositionsUp(param);
        } else {
            swapMiddlePositionsDown(param);
        }
    }

    private void swapMiddlePositionsUp(SequenceParam param) {
        var sequence = (short) (param.getSequence() - 1);

        for (short i = sequence, j = param.getSequence(); i >= param.getTargetSequence(); i--, j--) {
            var paramSwap = SequenceParam.of(param.getYear(), param.getMonth(), i, j, param.getBalanceType());
            repository.updateSequence(paramSwap);
        }
    }

    private void swapMiddlePositionsDown(SequenceParam param) {
        var sequence = (short) (param.getSequence() + 1);

        for (short i = sequence, j = param.getSequence(); i <= param.getTargetSequence(); i++, j++) {
            var paramSwap = SequenceParam.of(param.getYear(), param.getMonth(), i, j, param.getBalanceType());
            repository.updateSequence(paramSwap);
        }
    }
}
