package com.onurbcd.cli.config;

import org.jline.utils.AttributedStyle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PromptConfigTest {

    @Nested
    @DisplayName("getPrompt")
    class GetPrompt {

        @Test
        @DisplayName("should return the blue eru-shell prompt")
        void whenGetPrompt_shouldReturnBluePrompt() {
            var result = new PromptConfig().getPrompt();

            assertThat(result).hasToString("eru-shell:> ");
            assertThat(result.styleAt(0)).isEqualTo(AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE));
            assertThat(result.styleAt(result.length() - 1))
                    .isEqualTo(AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE));
        }
    }
}
