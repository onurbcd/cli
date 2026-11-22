package com.onurbcd.cli.param.flow;

import com.onurbcd.cli.dto.category.CategoryDto;
import com.onurbcd.cli.enums.FlowType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.util.List;

import static com.onurbcd.cli.TestFactory.createCategoryDto;
import static com.onurbcd.cli.TestFactory.createSelectItems;
import static org.assertj.core.api.Assertions.assertThat;

class CategorySaveFlowParamTest {

    private static final List<SelectItem> CATEGORIES = createSelectItems("category");

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("when category is complete, should convert its fields and keep the categories")
        void givenFullCategory_whenOf_shouldConvertEveryField() {
            var result = CategorySaveFlowParam.of(createCategoryDto(), SaveFlowParam.category(CATEGORIES));

            var expected = CategorySaveFlowParam.builder()
                    .name("bogus")
                    .description("description")
                    .items(CATEGORIES)
                    .parent("parent")
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
        }

        @Test
        @DisplayName("when category is null, should leave the fields null and keep the categories")
        void givenNullCategory_whenOf_shouldLeaveFieldsNull() {
            var result = CategorySaveFlowParam.of(null, SaveFlowParam.category(CATEGORIES));

            var expected = CategorySaveFlowParam.builder().items(CATEGORIES).build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
        }

        @Test
        @DisplayName("when category has no parent, should leave the parent null")
        void givenCategoryWithoutParent_whenOf_shouldLeaveParentNull() {
            var result = CategorySaveFlowParam.of(new CategoryDto(), SaveFlowParam.category(CATEGORIES));

            assertThat(result.getParent()).isNull();
        }
    }

    @Nested
    @DisplayName("getType")
    class GetType {

        @Test
        @DisplayName("should be CATEGORY")
        void whenGetType_shouldBeCategory() {
            assertThat(CategorySaveFlowParam.builder().build().getType()).isEqualTo(FlowType.CATEGORY);
        }
    }
}
