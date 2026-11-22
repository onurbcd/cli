package com.onurbcd.cli.persistency.entity;

import com.onurbcd.cli.enums.CurrencyType;
import com.onurbcd.cli.enums.SourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;

class SourceTest {

    private static final UUID OTHER_ID = UUID.fromString("99999999-9999-9999-0000-000000000001");
    private static final BigDecimal BALANCE = new BigDecimal("10.5000");

    @Test
    @DisplayName("the builder fills every field")
    void givenBuilder_whenBuild_shouldFillEveryField() {
        var incomeSource = IncomeSource.builder().id(ID).build();

        var source = Source.builder()
                .id(ID)
                .name("wallet")
                .active(true)
                .incomeSource(incomeSource)
                .sourceType(SourceType.USABLE)
                .currencyType(CurrencyType.MONEY)
                .balance(BALANCE)
                .build();

        assertThat(source.getId()).isEqualTo(ID);
        assertThat(source.getName()).isEqualTo("wallet");
        assertThat(source.getActive()).isTrue();
        assertThat(source.getIncomeSource()).isSameAs(incomeSource);
        assertThat(source.getSourceType()).isEqualTo(SourceType.USABLE);
        assertThat(source.getCurrencyType()).isEqualTo(CurrencyType.MONEY);
        assertThat(source.getBalance()).isEqualTo(BALANCE);
    }

    @Test
    @DisplayName("the all-args constructor keeps every field in order")
    void givenAllArgs_whenConstruct_shouldKeepEveryField() {
        var incomeSource = IncomeSource.builder().id(ID).build();

        var source = new Source(incomeSource, SourceType.UNUSABLE, CurrencyType.DIGITAL, BALANCE);

        assertThat(source.getIncomeSource()).isSameAs(incomeSource);
        assertThat(source.getSourceType()).isEqualTo(SourceType.UNUSABLE);
        assertThat(source.getCurrencyType()).isEqualTo(CurrencyType.DIGITAL);
        assertThat(source.getBalance()).isEqualTo(BALANCE);
    }

    @Test
    @DisplayName("setters store each field")
    void givenValues_whenSet_shouldReturnThemFromGetters() {
        var incomeSource = IncomeSource.builder().id(ID).build();
        var source = new Source();

        source.setIncomeSource(incomeSource);
        source.setSourceType(SourceType.USABLE);
        source.setCurrencyType(CurrencyType.DIGITAL);
        source.setBalance(BALANCE);

        assertThat(source.getIncomeSource()).isSameAs(incomeSource);
        assertThat(source.getSourceType()).isEqualTo(SourceType.USABLE);
        assertThat(source.getCurrencyType()).isEqualTo(CurrencyType.DIGITAL);
        assertThat(source.getBalance()).isEqualTo(BALANCE);
    }

    @Test
    @DisplayName("equality follows the id")
    void givenIds_whenEqualsAndHashCode_shouldFollowId() {
        var source = Source.builder().id(ID).build();

        assertThat(source).isEqualTo(Source.builder().id(ID).build()).hasSameHashCodeAs(Source.builder().id(ID).build());
        assertThat(source).isNotEqualTo(Source.builder().id(OTHER_ID).build())
                .doesNotHaveSameHashCodeAs(Source.builder().id(OTHER_ID).build());
    }
}
