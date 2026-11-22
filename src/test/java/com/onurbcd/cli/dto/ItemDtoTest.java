package com.onurbcd.cli.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ItemDtoTest {

    @Test
    @DisplayName("the constructor stores id and name")
    void givenIdAndName_whenConstructed_shouldExposeThem() {
        var id = UUID.fromString("11111111-1111-1111-1111-111111111111");

        var dto = new ItemDto(id, "Item");

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getName()).isEqualTo("Item");
    }
}
