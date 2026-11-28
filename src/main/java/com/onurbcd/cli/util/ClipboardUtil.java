package com.onurbcd.cli.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Copies text to the user's clipboard through the terminal itself (OSC 52), so the text is never printed and no
 * external tool such as {@code wl-copy} or {@code xclip} is needed. The terminal must support OSC 52.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClipboardUtil {

    private static final String OSC_52_PREFIX = "\u001b]52;c;";
    private static final String BELL = "\u0007";

    public static String osc52Copy(String text) {
        return OSC_52_PREFIX + Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8)) + BELL;
    }
}
