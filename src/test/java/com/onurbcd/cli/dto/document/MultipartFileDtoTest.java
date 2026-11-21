package com.onurbcd.cli.dto.document;

import com.onurbcd.cli.param.MultipartFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MultipartFileDtoTest {

    @Test
    @DisplayName("the builder stores every field")
    void givenBuilder_shouldStoreFields() {
        var file = new MultipartFile();
        var files = new MultipartFile[] {file};

        var dto = MultipartFileDto.builder().path("p").multipartFiles(files).name("n").multipartFile(file).build();

        assertThat(dto.getPath()).isEqualTo("p");
        assertThat(dto.getMultipartFiles()).containsExactly(file);
        assertThat(dto.getName()).isEqualTo("n");
        assertThat(dto.getMultipartFile()).isSameAs(file);
    }

    @Test
    @DisplayName("the all-args constructor keeps the declaration order")
    void givenAllArgs_shouldKeepOrder() {
        var file = new MultipartFile();

        var dto = new MultipartFileDto("p", new MultipartFile[] {file}, "n", file);

        assertThat(dto.getPath()).isEqualTo("p");
        assertThat(dto.getName()).isEqualTo("n");
    }

    @Test
    @DisplayName("the no-args constructor and setters work")
    void givenNoArgs_whenSet_shouldExposeValues() {
        var file = new MultipartFile();
        var dto = new MultipartFileDto();
        assertThat(dto.getPath()).isNull();

        dto.setPath("p");
        dto.setName("n");
        dto.setMultipartFile(file);
        dto.setMultipartFiles(new MultipartFile[] {file});

        assertThat(dto.getPath()).isEqualTo("p");
        assertThat(dto.getName()).isEqualTo("n");
        assertThat(dto.getMultipartFile()).isSameAs(file);
        assertThat(dto.getMultipartFiles()).containsExactly(file);
    }
}
