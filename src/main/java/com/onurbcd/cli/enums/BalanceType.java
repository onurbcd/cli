package com.onurbcd.cli.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BalanceType implements Codeable {

    INCOME("Entrada"),
    OUTCOME("Saída");

    private final String code;

    @Override
    public String getCode() {
        return code;
    }
}
