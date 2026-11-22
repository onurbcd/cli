package com.onurbcd.cli.factory;

import org.springframework.shell.jline.tui.component.flow.ComponentFlow;

import java.util.function.Supplier;

@FunctionalInterface
public interface FlowSupplier extends Supplier<ComponentFlow.ComponentFlowResult> {
}
