package com.onurbcd.cli.dto.incomesource;

import com.onurbcd.cli.dto.PrimeDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestFactory.createIncomeSourceDto;
import static org.assertj.core.api.Assertions.assertThat;

class IncomeSourceDtoTest {

    @Test
    @DisplayName("is a PrimeDto that keeps the inherited fields")
    void givenFactoryIncomeSource_shouldExposeInheritedFields() {
        var incomeSource = createIncomeSourceDto();

        assertThat(incomeSource).isInstanceOf(PrimeDto.class);
        assertThat(incomeSource.getId()).isEqualTo(ID);
        assertThat(incomeSource.getName()).isEqualTo("bogus");
        assertThat(incomeSource.isActive()).isFalse();
    }
}
