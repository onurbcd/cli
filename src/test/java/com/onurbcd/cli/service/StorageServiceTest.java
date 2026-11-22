package com.onurbcd.cli.service;

import com.onurbcd.cli.config.property.AdminProperties;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.param.MultipartFile;
import com.onurbcd.cli.persistency.entity.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorageServiceTest {

    private static final String CONTENT = "file content";
    private static final String HASH = "0123456789abcdef0123456789abcdef";

    @TempDir
    private Path root;

    private StorageService service;
    private Document document;

    @BeforeEach
    void setUp() {
        var config = new AdminProperties();
        config.setStoragePath(root.toString());
        service = new StorageService(config);
        document = new Document();
        document.setName("report.pdf");
        document.setPath("balance/2024");
        document.setHash(HASH);
    }

    private MultipartFile upload(InputStream stream) {
        return MultipartFile.builder().originalFilename("report.pdf").contentType("application/pdf").size(12L)
                .inputStream(stream).build();
    }

    private Path expectedFile() {
        return root.resolve("balance/2024").resolve(HASH + ".pdf");
    }

    @Nested
    @DisplayName("saveFile")
    class SaveFile {

        @Test
        @DisplayName("should create the folders and store the content as <hash>.<extension>")
        void shouldStoreFile() {
            var stream = new ByteArrayInputStream(CONTENT.getBytes(StandardCharsets.UTF_8));

            service.saveFile(document, upload(stream));

            assertThat(expectedFile()).hasContent(CONTENT);
        }

        @Test
        @DisplayName("when the file already exists, should fail and keep the existing content")
        void givenExistingFile_shouldFailAndKeepContent() throws IOException {
            Files.createDirectories(expectedFile().getParent());
            Files.writeString(expectedFile(), "old");
            var stream = new ByteArrayInputStream(CONTENT.getBytes(StandardCharsets.UTF_8));
            var file = upload(stream);

            assertThatThrownBy(() -> service.saveFile(document, file))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.FILE_ALREADY_EXISTS);
                        assertThat(e.getMessage()).contains(expectedFile().toAbsolutePath().normalize().toString());
                    });

            assertThat(expectedFile()).hasContent("old");
        }

        @Test
        @DisplayName("when reading the upload fails, should translate the IOException to a storage save error")
        void givenUnreadableUpload_shouldThrowStorageFileSave() {
            var failure = new IOException("disk");
            var file = upload(new InputStream() {
                @Override
                public int read() throws IOException {
                    throw failure;
                }

                @Override
                public int read(byte[] buffer, int offset, int length) throws IOException {
                    throw failure;
                }
            });

            assertThatThrownBy(() -> service.saveFile(document, file))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.STORAGE_FILE_SAVE);
                        assertThat(e.getCause()).isSameAs(failure);
                    });
        }

        @Test
        @DisplayName("when the folder cannot be created, should translate the IOException to a storage save error")
        void givenBlockedFolder_shouldThrowStorageFileSave() throws IOException {
            Files.writeString(root.resolve("balance"), "a file where a folder should be");
            var file = upload(new ByteArrayInputStream(CONTENT.getBytes(StandardCharsets.UTF_8)));

            assertThatThrownBy(() -> service.saveFile(document, file))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.STORAGE_FILE_SAVE);
                        assertThat(e.getCause()).isInstanceOf(IOException.class);
                    });
        }
    }

    @Nested
    @DisplayName("deleteFile")
    class DeleteFile {

        @Test
        @DisplayName("when the file exists, should delete only that file")
        void givenFile_shouldDelete() throws IOException {
            Files.createDirectories(expectedFile().getParent());
            Files.writeString(expectedFile(), CONTENT);
            var sibling = expectedFile().resolveSibling("other.pdf");
            Files.writeString(sibling, CONTENT);

            service.deleteFile(document);

            assertThat(expectedFile()).doesNotExist();
            assertThat(sibling).exists();
        }

        @Test
        @DisplayName("when the file does not exist, should not throw")
        void givenMissingFile_shouldNotThrow() {
            assertThatCode(() -> service.deleteFile(document)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("when the file cannot be deleted, should translate the IOException to a storage delete error")
        void givenUndeletableFile_shouldThrowStorageFileDelete() throws IOException {
            Files.createDirectories(expectedFile());
            Files.writeString(expectedFile().resolve("child.txt"), CONTENT);

            assertThatThrownBy(() -> service.deleteFile(document))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.STORAGE_FILE_DELETE);
                        assertThat(e.getCause()).isInstanceOf(IOException.class);
                    });
        }
    }
}
