package com.onurbcd.eruservice.factory;

import org.springframework.shell.component.flow.ComponentFlow;

import java.util.function.Supplier;

@FunctionalInterface
public interface FlowSupplier extends Supplier<ComponentFlow.ComponentFlowResult> {
}
