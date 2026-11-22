package com.onurbcd.cli.dto.category;

import com.onurbcd.cli.dto.PrimeDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static com.onurbcd.cli.TestFactory.createCategoryDto;
import static org.assertj.core.api.Assertions.assertThat;

class CategoryDtoTest {

    @Test
    @DisplayName("is a PrimeDto and exposes the fields set through the setters")
    void givenFactoryCategory_shouldExposeFields() {
        var category = createCategoryDto();
        category.setLevel((short) 2);
        category.setLastBranch(true);

        assertThat(category).isInstanceOf(PrimeDto.class);
        assertThat(category.getParentId()).isEqualTo(PARENT_ID);
        assertThat(category.getParentName()).isEqualTo("parent");
        assertThat(category.getLevel()).isEqualTo((short) 2);
        assertThat(category.getLastBranch()).isTrue();
        assertThat(category.getDescription()).isEqualTo("description");
    }
}
