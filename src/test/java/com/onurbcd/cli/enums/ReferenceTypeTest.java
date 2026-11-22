package com.onurbcd.cli.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class ReferenceTypeTest {

    @Test
    @DisplayName("declares exactly these constants, in this order, each with its code")
    void givenAllConstants_whenGetCode_shouldReturnTheirCodes() {
        var codes = CodeableTestSupport.codesByName(ReferenceType.values());

        assertThat(codes).containsExactly(
                entry("YEAR", "Ano"),
                entry("MONTH", "Mês"),
                entry("DAY", "Dia"));
    }

    @Test
    @DisplayName("is Codeable")
    void givenConstants_whenCheckedAgainstCodeable_shouldAllBeCodeable() {
        assertThat(ReferenceType.values()).allSatisfy(value -> assertThat(value).isInstanceOf(Codeable.class));
    }
}
