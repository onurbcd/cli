package com.onurbcd.cli.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class SourceTypeTest {

    @Test
    @DisplayName("declares exactly these constants, in this order, each with its code")
    void givenAllConstants_whenGetCode_shouldReturnTheirCodes() {
        var codes = CodeableTestSupport.codesByName(SourceType.values());

        assertThat(codes).containsExactly(
                entry("USABLE", "Disponível"),
                entry("UNUSABLE", "Indisponível"));
    }

    @Test
    @DisplayName("is Codeable")
    void givenConstants_whenCheckedAgainstCodeable_shouldAllBeCodeable() {
        assertThat(SourceType.values()).allSatisfy(value -> assertThat(value).isInstanceOf(Codeable.class));
    }
}
