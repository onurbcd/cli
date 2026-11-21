package com.onurbcd.cli.dto.secret;

import com.onurbcd.cli.dto.PrimeDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.onurbcd.cli.TestFactory.createSecretDto;
import static org.assertj.core.api.Assertions.assertThat;

class SecretDtoTest {

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
}
