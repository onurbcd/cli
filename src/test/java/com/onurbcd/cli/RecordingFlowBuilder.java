package com.onurbcd.cli;

import org.mockito.invocation.InvocationOnMock;
import org.springframework.shell.jline.tui.component.flow.ComponentFlow;
import org.springframework.shell.jline.tui.component.flow.ConfirmationInputSpec;
import org.springframework.shell.jline.tui.component.flow.MultiItemSelectorSpec;
import org.springframework.shell.jline.tui.component.flow.SingleItemSelectorSpec;
import org.springframework.shell.jline.tui.component.flow.StringInputSpec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Fake of the fluent Spring Shell {@link ComponentFlow.Builder} that records every step added to the flow and every
 * setting applied to it, so tests can assert the whole flow instead of stubbing each fluent call.
 */
public final class RecordingFlowBuilder {

    public record Step(String kind, String id, Map<String, Object> settings) {
    }

    private final List<Step> steps = new ArrayList<>();
    private final ComponentFlow.Builder builder = mock(ComponentFlow.Builder.class, this::answerBuilder);
    private final ComponentFlow flow = mock(ComponentFlow.class);
    private final ComponentFlow.ComponentFlowResult result = mock(ComponentFlow.ComponentFlowResult.class);

    public RecordingFlowBuilder() {
        when(flow.run()).thenReturn(result);
    }

    public ComponentFlow.Builder builder() {
        return builder;
    }

    public ComponentFlow flow() {
        return flow;
    }

    public ComponentFlow.ComponentFlowResult result() {
        return result;
    }

    public List<Step> steps() {
        return steps;
    }

    private Object answerBuilder(InvocationOnMock invocation) {
        return switch (invocation.getMethod().getName()) {
            case "clone", "reset" -> builder;
            case "withStringInput" -> spec(StringInputSpec.class, "string", invocation.getArgument(0));
            case "withSingleItemSelector" -> spec(SingleItemSelectorSpec.class, "single", invocation.getArgument(0));
            case "withMultiItemSelector" -> spec(MultiItemSelectorSpec.class, "multi", invocation.getArgument(0));
            case "withConfirmationInput" -> spec(ConfirmationInputSpec.class, "confirm", invocation.getArgument(0));
            case "build" -> flow;
            default -> throw new AssertionError("Unexpected builder call: " + invocation.getMethod());
        };
    }

    private <S> S spec(Class<S> type, String kind, String id) {
        var step = new Step(kind, id, new LinkedHashMap<>());
        steps.add(step);

        return mock(type, invocation -> {
            var name = invocation.getMethod().getName();

            if ("and".equals(name)) {
                return builder;
            }

            step.settings().put(name, invocation.getArgument(0));
            return invocation.getMock();
        });
    }
}
