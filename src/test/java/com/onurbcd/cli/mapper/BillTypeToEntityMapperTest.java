package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.billtype.BillTypeSaveDto;
import com.onurbcd.cli.persistency.entity.BillType;
import com.onurbcd.cli.persistency.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.onurbcd.cli.TestConstants.CATEGORY_ID;
import static org.assertj.core.api.Assertions.assertThat;

class BillTypeToEntityMapperTest {

    private final BillTypeToEntityMapperImpl mapper = new BillTypeToEntityMapperImpl();

    private static BillTypeSaveDto createSaveDto() {
        return BillTypeSaveDto.builder()
                .name("water")
                .active(true)
                .path("bills/water")
                .categoryId(CATEGORY_ID)
                .build();
    }

    @Nested
    @DisplayName("apply")
    class Apply {

        @Test
        @DisplayName("when dto is complete, should map the fields and the category id")
        void givenFullDto_whenApply_shouldMapEveryField() {
            var dto = createSaveDto();

            var result = mapper.apply(dto);

            var expected = BillType.builder()
                    .name("water")
                    .active(true)
                    .path("bills/water")
                    .category(Category.builder().id(CATEGORY_ID).build())
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
            assertThat(dto).usingRecursiveComparison().isEqualTo(createSaveDto());
        }

        @Test
        @DisplayName("when dto is null, should return an empty entity")
        void givenNullDto_whenApply_shouldReturnEmptyEntity() {
            var result = mapper.apply(null);

            assertThat(result).usingRecursiveComparison().isEqualTo(BillType.builder().build());
        }
    }

    @Test
    void givenNullDto_whenMapCategory_shouldReturnEmptyCategory() {
        var result = mapper.billTypeSaveDtoToCategory(null);

        assertThat(result).usingRecursiveComparison().isEqualTo(Category.builder().build());
    }
}
