package com.onurbcd.cli.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SourceType implements Codeable {

    USABLE("Disponível"),
    UNUSABLE("Indisponível");

    private final String code;

    @Override
    public String getCode() {
        return code;
    }
}
