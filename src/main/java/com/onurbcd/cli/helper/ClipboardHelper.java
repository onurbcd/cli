package com.onurbcd.cli.helper;

import com.onurbcd.cli.annotation.Helper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Copies text to the system clipboard with the first available tool ({@code wl-copy} on Wayland, {@code xclip} or
 * {@code xsel} on X11). The text goes through the tool's standard input, so it never shows up in the process list,
 * and it is never printed.
 */
@Helper
public class ClipboardHelper {

    private static final List<List<String>> TOOLS = List.of(
            List.of("wl-copy"),
            List.of("xclip", "-selection", "clipboard", "-in"),
            List.of("xsel", "--clipboard", "--input"));
    private static final long DEFAULT_TIMEOUT_MILLIS = 5000;

    private final List<Path> searchDirs;
    private final long timeoutMillis;

    public ClipboardHelper() {
        this(parsePath(System.getenv("PATH")));
    }

    /** Splits a PATH value, skipping empty entries so the current directory is never searched. */
    static List<Path> parsePath(String path) {
        if (path == null) {
            return List.of();
        }

        return Arrays.stream(path.split(File.pathSeparator))
                .filter(dir -> !dir.isEmpty())
                .map(Path::of)
                .toList();
    }

    ClipboardHelper(List<Path> searchDirs) {
        this(searchDirs, DEFAULT_TIMEOUT_MILLIS);
    }

    ClipboardHelper(List<Path> searchDirs, long timeoutMillis) {
        this.searchDirs = searchDirs;
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * Copies the text to the clipboard.
     *
     * @return {@code true} when a clipboard tool accepted the text, {@code false} when none is installed or all failed
     */
    public boolean copy(String text) {
        for (var tool : TOOLS) {
            var executable = find(tool.getFirst());

            if (executable != null && run(executable, tool.subList(1, tool.size()), text)) {
                return true;
            }
        }

        return false;
    }

    private Path find(String name) {
        return searchDirs.stream()
                .map(dir -> dir.resolve(name))
                .filter(Files::isExecutable)
                .findFirst()
                .orElse(null);
    }

    private boolean run(Path executable, List<String> args, String text) {
        var command = new ArrayList<String>();
        command.add(executable.toString());
        command.addAll(args);

        try {
            var process = new ProcessBuilder(command)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();

            try (var stdin = process.getOutputStream()) {
                stdin.write(text.getBytes(StandardCharsets.UTF_8));
            }

            if (!process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                return false;
            }

            return process.exitValue() == 0;
        } catch (IOException _) {
            return false;
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
