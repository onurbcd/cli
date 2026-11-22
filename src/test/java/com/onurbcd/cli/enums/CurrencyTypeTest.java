package com.onurbcd.cli.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class CurrencyTypeTest {

    @Test
    @DisplayName("declares exactly these constants, in this order, each with its code")
    void givenAllConstants_whenGetCode_shouldReturnTheirCodes() {
        var codes = CodeableTestSupport.codesByName(CurrencyType.values());

        assertThat(codes).containsExactly(
                entry("MONEY", "Dinheiro (espécie)"),
                entry("DIGITAL", "Digital"));
    }

    @Test
    @DisplayName("is Codeable")
    void givenConstants_whenCheckedAgainstCodeable_shouldAllBeCodeable() {
        assertThat(CurrencyType.values()).allSatisfy(value -> assertThat(value).isInstanceOf(Codeable.class));
    }
}
