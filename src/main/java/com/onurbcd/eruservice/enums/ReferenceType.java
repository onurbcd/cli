package com.onurbcd.eruservice.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ReferenceType implements Codeable {

    YEAR("Ano"),
    MONTH("Mês"),
    DAY("Dia");

    private final String code;

    @Override
    public String getCode() {
        return code;
    }
}
