package com.onurbcd.cli.dto.billtype;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.onurbcd.cli.TestConstants.CATEGORY_ID;
import static org.assertj.core.api.Assertions.assertThat;

class BillTypeValuesDtoTest {

    @Test
    @DisplayName("the record exposes path and category id")
    void givenValues_shouldExposeComponents() {
        var dto = new BillTypeValuesDto("bills/water", CATEGORY_ID);

        assertThat(dto.path()).isEqualTo("bills/water");
        assertThat(dto.categoryId()).isEqualTo(CATEGORY_ID);
        assertThat(dto).isEqualTo(new BillTypeValuesDto("bills/water", CATEGORY_ID));
    }
}
