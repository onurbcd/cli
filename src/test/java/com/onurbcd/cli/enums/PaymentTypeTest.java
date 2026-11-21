package com.onurbcd.cli.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class PaymentTypeTest {

    @Test
    @DisplayName("declares exactly these constants, in this order, each with its code")
    void givenAllConstants_whenGetCode_shouldReturnTheirCodes() {
        var codes = CodeableTestSupport.codesByName(PaymentType.values());

        assertThat(codes).containsExactly(
                entry("CREDIT_CARD", "Cartão de crédito"),
                entry("DEBIT_CARD", "Cartão de débito"),
                entry("CASH", "Dinheiro (espécie)"),
                entry("CHECK", "Cheque"),
                entry("PIX", "Pix"),
                entry("AUTOMATIC_DEBIT", "Débito automático"),
                entry("BAR_CODE", "Boleto bancário"),
                entry("PAYMENT_LINK", "Link de pagamento"),
                entry("QR_CODE", "QR Code"),
                entry("DIGITAL_WALLET", "Carteira digital"),
                entry("CORPORATE_INVOICE", "Boleto corporativo"),
                entry("INVOICE_PAYMENT", "Pagamento por fatura"),
                entry("ACCOUNT_CREDIT", "Crédito em conta"),
                entry("GIFT_CARD", "Vale-presente/vale-alimentação"),
                entry("BANK_DEPOSIT", "Depósito bancário"),
                entry("PAYMENT_ORDER", "Ordem de pagamento"),
                entry("TED", "TED"),
                entry("DOC", "DOC"),
                entry("CRYPTO_CURRENCY", "Criptomoeda"),
                entry("INSTALLMENT_PLAN", "Carnê/Parcelamento"),
                entry("PAYMENT_BY_APPROACH", "Pagamento por aproximação (NFC)"),
                entry("RECURRING_PAYMENT", "Pagamento recorrente"),
                entry("STORE_FINANCING", "Crediário"),
                entry("WHATSAPP_PAY", "WhatsApp Pay"),
                entry("TRANSPARENT_CHECKOUT", "Checkout transparente"),
                entry("BUY_NOW_PAY_LATER", "Compre Agora, Pague Depois (BNPL)"));
    }

    @Test
    @DisplayName("is Codeable")
    void givenConstants_whenCheckedAgainstCodeable_shouldAllBeCodeable() {
        assertThat(PaymentType.values()).allSatisfy(value -> assertThat(value).isInstanceOf(Codeable.class));
    }
}
