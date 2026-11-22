package com.onurbcd.cli.persistency.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;

class IncomeSourceTest {

    private static final UUID OTHER_ID = UUID.fromString("99999999-9999-9999-0000-000000000001");

    @Test
    @DisplayName("the builder fills the inherited fields")
    void givenBuilder_whenBuild_shouldFillInheritedFields() {
        var incomeSource = IncomeSource.builder().id(ID).name("salary").active(true).build();

        assertThat(incomeSource.getId()).isEqualTo(ID);
        assertThat(incomeSource.getName()).isEqualTo("salary");
        assertThat(incomeSource.getActive()).isTrue();
    }

    @Test
    @DisplayName("equality follows the id")
    void givenIds_whenEqualsAndHashCode_shouldFollowId() {
        var incomeSource = IncomeSource.builder().id(ID).build();

        assertThat(incomeSource).isEqualTo(IncomeSource.builder().id(ID).build())
                .hasSameHashCodeAs(IncomeSource.builder().id(ID).build());
        assertThat(incomeSource).isNotEqualTo(IncomeSource.builder().id(OTHER_ID).build())
                .doesNotHaveSameHashCodeAs(IncomeSource.builder().id(OTHER_ID).build());
    }
}
