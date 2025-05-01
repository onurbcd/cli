package com.onurbcd.eruservice.service.impl;

import com.onurbcd.eruservice.persistency.entity.Balance;
import com.onurbcd.eruservice.service.BalanceSourceService;
import com.onurbcd.eruservice.service.SourceService;
import com.onurbcd.eruservice.enums.BalanceOperation;
import com.onurbcd.eruservice.service.enums.Operation;
import com.onurbcd.eruservice.service.resource.UpdateSourceBalance;
import com.onurbcd.eruservice.util.NumberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BalanceSourceServiceImpl implements BalanceSourceService {

    private final SourceService sourceService;

    @Override
    public void save(Balance newBalance, BigDecimal currentAmount) {
        Optional
                .ofNullable(currentAmount)
                .ifPresentOrElse(p -> update(newBalance, Objects.requireNonNull(currentAmount)),
                        () -> insert(newBalance));
    }

    @Override
    public void delete(Balance balance) {
        updateBalance(balance, Operation.DELETE, balance.getAmount());
    }

    private void insert(Balance balance) {
        updateBalance(balance, Operation.INSERT, balance.getAmount());
    }

    private void update(Balance newBalance, BigDecimal currentAmount) {
        var diff = NumberUtil.subtract(newBalance.getAmount(), currentAmount);

        if (diff.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        updateBalance(newBalance, Operation.UPDATE, diff);
    }

    private void updateBalance(Balance balance, Operation operation, BigDecimal amount) {
        var param = getParam(balance, operation, amount);
        sourceService.updateBalance(param);
    }

    private UpdateSourceBalance getParam(Balance balance, Operation operation, BigDecimal amount) {
        var balanceOperation = BalanceOperation.from(operation, balance.getBalanceType());

        return UpdateSourceBalance
                .builder()
                .source(balance.getSource())
                .func(balanceOperation.getFunc())
                .value(amount)
                .build();
    }
}
