package com.onurbcd.cli.util;

import com.onurbcd.cli.dto.document.DocumentDto;
import com.onurbcd.cli.enums.Error;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.model.MultipartFile;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.SelectItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.onurbcd.cli.validator.Action.checkIf;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtil {

    public static List<SelectItem> getFiles(String pathName) {
        var path = Path.of(pathName);

        checkIf(Files.exists(path) && Files.isDirectory(path))
                .orElseThrow(Error.FOLDER_DOES_NOT_EXIST, pathName);

        var files = new File(pathName)
                .listFiles(File::isFile);

        return Optional.ofNullable(files)
                .stream()
                .flatMap(Arrays::stream)
                .map(file -> SelectItem.of(file.getName(), file.getPath()))
                .toList();
    }

    public static List<MultipartFile> filesToMultipartFiles(@Nullable List<String> filesPaths) {
        if (filesPaths == null || filesPaths.isEmpty()) {
            return Collections.emptyList();
        }

        return filesPaths.stream()
                .map(filePath -> {
                    try {
                        return fileToMultipartFile(filePath);
                    } catch (IOException e) {
                        throw new ApiException(Error.CONVERTING_TO_MULTIPART_FILE, e);
                    }
                })
                .toList();
    }

    public static String getHyperlink(DocumentDto document) {
        var extension = org.springframework.util.StringUtils.getFilenameExtension(document.getName());
        var fileNameBuilder = new StringBuilder();

        fileNameBuilder
                .append(document.getStoragePath())
                .append("/")
                .append(document.getPath())
                .append("/")
                .append(document.getHash());

        if (extension != null && !extension.isEmpty()) {
            fileNameBuilder.append('.').append(extension);
        }

        return "file://" + fileNameBuilder;
    }

    private static MultipartFile fileToMultipartFile(String filePath) throws IOException {
        var path = Path.of(filePath);
        var fileName = path.getFileName().toString();
        var contentType = Files.probeContentType(path);

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        var size = Files.size(path);
        var inputStream = Files.newInputStream(path);

        return MultipartFile
                .builder()
                .originalFilename(fileName)
                .contentType(contentType)
                .size(size)
                .inputStream(inputStream)
                .build();
    }
}
