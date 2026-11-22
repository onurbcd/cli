package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.category.CategorySaveDto;
import com.onurbcd.cli.persistency.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static org.assertj.core.api.Assertions.assertThat;

class CategoryToEntityMapperTest {

    private final CategoryToEntityMapperImpl mapper = new CategoryToEntityMapperImpl();

    private static CategorySaveDto createSaveDto() {
        return CategorySaveDto.builder()
                .name("category")
                .active(true)
                .parentId(PARENT_ID)
                .description("description")
                .build();
    }

    @Nested
    @DisplayName("apply")
    class Apply {

        @Test
        @DisplayName("when dto is complete, should map the fields and the parent id")
        void givenFullDto_whenApply_shouldMapEveryField() {
            var dto = createSaveDto();

            var result = mapper.apply(dto);

            var expected = Category.builder()
                    .name("category")
                    .active(true)
                    .description("description")
                    .parent(Category.builder().id(PARENT_ID).build())
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
            assertThat(dto).usingRecursiveComparison().isEqualTo(createSaveDto());
        }

        @Test
        @DisplayName("when dto is null, should return an empty entity")
        void givenNullDto_whenApply_shouldReturnEmptyEntity() {
            var result = mapper.apply(null);

            assertThat(result).usingRecursiveComparison().isEqualTo(Category.builder().build());
        }
    }

    @Test
    void givenNullDto_whenMapParent_shouldReturnEmptyCategory() {
        var result = mapper.categorySaveDtoToCategory(null);

        assertThat(result).usingRecursiveComparison().isEqualTo(Category.builder().build());
    }
}
