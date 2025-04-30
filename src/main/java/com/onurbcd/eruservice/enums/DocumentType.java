package com.onurbcd.eruservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DocumentType {

    /**
     * Boleto
     */
    BILLET("boleto"),

    /**
     * Fatura
     */
    INVOICE("fatura"),

    /**
     * Conta - para os casos da faxina, contador, vigilante ou conta da água
     */
    BILL("conta"),

    /**
     * Documento de Arrecadação de Receitas Federais
     */
    DARF("darf"),

    /**
     * Documento de Arrecadação do Simples Nacional
     */
    DAS("das"),

    /**
     * Recibo - para o caso do vigilante
     */
    RECEIPT("recibo"),

    /**
     * Papel - para o caso que não tem formato digital, mas sempre pode ser scaneado
     */
    PAPER("papel"),

    /**
     * Imposto sobre a Propriedade Predial e Territorial Urbana ou Imposto Predial e Territorial Urbano.
     */
    IPTU("iptu");

    private final String code;
}
