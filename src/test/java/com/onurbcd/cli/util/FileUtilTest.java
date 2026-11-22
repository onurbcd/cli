package com.onurbcd.cli.util;

import com.onurbcd.cli.dto.document.DocumentDto;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class FileUtilTest {

    private static final String CONTENT = "hello";

    @TempDir
    private Path root;

    private Path textFile() throws IOException {
        return Files.writeString(root.resolve("note.txt"), CONTENT);
    }

    private static DocumentDto document(String name) {
        var document = new DocumentDto();
        document.setName(name);
        document.setStoragePath("/data");
        document.setPath("balance/2024");
        document.setHash("abc123");
        return document;
    }

    private static Stream<Arguments> hyperlinks() {
        return Stream.of(
                Arguments.of("invoice.pdf", "file:///data/balance/2024/abc123.pdf"),
                Arguments.of("archive.tar.gz", "file:///data/balance/2024/abc123.gz"),
                Arguments.of("noextension", "file:///data/balance/2024/abc123"),
                Arguments.of("trailingdot.", "file:///data/balance/2024/abc123"),
                Arguments.of(null, "file:///data/balance/2024/abc123"));
    }

    @Nested
    @DisplayName("getFiles")
    class GetFiles {

        @Test
        @DisplayName("should list only the regular files of the folder as name/path items")
        void givenFolder_shouldListOnlyFiles() throws IOException {
            var file = Files.writeString(root.resolve("a.txt"), CONTENT);
            Files.createDirectory(root.resolve("sub"));
            Files.writeString(root.resolve("sub").resolve("nested.txt"), CONTENT);

            var result = FileUtil.getFiles(root.toString());

            assertThat(result)
                    .extracting(SelectItem::name, SelectItem::item)
                    .containsExactly(tuple("a.txt", file.toString()));
        }

        @Test
        @DisplayName("an empty folder yields no items")
        void givenEmptyFolder_shouldReturnEmpty() {
            assertThat(FileUtil.getFiles(root.toString())).isEmpty();
        }

        @Test
        @DisplayName("a missing folder fails with FOLDER_DOES_NOT_EXIST")
        void givenMissingFolder_shouldThrow() {
            var missing = root.resolve("missing").toString();

            assertThatThrownBy(() -> FileUtil.getFiles(missing))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.FOLDER_DOES_NOT_EXIST);
                        assertThat(e.getMessage()).contains(missing);
                    });
        }

        @Test
        @DisplayName("a path that is a file, not a folder, fails with FOLDER_DOES_NOT_EXIST")
        void givenFile_shouldThrow() throws IOException {
            var file = textFile().toString();

            assertThatThrownBy(() -> FileUtil.getFiles(file))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.FOLDER_DOES_NOT_EXIST);
                        assertThat(e.getMessage()).contains(file);
                    });
        }
    }

    @Nested
    @DisplayName("fileToMultipartFile")
    class FileToMultipartFile {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t"})
        @DisplayName("a blank path returns null")
        void givenBlank_shouldReturnNull(String path) {
            assertThat(FileUtil.fileToMultipartFile(path)).isNull();
        }

        @Test
        @DisplayName("a known file type keeps its detected content type, name, size and content")
        void givenTextFile_shouldConvert() throws IOException {
            var path = textFile();

            var result = FileUtil.fileToMultipartFile(path.toString());

            assertThat(result.getOriginalFilename()).isEqualTo("note.txt");
            assertThat(result.getContentType()).isEqualTo("text/plain");
            assertThat(result.getSize()).isEqualTo(5L);
            try (var stream = result.getInputStream()) {
                assertThat(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo(CONTENT);
            }
        }

        @Test
        @DisplayName("an undetectable content type falls back to application/octet-stream")
        void givenUnknownType_shouldUseOctetStream() throws IOException {
            var path = Files.writeString(root.resolve("data.zzzunknown"), CONTENT);

            var result = FileUtil.fileToMultipartFile(path.toString());

            assertThat(result.getOriginalFilename()).isEqualTo("data.zzzunknown");
            assertThat(result.getContentType()).isEqualTo("application/octet-stream");
            assertThat(result.getSize()).isEqualTo(5L);
            result.getInputStream().close();
        }

        @Test
        @DisplayName("a missing file fails with CONVERTING_TO_MULTIPART_FILE caused by an IOException")
        void givenMissingFile_shouldThrow() {
            var missing = root.resolve("missing.txt").toString();

            assertThatThrownBy(() -> FileUtil.fileToMultipartFile(missing))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.CONVERTING_TO_MULTIPART_FILE);
                        assertThat(e.getCause()).isInstanceOf(IOException.class);
                    });
        }

        @Test
        @DisplayName("a path without a file name fails with CONVERTING_TO_MULTIPART_FILE")
        void givenPathWithoutFileName_shouldThrow() {
            assertThatThrownBy(() -> FileUtil.fileToMultipartFile("/"))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.CONVERTING_TO_MULTIPART_FILE);
                        assertThat(e.getCause()).isInstanceOf(IOException.class)
                                .hasMessage("The path has no file name: /");
                    });
        }
    }

    @Nested
    @DisplayName("filesToMultipartFiles")
    class FilesToMultipartFiles {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null or empty input returns an empty list")
        void givenNullOrEmpty_shouldReturnEmpty(List<String> paths) {
            assertThat(FileUtil.filesToMultipartFiles(paths)).isEmpty();
        }

        @Test
        @DisplayName("each path is converted in order, blank entries become null")
        void givenPaths_shouldConvertEach() throws IOException {
            var first = textFile().toString();
            var paths = new ArrayList<String>();
            paths.add(first);
            paths.add(" ");

            var result = FileUtil.filesToMultipartFiles(paths);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getOriginalFilename()).isEqualTo("note.txt");
            assertThat(result.get(1)).isNull();
            assertThat(paths).containsExactly(first, " ");
            result.get(0).getInputStream().close();
        }
    }

    @ParameterizedTest(name = "[{index}] name={0}")
    @MethodSource("hyperlinks")
    @DisplayName("getHyperlink builds a file URL from storage path, path, hash and extension")
    void givenDocument_whenGetHyperlink_shouldBuildUrl(String name, String expected) {
        assertThat(FileUtil.getHyperlink(document(name))).isEqualTo(expected);
    }
}
