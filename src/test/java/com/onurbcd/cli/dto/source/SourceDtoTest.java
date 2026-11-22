package com.onurbcd.cli.dto.source;

import com.onurbcd.cli.dto.PrimeDto;
import com.onurbcd.cli.enums.CurrencyType;
import com.onurbcd.cli.enums.SourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.onurbcd.cli.TestFactory.createSourceDto;
import static org.assertj.core.api.Assertions.assertThat;

class SourceDtoTest {

    @Test
    @DisplayName("is a PrimeDto and exposes the fields set through the setters")
    void givenFactorySource_shouldExposeFields() {
        var source = createSourceDto();

        assertThat(source).isInstanceOf(PrimeDto.class);
        assertThat(source.getSourceType()).isEqualTo(SourceType.USABLE);
        assertThat(source.getCurrencyType()).isEqualTo(CurrencyType.DIGITAL);
        assertThat(source.getBalance()).isEqualTo(new BigDecimal("100.50"));
        assertThat(source.getIncomeSourceId()).isNotNull();
        assertThat(source.getIncomeSourceName()).isNotNull();
    }
}
