package com.onurbcd.eruservice.util;

import java.io.InterruptedIOException;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.ComponentFlow;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlowUtil {

    /**
     * Executes a ComponentFlow and handles user interruption (Ctrl+C) gracefully.
     * 
     * @param flowSupplier Supplier that builds and runs the flow
     * @return ComponentFlow.ComponentFlowResult if successful, null if cancelled by
     *         user
     */
    @Nullable
    public static ComponentFlow.ComponentFlowResult runFlowSafely(
            Supplier<ComponentFlow.ComponentFlowResult> flowSupplier) {

        try {
            return flowSupplier.get();
        } catch (Exception e) {
            if (e.getCause() instanceof InterruptedIOException) {
                return null;
            }

            throw e;
        } catch (java.io.IOError e) {
            if (e.getCause() instanceof InterruptedIOException) {
                return null;
            }

            throw e;
        }
    }
}
