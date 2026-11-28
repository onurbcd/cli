package com.onurbcd.cli.dto.secret;

import com.onurbcd.cli.dto.PrimeDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static com.onurbcd.cli.TestFactory.createSecretDto;
import static org.assertj.core.api.Assertions.assertThat;

class SecretDtoTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    @DisplayName("is a PrimeDto and exposes the fields set through the setters")
    void givenFactorySecret_shouldExposeFields() {
        var secret = createSecretDto();

        assertThat(secret).isInstanceOf(PrimeDto.class);
        assertThat(secret.getDescription()).isEqualTo("description");
        assertThat(secret.getLink()).isEqualTo("https://example.com");
        assertThat(secret.getUsername()).isEqualTo("user");
        assertThat(secret.getPassword()).isEqualTo("password");
    }

    @Test
    @DisplayName("serialization should omit the password but keep the other fields")
    void givenDto_whenSerialize_shouldOmitPassword() {
        var json = mapper.writeValueAsString(createSecretDto());

        assertThat(json).doesNotContain("password").contains("\"username\":\"user\"");
    }

    @Test
    @DisplayName("deserialization should still accept the password")
    void givenJson_whenDeserialize_shouldSetPassword() {
        var dto = mapper.readValue("{\"password\":\"p\"}", SecretDto.class);

        assertThat(dto.getPassword()).isEqualTo("p");
    }
}
