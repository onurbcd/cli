package com.onurbcd.cli.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CurrencyType implements Codeable {

    MONEY("Dinheiro (espécie)"),
    DIGITAL("Digital");

    private final String code;

    @Override
    public String getCode() {
        return code;
    }
}
