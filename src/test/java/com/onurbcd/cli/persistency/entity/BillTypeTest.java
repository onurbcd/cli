package com.onurbcd.cli.persistency.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.onurbcd.cli.TestConstants.CATEGORY_ID;
import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;

class BillTypeTest {

    private static final UUID OTHER_ID = UUID.fromString("99999999-9999-9999-0000-000000000001");

    @Test
    @DisplayName("the builder fills every field")
    void givenBuilder_whenBuild_shouldFillEveryField() {
        var category = new Category(CATEGORY_ID);

        var billType = BillType.builder().id(ID).name("water").active(true).path("home/water").category(category).build();

        assertThat(billType)
                .returns(ID, BillType::getId)
                .returns("water", BillType::getName)
                .returns(true, BillType::getActive)
                .returns("home/water", BillType::getPath)
                .satisfies(a -> assertThat(a.getCategory()).isSameAs(category));
    }

    @Test
    @DisplayName("the all-args constructor keeps every field in order")
    void givenAllArgs_whenConstruct_shouldKeepEveryField() {
        var category = new Category(CATEGORY_ID);

        var billType = new BillType("home/light", category);

        assertThat(billType)
                .returns("home/light", BillType::getPath)
                .satisfies(a -> assertThat(a.getCategory()).isSameAs(category));
    }

    @Test
    @DisplayName("setters store each field")
    void givenValues_whenSet_shouldReturnThemFromGetters() {
        var category = new Category(CATEGORY_ID);
        var billType = new BillType();

        billType.setPath("home/gas");
        billType.setCategory(category);

        assertThat(billType)
                .returns("home/gas", BillType::getPath)
                .satisfies(a -> assertThat(a.getCategory()).isSameAs(category));
    }

    @Test
    @DisplayName("equality follows the id")
    void givenIds_whenEqualsAndHashCode_shouldFollowId() {
        var billType = BillType.builder().id(ID).build();

        assertThat(billType).isEqualTo(BillType.builder().id(ID).build())
                .hasSameHashCodeAs(BillType.builder().id(ID).build());
        assertThat(billType).isNotEqualTo(BillType.builder().id(OTHER_ID).build())
                .doesNotHaveSameHashCodeAs(BillType.builder().id(OTHER_ID).build());
    }
}
