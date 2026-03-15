package com.onurbcd.cli.service;

import com.onurbcd.cli.enums.BalanceOperation;
import com.onurbcd.cli.enums.Operation;
import com.onurbcd.cli.model.UpdateSourceBalance;
import com.onurbcd.cli.persistency.entity.Balance;
import com.onurbcd.cli.util.NumberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BalanceSourceService {

    private final SourceService sourceService;

    public void save(Balance newBalance, BigDecimal currentAmount) {
        Optional
                .ofNullable(currentAmount)
                .ifPresentOrElse(p -> update(newBalance, Objects.requireNonNull(currentAmount)),
                        () -> insert(newBalance));
    }

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
