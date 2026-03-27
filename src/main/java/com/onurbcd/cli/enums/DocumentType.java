package com.onurbcd.cli.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DocumentType implements Codeable {

    /**
     * Boleto bancário - documento de pagamento emitido por instituições financeiras
     * utilizado para quitar dívidas, contas ou serviços. Contém código de barras
     * para pagamento em bancos, lotéricas ou canais digitais.
     */
    BILLET("boleto"),

    /**
     * Fatura - documento comercial que detalha produtos ou serviços fornecidos,
     * valores, condições de pagamento e prazo de vencimento. Serve como
     * instrumento de cobrança e comprovante fiscal da transação comercial.
     */
    INVOICE("fatura"),

    /**
     * Conta - documento que representa uma cobrança por serviços específicos.
     * Utilizado para casos como faxina, contador, vigilante ou conta de água,
     * onde o serviço é prestado periodicamente e gera uma cobrança regular.
     */
    BILL("conta"),

    /**
     * DARF (Documento de Arrecadação da Receita Federal) - formulário oficial
     * utilizado para pagamento de tributos federais como Imposto de Renda,
     * PIS, COFINS, CSLL e IPI. Emitido pela Receita Federal do Brasil.
     */
    DARF("darf"),

    /**
     * DAS (Documento de Arrecadação do Simples Nacional) - guia única de
     * pagamento para empresas optantes pelo Simples Nacional. Unifica diversos
     * tributos federais (IRPJ, CSLL, PIS, COFINS, IPI, CPP, ISS e ICMS) em
     * um único documento.
     */
    DAS("das"),

    /**
     * Recibo - documento que comprova o recebimento de pagamento ou entrega
     * de mercadorias/serviços. Utilizado para casos como pagamento ao vigilante,
     * servindo como prova formal de que a transação foi concluída.
     */
    RECEIPT("recibo"),

    /**
     * Papel - documento físico que não existe em formato digital original.
     * Pode ser escaneado ou digitalizado posteriormente para armazenamento
     * e processamento eletrônico, mas sua origem é um documento material.
     */
    PAPER("papel"),

    /**
     * IPTU (Imposto sobre a Propriedade Predial e Territorial Urbana) - tributo
     * municipal cobrado anualmente sobre imóveis urbanos. Também conhecido como
     * Imposto Predial e Territorial Urbano, financia serviços públicos municipais.
     */
    IPTU("iptu"),

    /**
     * Comprovante - documento que serve como prova ou evidência de uma
     * transação, pagamento, serviço prestado ou qualquer outra ação que
     * necessite de confirmação oficial. Pode ser utilizado em diversos contextos
     * como comprovante de pagamento, inscrição, entrega ou participação.
     */
    VOUCHER("comprovante");

    private final String code;

    @Override
    public String getCode() {
        return code;
    }
}
