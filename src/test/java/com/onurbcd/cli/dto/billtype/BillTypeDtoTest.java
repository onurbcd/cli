package com.onurbcd.cli.dto.billtype;

import com.onurbcd.cli.dto.PrimeDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.onurbcd.cli.TestConstants.CATEGORY_ID;
import static com.onurbcd.cli.TestFactory.createBillTypeDto;
import static org.assertj.core.api.Assertions.assertThat;

class BillTypeDtoTest {

    @Test
    @DisplayName("is a PrimeDto and exposes the fields set through the setters")
    void givenFactoryBillType_shouldExposeFields() {
        var billType = createBillTypeDto();

        assertThat(billType).isInstanceOf(PrimeDto.class);
        assertThat(billType.getPath()).isEqualTo("bills/water");
        assertThat(billType.getCategoryId()).isEqualTo(CATEGORY_ID);
        assertThat(billType.getCategoryName()).isEqualTo("category");
    }
}
