package com.onurbcd.cli.command;

import com.onurbcd.cli.TestValidator;
import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.filter.SecretFilter;
import com.onurbcd.cli.dto.secret.SecretPatchDto;
import com.onurbcd.cli.dto.secret.SecretSaveDto;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.enums.FlowType;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.param.CommandParam;
import com.onurbcd.cli.param.flow.SaveFlowParam;
import com.onurbcd.cli.service.SecretService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.shell.jline.tui.component.context.ComponentContext;
import org.springframework.shell.jline.tui.component.flow.ComponentFlow;

import java.io.InterruptedIOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestFactory.createSecretDto;
import static com.onurbcd.cli.util.Constant.OPERATION_CANCELLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecretCommandTest {

    private static final UUID RETURNED_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");

    @Mock
    private SecretService secretService;
    @Mock
    private ShellHelper shellHelper;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ComponentFlow.Builder flowBuilder;
    @Captor
    private ArgumentCaptor<SecretSaveDto> saveDtoCaptor;
    @Captor
    private ArgumentCaptor<String> messageCaptor;

    private SecretCommand command;

    @BeforeEach
    void setUp() {
        command = new SecretCommand(secretService, flowBuilder, shellHelper, TestValidator.create());
    }

    private void stubFlowResult(ComponentContext<?> context) {
        var result = mock(ComponentFlow.ComponentFlowResult.class);
        doReturn(context).when(result).getContext();
        when(flowBuilder.clone()).thenReturn(flowBuilder);
        when(flowBuilder.reset()).thenReturn(flowBuilder);
        when(flowBuilder.build().run()).thenReturn(result);
    }

    private static ComponentContext<?> completeContext() {
        var context = ComponentContext.empty();
        context.put("name", "  Bank   Login ");
        context.put("description", "  Main   account ");
        context.put("link", " https://example.com/login ");
        context.put("username", "  john ");
        context.put("password", "  s3  cret ");
        return context;
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("when the flow is interrupted, should return the cancelled warning and not save")
        void givenInterruptedFlow_whenSave_shouldReturnCancelledWarning() {
            when(flowBuilder.clone()).thenReturn(flowBuilder);
            when(flowBuilder.reset()).thenReturn(flowBuilder);
            when(flowBuilder.build().run()).thenThrow(new RuntimeException(new InterruptedIOException()));
            when(shellHelper.warning(OPERATION_CANCELLED)).thenReturn("cancelled");

            var result = command.save(null);

            assertThat(result).isEqualTo("cancelled");
            verify(shellHelper).warning(OPERATION_CANCELLED);
            verifyNoMoreInteractions(shellHelper);
            verifyNoInteractions(secretService);
        }

        @Test
        @DisplayName("when the flow result is invalid, should return the validation errors and not save")
        void givenInvalidFlowResult_whenSave_shouldReturnValidationErrors() {
            stubFlowResult(ComponentContext.empty());
            when(shellHelper.error(anyString())).thenReturn("invalid");

            var result = command.save(null);

            assertThat(result).isEqualTo("invalid");
            verify(shellHelper).error(messageCaptor.capture());
            assertThat(messageCaptor.getValue().split("\n")).containsExactlyInAnyOrder(
                    "Name is required.",
                    "Username is required.",
                    "Password is required.");
            verifyNoMoreInteractions(shellHelper);
            verifyNoInteractions(secretService);
        }

        @Test
        @DisplayName("when no id is given, should save the flow result as a new active secret")
        void givenNoId_whenSave_shouldSaveNewSecret() {
            stubFlowResult(completeContext());
            when(secretService.save(any(), isNull())).thenReturn(RETURNED_ID.toString());
            when(shellHelper.success("Secret with id %s saved with success.".formatted(RETURNED_ID)))
                    .thenReturn("saved");

            var result = command.save(null);

            assertThat(result).isEqualTo("saved");
            verify(secretService).save(saveDtoCaptor.capture(), isNull());
            assertThat(saveDtoCaptor.getValue()).usingRecursiveComparison().isEqualTo(
                    SecretSaveDto.builder()
                            .name("Bank Login")
                            .active(true)
                            .description("Main account")
                            .link("https://example.com/login")
                            .username("john")
                            .password("  s3  cret ")
                            .build());
            verify(shellHelper).success("Secret with id %s saved with success.".formatted(RETURNED_ID));
            verifyNoMoreInteractions(secretService, shellHelper);
        }

        @Test
        @DisplayName("when an id is given, should load the secret, rerun the flow and update it")
        void givenId_whenSave_shouldUpdateExistingSecret() {
            when(secretService.getById(ID)).thenReturn(createSecretDto());
            stubFlowResult(completeContext());
            when(secretService.save(any(), eq(ID))).thenReturn(ID.toString());
            when(shellHelper.success("Secret with id %s saved with success.".formatted(ID))).thenReturn("saved");

            var result = command.save(ID);

            assertThat(result).isEqualTo("saved");
            verify(secretService).getById(ID);
            verify(secretService).save(saveDtoCaptor.capture(), eq(ID));
            assertThat(saveDtoCaptor.getValue()).usingRecursiveComparison().isEqualTo(
                    SecretSaveDto.builder()
                            .name("Bank Login")
                            .active(false)
                            .description("Main account")
                            .link("https://example.com/login")
                            .username("john")
                            .password("  s3  cret ")
                            .build());
            verify(shellHelper).success("Secret with id %s saved with success.".formatted(ID));
            verifyNoMoreInteractions(secretService, shellHelper);
        }
    }

    @Nested
    @DisplayName("preSaveFlow")
    class PreSaveFlow {

        @Test
        @DisplayName("should build the secret flow params without extra data")
        void givenParams_whenPreSaveFlow_shouldReturnSecretParams() {
            var result = command.preSaveFlow(CommandParam.of(ID));

            assertThat(result.getType()).isEqualTo(FlowType.SECRET);
            assertThat(result).usingRecursiveComparison()
                    .isEqualTo(SaveFlowParam.noArgs(FlowType.SECRET));
            verifyNoInteractions(secretService, shellHelper);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete through the service and return the success message")
        void givenId_whenDelete_shouldDeleteAndReturnMessage() {
            when(shellHelper.success("Secret with id %s deleted with success.".formatted(ID)))
                    .thenReturn("deleted");

            var result = command.delete(ID);

            assertThat(result).isEqualTo("deleted");
            verify(secretService).delete(ID);
            verify(shellHelper).success("Secret with id %s deleted with success.".formatted(ID));
            verifyNoMoreInteractions(secretService, shellHelper);
        }
    }

    @Nested
    @DisplayName("get")
    class Get {

        @Test
        @DisplayName("should print the secret found by id as JSON")
        void givenId_whenGet_shouldPrintJson() {
            var dto = createSecretDto();
            when(secretService.getById(ID)).thenReturn(dto);
            when(shellHelper.printJson(dto)).thenReturn("json");

            var result = command.get(ID);

            assertThat(result).isEqualTo("json");
            verify(secretService).getById(ID);
            verify(shellHelper).printJson(dto);
            verifyNoMoreInteractions(secretService, shellHelper);
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        static Stream<Arguments> sortProperties() {
            return Stream.of(
                    Arguments.of("null properties use the default", null, new String[]{"name"}),
                    Arguments.of("empty properties use the default", new String[0], new String[]{"name"}),
                    Arguments.of("blank properties use the default", new String[]{" ", ""}, new String[]{"name"}),
                    Arguments.of("given properties replace the default", new String[]{"active", "id"},
                            new String[]{"active", "id"}));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("sortProperties")
        @DisplayName("should page, sort and filter through the service")
        void givenArguments_whenGetAll_shouldQueryServiceAndPrintTable(String label, String[] properties,
                                                                       String[] expectedProperties) {
            Page<Dtoable> page = new PageImpl<>(List.of());
            when(secretService.getAll(any(Pageable.class), any(SecretFilter.class))).thenReturn(page);
            when(shellHelper.printTable(page, EruTable.SECRET)).thenReturn("table");

            var result = command.getAll(2, 5, Sort.Direction.DESC, true, " abc ", properties);

            assertThat(result).isEqualTo("table");
            var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            var filterCaptor = ArgumentCaptor.forClass(SecretFilter.class);
            verify(secretService).getAll(pageableCaptor.capture(), filterCaptor.capture());
            assertThat(pageableCaptor.getValue())
                    .isEqualTo(PageRequest.of(1, 5, Sort.Direction.DESC, expectedProperties));
            assertThat(filterCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(SecretFilter.of(true, " abc "));
            assertThat(filterCaptor.getValue().isActive()).isTrue();
            assertThat(filterCaptor.getValue().getSearch()).isEqualTo("abc");
            verify(shellHelper).printTable(page, EruTable.SECRET);
            verifyNoMoreInteractions(secretService, shellHelper);
        }

        @Test
        @DisplayName("when optional filters are absent, should keep them null and use the first page")
        void givenNoOptionalFilters_whenGetAll_shouldPassNulls() {
            Page<Dtoable> page = new PageImpl<>(List.of());
            when(secretService.getAll(any(Pageable.class), any(SecretFilter.class))).thenReturn(page);
            when(shellHelper.printTable(page, EruTable.SECRET)).thenReturn("table");

            var result = command.getAll(1, 10, Sort.Direction.ASC, null, null, (String[]) null);

            assertThat(result).isEqualTo("table");
            var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            var filterCaptor = ArgumentCaptor.forClass(SecretFilter.class);
            verify(secretService).getAll(pageableCaptor.capture(), filterCaptor.capture());
            assertThat(pageableCaptor.getValue()).isEqualTo(PageRequest.of(0, 10, Sort.Direction.ASC, "name"));
            assertThat(filterCaptor.getValue()).usingRecursiveComparison()
                    .isEqualTo(SecretFilter.of(null, null));
            verify(shellHelper).printTable(page, EruTable.SECRET);
            verifyNoMoreInteractions(secretService, shellHelper);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @ParameterizedTest(name = "[{index}] active={0}")
        @ValueSource(booleans = {true, false})
        @DisplayName("should patch the status through the service and return the success message")
        void givenIdAndStatus_whenUpdate_shouldPatchAndReturnMessage(boolean active) {
            when(shellHelper.success("Secret with id %s updated with success.".formatted(ID)))
                    .thenReturn("updated");

            var result = command.update(ID, active);

            assertThat(result).isEqualTo("updated");
            var dtoCaptor = ArgumentCaptor.forClass(Dtoable.class);
            verify(secretService).update(dtoCaptor.capture(), eq(ID));
            assertThat(dtoCaptor.getValue()).isInstanceOf(SecretPatchDto.class);
            assertThat(dtoCaptor.getValue().isActive()).isEqualTo(active);
            verify(shellHelper).success("Secret with id %s updated with success.".formatted(ID));
            verifyNoMoreInteractions(secretService, shellHelper);
        }
    }
}
