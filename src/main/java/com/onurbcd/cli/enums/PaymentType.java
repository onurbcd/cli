package com.onurbcd.cli.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaymentType {

    /**
     * Modo de transferência monetária instantâneo e de pagamento eletrônico instantâneo em real brasileiro
     */
    PIX("pix"),

    /**
     * Código de Barras - no caso de boletos
     */
    BAR_CODE("boleto"),

    /**
     * Transferência - no caso de pagamentos de faturas de cartão de crédito, em que o dinheiro sai direto da conta escolhida
     */
    TRANSFER("transferência");

    private final String code;
}
