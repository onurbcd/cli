package com.onurbcd.cli.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClipboardUtilTest {

    @Test
    @DisplayName("osc52Copy should wrap the Base64 of the UTF-8 text in an OSC 52 sequence")
    void givenText_whenOsc52Copy_shouldReturnSequence() {
        assertThat(ClipboardUtil.osc52Copy("pässword")).isEqualTo("\u001b]52;c;cMOkc3N3b3Jk\u0007");
    }

    @Test
    @DisplayName("osc52Copy should not contain the plain text")
    void givenText_whenOsc52Copy_shouldNotContainPlainText() {
        assertThat(ClipboardUtil.osc52Copy("password")).doesNotContain("password");
    }
}
