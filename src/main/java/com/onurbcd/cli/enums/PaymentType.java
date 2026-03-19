package com.onurbcd.cli.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaymentType {

    /*
     * Métodos Tradicionais
     */

    /**
     * Pagamento realizado com cartão de crédito.
     *
     * <p>Método de pagamento que permite parcelar compras e pagar em datas futuras.
     * O valor é debitado na fatura do cartão e pago posteriormente pelo titular.
     * Utilizado para compras em lojas físicas, online e aplicativos.</p>
     */
    CREDIT_CARD("Cartão de crédito"),

    /**
     * Pagamento realizado com cartão de débito.
     *
     * <p>Método de pagamento que debita o valor diretamente da conta corrente do titular
     * no momento da transação. Não permite parcelamento e o saldo é verificado
     * instantaneamente. Ideal para controle financeiro e compras do dia a dia.</p>
     */
    DEBIT_CARD("Cartão de débito"),

    /**
     * Pagamento realizado em dinheiro (espécie).
     *
     * <p>Método de pagamento tradicional utilizando cédulas e moedas.
     * Oferece anonimato e não depende de sistemas eletrônicos.
     * Comum em pequenos estabelecimentos e transações informais.</p>
     */
    CASH("Dinheiro (espécie)"),

    /**
     * Pagamento realizado através de cheque.
     *
     * <p>Ordem de pagamento escrita que autoriza o banco a debitar o valor da conta
     * do emitente e creditar na conta do beneficiário. Requer fundos disponíveis
     * e pode levar alguns dias para compensar. Em desuso no Brasil.</p>
     */
    CHECK("Cheque"),

    /*
     * Métodos Digitais
     */

    /**
     * Pagamento instantâneo via PIX.
     *
     * <p>Sistema de pagamento instantâneo desenvolvido pelo Banco Central que permite
     * transferências de dinheiro em tempo real, 24/7, inclusive fins de semana e feriados.
     * Utiliza chaves cadastradas (CPF, CNPJ, e-mail, telefone ou chave aleatória).
     * Ideal para pagamentos rápidos e sem custo adicional para pessoa física.</p>
     */
    PIX("Pix"),

    /**
     * Débito automático em conta bancária.
     *
     * <p>Autorização prévia para débito automático de contas recorrentes (água, luz,
     * telefone, etc.) diretamente da conta corrente. O valor é debitado na data
     * de vencimento sem necessidade de intervenção manual. Facilita organização
     * e evita esquecimentos de pagamentos.</p>
     */
    AUTOMATIC_DEBIT("Débito automático"),

    /**
     * Pagamento através de código de barras.
     *
     * <p>Método utilizado principalmente para pagamento de boletos bancários.
     * O código de barras contém todas as informações necessárias para identificar
     * e liquidar a obrigação. Pode ser pago em aplicativos de banco, lotéricas
     * ou caixas eletrônicos. Comum para contas de serviços e carnês.</p>
     */
    BAR_CODE("Boleto bancário"),

    /**
     * Pagamento através de link de pagamento.
     *
     * <p>URL gerada pelo recebedor que direciona para uma página de pagamento segura.
     * O pagador insere dados do cartão ou outros métodos para concluir a transação.
     * Amplamente utilizado em e-commerce, cobranças online e faturamento eletrônico.
     * Facilita pagamentos à distância sem necessidade de presença física.</p>
     */
    PAYMENT_LINK("Link de pagamento"),

    /**
     * Pagamento através de código QR.
     *
     * <p>Leitura de código bidimensional que contém informações de pagamento.
     * Pode ser estático (valor fixo) ou dinâmico (valor variável).
     * Utilizado em pagamentos em lojas, restaurantes e aplicativos de transporte.
     * Oferece praticidade e segurança ao evitar digitação de dados.</p>
     */
    QR_CODE("QR Code"),

    /**
     * Pagamento através de carteira digital.
     *
     * <p>Aplicativos que armazenam cartões e permitem pagamentos por aproximação
     * (NFC) ou QR code. Inclui Apple Pay, Google Pay, Samsung Pay e outros.
     * Oferece camada adicional de segurança através de biometria ou tokenização.
     * Crescente adoção em pagamentos de contato e transações digitais.</p>
     */
    DIGITAL_WALLET("Carteira digital"),

    /*
     * Métodos Corporativos
     */

    /**
     * Boleto bancário corporativo.
     *
     * <p>Boletos emitidos por empresas para pagamento de grandes volumes,
     * geralmente com valores superiores aos boletos convencionais.
     * Utilizados em transações B2B, pagamentos de fornecedores e serviços corporativos.
     * Podem ter regras de negociação e prazos diferenciados.</p>
     */
    CORPORATE_INVOICE("Boleto corporativo"),

    /**
     * Pagamento por fatura de cartão de crédito.
     *
     * <p>Transferência bancária realizada para quitar a fatura do cartão de crédito.
     * O dinheiro sai diretamente da conta corrente do titular e é aplicado
     * no pagamento da fatura vencida ou a vencer. Essencial para manter
     * o cartão em dia e evitar juros e multas por atraso.</p>
     */
    INVOICE_PAYMENT("Pagamento por fatura"),

    /**
     * Crédito em conta bancária.
     *
     * <p>Valor creditado diretamente na conta corrente ou poupança do beneficiário.
     * Pode ser originário de transferências, estornos, salários ou outras
     * operações financeiras. O valor fica imediatamente disponível
     * para utilização pelo titular da conta.</p>
     */
    ACCOUNT_CREDIT("Crédito em conta"),

    /**
     * Vale-presente e vale-alimentação.
     *
     * <p>Cartões pré-pagos ou vouchers utilizados para pagamento em estabelecimentos
     * conveniados. Vale-alimentação para refeições, vale-refeição para alimentação
     * em geral, e vale-presente para compras diversas. Oferecidos como benefício
     * empresarial ou vendidos como presente. Possuem regras de uso específicas.</p>
     */
    GIFT_CARD("Vale-presente/vale-alimentação"),

    /*
     * Outros Métodos
     */

    /**
     * Depósito bancário direto.
     *
     * <p>Transferência de dinheiro realizada diretamente no caixa do banco
     * ou através de terminais de autoatendimento. Requer dados completos
     * do beneficiário (banco, agência, conta, nome, CPF/CNPJ). Pode ter
     * custo e tempo de processamento variáveis conforme o banco e valor.</p>
     */
    BANK_DEPOSIT("Depósito bancário"),

    /**
     * Ordem de pagamento bancária.
     *
     * <p>Comando dado ao banco para realizar transferência de valores entre contas.
     * Pode ser emitida em agências, internet banking ou aplicativos.
     * Utilizada para pagamentos de médio e grande porte, com registro
     * e comprovação da operação. Mais formal que transferências simples.</p>
     */
    PAYMENT_ORDER("Ordem de pagamento"),

    /**
     * Transferência Eletrônica Disponível (TED).
     *
     * <p>Sistema de transferência entre contas de diferentes bancos para valores
     * acima de R$ 5.000. O processamento ocorre em tempo real durante o horário
     * bancário, com crédito imediato na conta do destinatário. Possui taxa
     * maior que DOC e é ideal para transferências de alto valor.</p>
     */
    TED("TED"),

    /**
     * Documento de Ordem de Crédito (DOC).
     *
     * <p>Sistema de transferência entre contas de diferentes bancos para valores
     * geralmente até R$ 5.000. O processamento não é imediato, podendo levar
     * até o próximo dia útil para creditar na conta do destinatário. Possui
     * taxa menor que TED e é ideal para transferências de menor valor.</p>
     */
    DOC("DOC"),

    /**
     * Pagamento com criptomoedas.
     *
     * <p>Transações realizadas utilizando moedas digitais como Bitcoin, Ethereum,
     * ou outras criptomoedas. Baseadas em tecnologia blockchain, oferecem
     * descentralização e anonimato relativo. Voláteis e com aceitação limitada,
     * mas crescente. Requer carteira digital e conhecimento técnico.</p>
     */
    CRYPTO_CURRENCY("Criptomoeda"),

    /**
     * Pagamento parcelado através de carnê.
     *
     * <p>Sistema de pagamento dividido em múltiplas parcelas com vencimentos
     * mensais. Cada parcela possui seu próprio boleto ou método de pagamento.
     * Comum em compras de maior valor (eletrodomésticos, imóveis, cursos).
     * Facilita aquisição de bens e serviços diluindo o custo no tempo.</p>
     */
    INSTALLMENT_PLAN("Carnê/Parcelamento"),

    /**
     * Pagamento por aproximação (NFC).
     *
     * <p>Tecnologia Near Field Communication que permite pagamentos sem contato
     * aproximando o cartão ou celular à máquina. Não necessita digitação de senha
     * para valores menores (geralmente até R$ 50). Oferece rapidez e conveniência
     * em transações do dia a dia. Disponível em cartões com chip NFC e celulares.</p>
     */
    PAYMENT_BY_APPROACH("Pagamento por aproximação (NFC)"),

    /**
     * Pagamento recorrente automático.
     *
     * <p>Cobranças automáticas realizadas em intervalos regulares (mensal, anual,
     * etc.) para serviços de assinatura. Utiliza dados de cartão ou autorização
     * de débito previamente cadastrados. Comum em streaming, softwares, academias
     * e outros serviços contínuos. Facilita gestão financeira e reduz inadimplência.</p>
     */
    RECURRING_PAYMENT("Pagamento recorrente"),

    /**
     * Crediário (financiamento da loja).
     *
     * <p>Financiamento oferecido diretamente pelo estabelecimento comercial,
     * sem necessidade de cartão de crédito. O cliente parcela a compra
     * diretamente com a loja, geralmente com juros menores que cartões.
     * Comum em móveis, eletrodomésticos e veículos. Oferece flexibilidade
     * e acesso a crédito para quem não possui cartão.</p>
     */
    STORE_FINANCING("Crediário"),

    /**
     * Pagamento através do WhatsApp Pay.
     *
     * <p>Sistema de pagamentos integrado ao WhatsApp que permite transferências
     * e pagamentos diretamente na conversa. Utiliza cartões de débito/crédito
     * cadastrados pelo Facebook Pay. Oferece conveniência para transações
     * entre contatos e pequenos comerciantes. Ainda em expansão no Brasil.</p>
     */
    WHATSAPP_PAY("WhatsApp Pay"),

    /**
     * Checkout transparente.
     *
     * <p>Sistema de pagamento que permite a finalização da compra sem que
     * o usuário saia da página atual do e-commerce. O formulário de pagamento
     * é integrado diretamente na loja virtual, oferecendo experiência
     * contínua e reduzindo abandono de carrinho. Mantém segurança através
     * de certificação PCI e processamento externo dos dados.</p>
     */
    TRANSPARENT_CHECKOUT("Checkout transparente"),

    /**
     * Compre Agora, Pague Depois (BNPL).
     *
     * <p>Modelo de pagamento que permite parcelar compras sem cartão de crédito,
     * com carência para primeira parcela. O consumidor recebe o produto
     * imediatamente e paga em parcelas futuras. Crescente no e-commerce,
     * especialmente entre jovens. Oferece flexibilidade e acesso ao crédito.</p>
     */
    BUY_NOW_PAY_LATER("Compre Agora, Pague Depois (BNPL)");

    private final String code;
}
