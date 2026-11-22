package com.onurbcd.cli.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.shell.jline.tui.component.context.ComponentContext;
import org.springframework.shell.jline.tui.component.flow.ComponentFlow;

import java.io.IOError;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class FlowUtilTest {

    private static final String KEY = "key";

    @Nested
    @DisplayName("runFlowSafely")
    class RunFlowSafely {

        @Test
        @DisplayName("should return the flow result")
        void givenSuccess_shouldReturnResult() {
            var result = mock(ComponentFlow.ComponentFlowResult.class);

            assertThat(FlowUtil.runFlowSafely(() -> result)).isSameAs(result);
        }

        @Test
        @DisplayName("when an exception is caused by an interrupted IO, should return null")
        void givenInterruptedException_shouldReturnNull() {
            Supplier<ComponentFlow.ComponentFlowResult> supplier = () -> {
                throw new IllegalStateException(new InterruptedIOException("ctrl+c"));
            };

            assertThat(FlowUtil.runFlowSafely(supplier)).isNull();
        }

        @Test
        @DisplayName("when an IOError is caused by an interrupted IO, should return null")
        void givenInterruptedError_shouldReturnNull() {
            Supplier<ComponentFlow.ComponentFlowResult> supplier = () -> {
                throw new IOError(new InterruptedIOException("ctrl+c"));
            };

            assertThat(FlowUtil.runFlowSafely(supplier)).isNull();
        }

        @Test
        @DisplayName("an exception with another cause is rethrown as is")
        void givenOtherCause_shouldRethrowException() {
            var failure = new IllegalStateException(new IOException("other"));

            assertThatThrownBy(() -> FlowUtil.runFlowSafely(() -> {
                throw failure;
            })).isSameAs(failure);
        }

        @Test
        @DisplayName("an exception without cause is rethrown as is")
        void givenNoCause_shouldRethrowException() {
            var failure = new IllegalArgumentException("boom");

            assertThatThrownBy(() -> FlowUtil.runFlowSafely(() -> {
                throw failure;
            })).isSameAs(failure);
        }

        @Test
        @DisplayName("an IOError with another cause is rethrown as is")
        void givenOtherErrorCause_shouldRethrowError() {
            var failure = new IOError(new IOException("other"));

            assertThatThrownBy(() -> FlowUtil.runFlowSafely(() -> {
                throw failure;
            })).isSameAs(failure);
        }
    }

    @Nested
    @DisplayName("context readers")
    class ContextReaders {

        @Test
        @DisplayName("getString returns the stored text or null when the key is missing")
        void givenContext_whenGetString_shouldReadOrNull() {
            ComponentContext<?> context = ComponentContext.empty();
            assertThat(FlowUtil.getString(context, KEY)).isNull();

            context.put(KEY, "text");

            assertThat(FlowUtil.getString(context, KEY)).isEqualTo("text");
            assertThat(FlowUtil.getString(context, "other")).isNull();
        }

        @Test
        @DisplayName("getStringList returns the stored list or null when the key is missing")
        void givenContext_whenGetStringList_shouldReadOrNull() {
            ComponentContext<?> context = ComponentContext.empty();
            assertThat(FlowUtil.getStringList(context, KEY)).isNull();
            var values = List.of("a", "b");

            context.put(KEY, values);

            assertThat(FlowUtil.getStringList(context, KEY)).isSameAs(values);
            assertThat(FlowUtil.getStringList(context, "other")).isNull();
        }

        @Test
        @DisplayName("getBoolean returns the stored flag or null when the key is missing")
        void givenContext_whenGetBoolean_shouldReadOrNull() {
            ComponentContext<?> context = ComponentContext.empty();
            assertThat(FlowUtil.getBoolean(context, KEY)).isNull();

            context.put(KEY, false);

            assertThat(FlowUtil.getBoolean(context, KEY)).isFalse();
            assertThat(FlowUtil.getBoolean(context, "other")).isNull();
        }
    }
}
