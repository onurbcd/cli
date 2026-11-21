package com.onurbcd.cli.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class DocumentTypeTest {

    @Test
    @DisplayName("declares exactly these constants, in this order, each with its code")
    void givenAllConstants_whenGetCode_shouldReturnTheirCodes() {
        var codes = CodeableTestSupport.codesByName(DocumentType.values());

        assertThat(codes).containsExactly(
                entry("BILLET", "boleto"),
                entry("INVOICE", "fatura"),
                entry("BILL", "conta"),
                entry("DARF", "darf"),
                entry("DAS", "das"),
                entry("RECEIPT", "recibo"),
                entry("PAPER", "papel"),
                entry("IPTU", "iptu"),
                entry("VOUCHER", "comprovante"));
    }

    @Test
    @DisplayName("is Codeable")
    void givenConstants_whenCheckedAgainstCodeable_shouldAllBeCodeable() {
        assertThat(DocumentType.values()).allSatisfy(value -> assertThat(value).isInstanceOf(Codeable.class));
    }
}
