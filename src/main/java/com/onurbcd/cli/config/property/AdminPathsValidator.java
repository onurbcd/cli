package com.onurbcd.cli.config.property;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Fails the application start-up with a clear message when the configured storage or files directory is missing,
 * relative or not accessible, instead of failing later in the middle of a command.
 */
@Component
@RequiredArgsConstructor
public class AdminPathsValidator implements InitializingBean {

    static final String STORAGE_VAR = "CLI_STORAGE_PATH";
    static final String FILES_VAR = "CLI_FILES_PATH";

    private final AdminProperties config;

    @Override
    public void afterPropertiesSet() {
        var storage = resolve(config.getStoragePath(), STORAGE_VAR);
        createIfMissing(storage);
        requireAccessible(storage, STORAGE_VAR, true);

        requireAccessible(resolve(config.getFilesPath(), FILES_VAR), FILES_VAR, false);
    }

    private static Path resolve(String value, String variable) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(variable + " must be set (see .env.example)");
        }

        Path path;

        try {
            path = Path.of(value);
        } catch (InvalidPathException e) {
            throw new IllegalStateException(variable + " is not a valid path: " + value, e);
        }

        if (!path.isAbsolute()) {
            throw new IllegalStateException(variable + " must be an absolute path: " + value);
        }

        return path.normalize();
    }

    private static void createIfMissing(Path storage) {
        try {
            Files.createDirectories(storage);
        } catch (IOException e) {
            throw new IllegalStateException(STORAGE_VAR + " cannot be created: " + storage, e);
        }
    }

    private static void requireAccessible(Path path, String variable, boolean writable) {
        if (!Files.isDirectory(path)) {
            throw new IllegalStateException(variable + " is not an existing directory: " + path);
        }

        if (!Files.isReadable(path) || (writable && !Files.isWritable(path))) {
            throw new IllegalStateException(variable + " is not " + (writable ? "readable and writable" : "readable")
                    + ": " + path);
        }
    }
}
