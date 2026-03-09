package com.onurbcd.eruservice.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.shell.component.flow.ComponentFlow;

import java.io.IOError;
import java.io.InterruptedIOException;
import java.util.List;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlowUtil {

    /**
     * Executes a ComponentFlow and handles user interruption (Ctrl+C) gracefully.
     *
     * @param flowSupplier Supplier that builds and runs the flow
     * @return ComponentFlow.ComponentFlowResult if successful, null if cancelled by
     * user
     */
    @Nullable
    public static ComponentFlow.ComponentFlowResult runFlowSafely(
            Supplier<ComponentFlow.ComponentFlowResult> flowSupplier) {

        try {
            return flowSupplier.get();
        } catch (Exception | IOError e) {
            if (e.getCause() instanceof InterruptedIOException) {
                return null;
            }

            throw e;
        }
    }

    @Nullable
    public static String getString(ComponentContext<?> context, String key) {
        return context.containsKey(key) ? context.get(key, String.class) : null;
    }

    @Nullable
    public static List<String> getStringList(ComponentContext<?> context, String key) {
        return context.containsKey(key) ? context.get(key) : null;
    }
}
