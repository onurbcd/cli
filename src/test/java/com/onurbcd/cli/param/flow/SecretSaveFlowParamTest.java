package com.onurbcd.cli.param.flow;

import com.onurbcd.cli.enums.FlowType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.onurbcd.cli.TestFactory.createSecretDto;
import static org.assertj.core.api.Assertions.assertThat;

class SecretSaveFlowParamTest {

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("when secret is complete, should convert every field")
        void givenFullSecret_whenOf_shouldConvertEveryField() {
            var result = SecretSaveFlowParam.of(createSecretDto());

            var expected = SecretSaveFlowParam.builder()
                    .name("bogus")
                    .description("description")
                    .link("https://example.com")
                    .username("user")
                    .password("password")
                    .build();
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
        }

        @Test
        @DisplayName("when secret is null, should leave every field null")
        void givenNullSecret_whenOf_shouldLeaveFieldsNull() {
            var result = SecretSaveFlowParam.of(null);

            assertThat(result).usingRecursiveComparison().isEqualTo(SecretSaveFlowParam.builder().build());
        }
    }

    @Nested
    @DisplayName("getType")
    class GetType {

        @Test
        @DisplayName("should be SECRET")
        void whenGetType_shouldBeSecret() {
            assertThat(SecretSaveFlowParam.builder().build().getType()).isEqualTo(FlowType.SECRET);
        }
    }
}
