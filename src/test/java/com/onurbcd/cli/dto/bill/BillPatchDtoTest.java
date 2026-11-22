package com.onurbcd.cli.dto.bill;

import com.onurbcd.cli.dto.PrimePatchDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BillPatchDtoTest {

    @Test
    @DisplayName("the builder carries the active flag")
    void givenActive_whenBuild_shouldCarryFlag() {
        var dto = BillPatchDto.builder().active(false).build();

        assertThat(dto).isInstanceOf(PrimePatchDto.class);
        assertThat(dto.isActive()).isFalse();
    }
}
