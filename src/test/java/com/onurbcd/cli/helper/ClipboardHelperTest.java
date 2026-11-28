package com.onurbcd.cli.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClipboardHelperTest {

    private static final String TEXT = "p@ss wörd";

    @TempDir
    private Path bin;
    @TempDir
    private Path other;

    private void fakeTool(Path dir, String name, String body) throws IOException {
        var script = dir.resolve(name);
        Files.writeString(script, "#!/bin/sh\n" + body + "\n", StandardCharsets.UTF_8);
        assertThat(script.toFile().setExecutable(true)).isTrue();
    }

    @Test
    @DisplayName("should send the text through stdin of the first available tool, not as an argument")
    void givenTool_whenCopy_shouldWriteStdinAndReturnTrue() throws IOException {
        var out = bin.resolve("out.txt");
        fakeTool(bin, "wl-copy", "echo \"args=$#\" > '" + out + ".args'; cat > '" + out + "'");

        var result = new ClipboardHelper(List.of(other, bin)).copy(TEXT);

        assertThat(result).isTrue();
        assertThat(Files.readString(out, StandardCharsets.UTF_8)).isEqualTo(TEXT);
        assertThat(Files.readString(bin.resolve("out.txt.args"), StandardCharsets.UTF_8).strip()).isEqualTo("args=0");
    }

    @Test
    @DisplayName("should use the next tool, with its arguments, when the previous one fails")
    void givenFailingTool_whenCopy_shouldFallBackToNextTool() throws IOException {
        var out = bin.resolve("out.txt");
        fakeTool(bin, "wl-copy", "cat > /dev/null; exit 1");
        fakeTool(bin, "xclip", "echo \"$@\" > '" + out + ".args'; cat > '" + out + "'");

        var result = new ClipboardHelper(List.of(bin)).copy(TEXT);

        assertThat(result).isTrue();
        assertThat(Files.readString(out, StandardCharsets.UTF_8)).isEqualTo(TEXT);
        assertThat(Files.readString(bin.resolve("out.txt.args"), StandardCharsets.UTF_8).strip())
                .isEqualTo("-selection clipboard -in");
    }

    @Test
    @DisplayName("should return false when no tool is installed")
    void givenNoTool_whenCopy_shouldReturnFalse() {
        assertThat(new ClipboardHelper(List.of(bin)).copy(TEXT)).isFalse();
    }

    @Test
    @DisplayName("should return false when every tool fails")
    void givenAllToolsFail_whenCopy_shouldReturnFalse() throws IOException {
        fakeTool(bin, "wl-copy", "cat > /dev/null; exit 1");
        fakeTool(bin, "xclip", "cat > /dev/null; exit 2");
        fakeTool(bin, "xsel", "cat > /dev/null; exit 3");

        assertThat(new ClipboardHelper(List.of(bin)).copy(TEXT)).isFalse();
    }

    @Test
    @DisplayName("should return false when the tool cannot be started")
    void givenUnstartableExecutable_whenCopy_shouldReturnFalse() throws IOException {
        Files.createDirectory(bin.resolve("wl-copy"));

        assertThat(new ClipboardHelper(List.of(bin)).copy(TEXT)).isFalse();
    }

    @Test
    @DisplayName("should kill the tool and return false when it does not finish in time")
    void givenSlowTool_whenCopy_shouldTimeOutAndReturnFalse() throws IOException {
        fakeTool(bin, "wl-copy", "cat > /dev/null; sleep 30");

        assertThat(new ClipboardHelper(List.of(bin), 200).copy(TEXT)).isFalse();
    }

    @Test
    @DisplayName("should keep the interrupt flag and return false when the thread is interrupted")
    void givenInterruptedThread_whenCopy_shouldReturnFalseAndKeepFlag() throws IOException {
        fakeTool(bin, "wl-copy", "cat > /dev/null; sleep 30");
        Thread.currentThread().interrupt();

        var result = new ClipboardHelper(List.of(bin), 60_000).copy(TEXT);

        assertThat(Thread.interrupted()).isTrue();
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("parsePath should split the entries and skip the empty ones")
    void givenPathWithEmptyEntries_whenParsePath_shouldSkipThem() {
        var path = "/usr/bin" + File.pathSeparator + File.pathSeparator + "/bin" + File.pathSeparator;

        assertThat(ClipboardHelper.parsePath(path)).containsExactly(Path.of("/usr/bin"), Path.of("/bin"));
    }

    @Test
    @DisplayName("parsePath should return no directory when PATH is not set")
    void givenNullPath_whenParsePath_shouldReturnEmpty() {
        assertThat(ClipboardHelper.parsePath(null)).isEmpty();
    }

    @Test
    @DisplayName("default constructor should build a helper from the PATH")
    void givenDefaultConstructor_shouldNotThrow() {
        assertThat(new ClipboardHelper()).isNotNull();
    }
}
