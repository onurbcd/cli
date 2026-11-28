package com.onurbcd.cli.config.property;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;

class AdminPathsValidatorTest {

    private static final String STORAGE_VAR = AdminPathsValidator.STORAGE_VAR;
    private static final String FILES_VAR = AdminPathsValidator.FILES_VAR;

    @TempDir
    Path root;

    private static AdminPathsValidator validator(String storage, String files) {
        var properties = new AdminProperties();
        properties.setStoragePath(storage);
        properties.setFilesPath(files);
        return new AdminPathsValidator(properties);
    }

    private Path directory(String name) throws IOException {
        return Files.createDirectory(root.resolve(name));
    }

    private static void assumeNotPrivileged() {
        assumeThat(System.getProperty("user.name")).isNotEqualTo("root");
    }

    private static void chmod(Path path, String permissions) throws IOException {
        Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(permissions));
    }

    @Nested
    @DisplayName("afterPropertiesSet")
    class AfterPropertiesSet {

        @Test
        @DisplayName("should accept an existing storage and files directory")
        void givenValidDirectories_whenAfterPropertiesSet_shouldPass() throws IOException {
            var storage = directory("storage");
            var files = directory("files");

            assertThatCode(() -> validator(storage.toString(), files.toString()).afterPropertiesSet())
                    .doesNotThrowAnyException();
            assertThat(storage).isDirectory();
        }

        @Test
        @DisplayName("should create the missing storage directory, including parents")
        void givenMissingStorage_whenAfterPropertiesSet_shouldCreateIt() throws IOException {
            var storage = root.resolve("a").resolve("b").resolve("storage");
            var files = directory("files");

            validator(storage.toString(), files.toString()).afterPropertiesSet();

            assertThat(storage).isDirectory();
        }

        @Test
        @DisplayName("should normalize the configured paths")
        void givenNonNormalizedPaths_whenAfterPropertiesSet_shouldPass() throws IOException {
            var storage = directory("storage");
            var files = directory("files");

            assertThatCode(() -> validator(storage + "/../storage", files + "/.").afterPropertiesSet())
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("should reject a null or blank storage path")
        void givenNullOrBlankStorage_whenAfterPropertiesSet_shouldFail(String storage) throws IOException {
            var files = directory("files").toString();

            assertThatThrownBy(() -> validator(storage, files).afterPropertiesSet())
                    .isExactlyInstanceOf(IllegalStateException.class)
                    .hasMessage(STORAGE_VAR + " must be set (see .env.example)");
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("should reject a null or blank files path")
        void givenNullOrBlankFiles_whenAfterPropertiesSet_shouldFail(String files) throws IOException {
            var storage = directory("storage").toString();

            assertThatThrownBy(() -> validator(storage, files).afterPropertiesSet())
                    .isExactlyInstanceOf(IllegalStateException.class)
                    .hasMessage(FILES_VAR + " must be set (see .env.example)");
        }

        @Test
        @DisplayName("should reject a storage path with an invalid character")
        void givenInvalidStorage_whenAfterPropertiesSet_shouldFail() {
            assertThatThrownBy(() -> validator("/bad\0path", root.toString()).afterPropertiesSet())
                    .isExactlyInstanceOf(IllegalStateException.class)
                    .hasMessage(STORAGE_VAR + " is not a valid path: /bad\0path")
                    .hasCauseInstanceOf(java.nio.file.InvalidPathException.class);
        }

        @Test
        @DisplayName("should reject a relative storage path")
        void givenRelativeStorage_whenAfterPropertiesSet_shouldFail() {
            assertThatThrownBy(() -> validator("storage", root.toString()).afterPropertiesSet())
                    .isExactlyInstanceOf(IllegalStateException.class)
                    .hasMessage(STORAGE_VAR + " must be an absolute path: storage");
        }

        @Test
        @DisplayName("should reject a relative files path")
        void givenRelativeFiles_whenAfterPropertiesSet_shouldFail() throws IOException {
            var storage = directory("storage").toString();

            assertThatThrownBy(() -> validator(storage, "files").afterPropertiesSet())
                    .isExactlyInstanceOf(IllegalStateException.class)
                    .hasMessage(FILES_VAR + " must be an absolute path: files");
        }

        @Test
        @DisplayName("should reject a storage path that is a regular file")
        void givenStorageIsFile_whenAfterPropertiesSet_shouldFail() throws IOException {
            var storage = Files.createFile(root.resolve("storage"));

            assertThatThrownBy(() -> validator(storage.toString(), root.toString()).afterPropertiesSet())
                    .isExactlyInstanceOf(IllegalStateException.class)
                    .hasMessage(STORAGE_VAR + " cannot be created: " + storage)
                    .hasCauseInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("should reject a missing files directory")
        void givenMissingFiles_whenAfterPropertiesSet_shouldFail() throws IOException {
            var storage = directory("storage").toString();
            var files = root.resolve("missing");

            assertThatThrownBy(() -> validator(storage, files.toString()).afterPropertiesSet())
                    .isExactlyInstanceOf(IllegalStateException.class)
                    .hasMessage(FILES_VAR + " is not an existing directory: " + files);
            assertThat(files).doesNotExist();
        }

        @Test
        @DisplayName("should reject a files path that is a regular file")
        void givenFilesIsFile_whenAfterPropertiesSet_shouldFail() throws IOException {
            var storage = directory("storage").toString();
            var files = Files.createFile(root.resolve("files"));

            assertThatThrownBy(() -> validator(storage, files.toString()).afterPropertiesSet())
                    .isExactlyInstanceOf(IllegalStateException.class)
                    .hasMessage(FILES_VAR + " is not an existing directory: " + files);
        }

        @Test
        @DisplayName("should reject an unreadable files directory")
        void givenUnreadableFiles_whenAfterPropertiesSet_shouldFail() throws IOException {
            assumeNotPrivileged();
            var storage = directory("storage").toString();
            var files = directory("files");
            chmod(files, "---------");

            try {
                assertThatThrownBy(() -> validator(storage, files.toString()).afterPropertiesSet())
                        .isExactlyInstanceOf(IllegalStateException.class)
                        .hasMessage(FILES_VAR + " is not readable: " + files);
            } finally {
                chmod(files, "rwx------");
            }
        }

        @Test
        @DisplayName("should reject a read-only storage directory")
        void givenReadOnlyStorage_whenAfterPropertiesSet_shouldFail() throws IOException {
            assumeNotPrivileged();
            var storage = directory("storage");
            var files = directory("files").toString();
            chmod(storage, "r-x------");

            try {
                assertThatThrownBy(() -> validator(storage.toString(), files).afterPropertiesSet())
                        .isExactlyInstanceOf(IllegalStateException.class)
                        .hasMessage(STORAGE_VAR + " is not readable and writable: " + storage);
            } finally {
                chmod(storage, "rwx------");
            }
        }

        @Test
        @DisplayName("should reject an unreadable storage directory")
        void givenUnreadableStorage_whenAfterPropertiesSet_shouldFail() throws IOException {
            assumeNotPrivileged();
            var storage = directory("storage");
            var files = directory("files").toString();
            chmod(storage, "-wx------");

            try {
                assertThatThrownBy(() -> validator(storage.toString(), files).afterPropertiesSet())
                        .isExactlyInstanceOf(IllegalStateException.class)
                        .hasMessage(STORAGE_VAR + " is not readable and writable: " + storage);
            } finally {
                chmod(storage, "rwx------");
            }
        }
    }
}
