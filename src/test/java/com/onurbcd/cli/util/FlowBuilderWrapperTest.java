package com.onurbcd.cli.util;

import com.onurbcd.cli.RecordingFlowBuilder;
import com.onurbcd.cli.RecordingFlowBuilder.Step;
import com.onurbcd.cli.enums.FlowField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FlowBuilderWrapperTest {

    private static final FlowField FIELD = FlowField.NAME;
    private static final List<SelectItem> ITEMS = List.of(SelectItem.of("one", "1"), SelectItem.of("two", "2"));

    private RecordingFlowBuilder recording;
    private FlowBuilderWrapper wrapper;

    @BeforeEach
    void setUp() {
        recording = new RecordingFlowBuilder();
        wrapper = FlowBuilderWrapper.init(recording.builder());
    }

    @Test
    @DisplayName("init starts from a cloned and reset builder, without any step")
    void givenBuilder_whenInit_shouldStartEmpty() {
        assertThat(recording.steps()).isEmpty();
    }

    @Nested
    @DisplayName("input")
    class Input {

        @Test
        @DisplayName("without default value, should add a string input with only the name")
        void givenNullDefault_shouldAddInputWithoutDefault() {
            var result = wrapper.input(FIELD, null);

            assertThat(result).isSameAs(wrapper);
            assertThat(recording.steps()).containsExactly(
                    new Step("string", FIELD.getId(), Map.of("name", FIELD.getName())));
        }

        @Test
        @DisplayName("with default value, should add a string input with the default")
        void givenDefault_shouldAddInputWithDefault() {
            var result = wrapper.input(FIELD, "value");

            assertThat(result).isSameAs(wrapper);
            assertThat(recording.steps()).containsExactly(
                    new Step("string", FIELD.getId(), Map.of("name", FIELD.getName(), "defaultValue", "value")));
        }

        @Test
        @DisplayName("the short form behaves as a null default")
        void givenField_shouldAddInputWithoutDefault() {
            var result = wrapper.input(FIELD);

            assertThat(result).isSameAs(wrapper);
            assertThat(recording.steps()).containsExactly(
                    new Step("string", FIELD.getId(), Map.of("name", FIELD.getName())));
        }
    }

    @Test
    @DisplayName("secretInput adds a masked string input without any default value")
    void givenField_whenSecretInput_shouldAddMaskedInputWithoutDefault() {
        var result = wrapper.secretInput(FIELD);

        assertThat(result).isSameAs(wrapper);
        assertThat(recording.steps()).containsExactly(
                new Step("string", FIELD.getId(), Map.of("name", FIELD.getName(), "maskCharacter", '*')));
    }

    @Nested
    @DisplayName("select")
    class Select {

        @Test
        @DisplayName("without default value, should add a single selector sized to the items")
        void givenNullDefault_shouldAddSelectorWithoutDefault() {
            var result = wrapper.select(FIELD, ITEMS, null);

            assertThat(result).isSameAs(wrapper);
            assertThat(recording.steps()).containsExactly(new Step("single", FIELD.getId(),
                    Map.of("name", FIELD.getName(), "selectItems", ITEMS, "max", 2)));
        }

        @Test
        @DisplayName("with default value, should add a single selector with the default selection")
        void givenDefault_shouldAddSelectorWithDefault() {
            var result = wrapper.select(FIELD, ITEMS, "2");

            assertThat(result).isSameAs(wrapper);
            assertThat(recording.steps()).containsExactly(new Step("single", FIELD.getId(),
                    Map.of("name", FIELD.getName(), "selectItems", ITEMS, "defaultSelect", "2", "max", 2)));
        }

        @Test
        @DisplayName("the short form behaves as a null default")
        void givenItems_shouldAddSelectorWithoutDefault() {
            var result = wrapper.select(FIELD, ITEMS);

            assertThat(result).isSameAs(wrapper);
            assertThat(recording.steps()).containsExactly(new Step("single", FIELD.getId(),
                    Map.of("name", FIELD.getName(), "selectItems", ITEMS, "max", 2)));
        }
    }

    @Nested
    @DisplayName("multiSelect")
    class MultiSelect {

        @Test
        @DisplayName("with default values, should add a multi selector preselecting them")
        void givenDefaults_shouldAddMultiSelector() {
            var defaults = List.of("1");

            var result = wrapper.multiSelect(FIELD, ITEMS, defaults);

            assertThat(result).isSameAs(wrapper);
            assertThat(recording.steps()).containsExactly(new Step("multi", FIELD.getId(),
                    Map.of("name", FIELD.getName(), "selectItems", ITEMS, "resultValues", defaults, "max", 2)));
        }

        @Test
        @DisplayName("the short form preselects nothing")
        void givenItems_shouldAddMultiSelectorWithEmptyDefaults() {
            var result = wrapper.multiSelect(FIELD, ITEMS);

            assertThat(result).isSameAs(wrapper);
            assertThat(recording.steps()).containsExactly(new Step("multi", FIELD.getId(),
                    Map.of("name", FIELD.getName(), "selectItems", ITEMS, "resultValues", List.of(), "max", 2)));
        }
    }

    @Test
    @DisplayName("confirm adds a confirmation input with the default answer")
    void givenDefault_whenConfirm_shouldAddConfirmation() {
        var result = wrapper.confirm(FIELD, false);

        assertThat(result).isSameAs(wrapper);
        assertThat(recording.steps()).containsExactly(
                new Step("confirm", FIELD.getId(), Map.of("name", FIELD.getName(), "defaultValue", false)));
    }

    @Test
    @DisplayName("execute builds and runs the flow, returning its result")
    void givenSteps_whenExecute_shouldRunFlow() {
        var result = wrapper.input(FIELD).execute();

        assertThat(result).isSameAs(recording.result());
    }
}
