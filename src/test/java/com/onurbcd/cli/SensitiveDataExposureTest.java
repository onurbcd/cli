package com.onurbcd.cli;

import com.onurbcd.cli.config.property.AdminProperties;
import com.onurbcd.cli.config.property.CryptoProperties;
import com.onurbcd.cli.dto.secret.SecretDto;
import com.onurbcd.cli.dto.secret.SecretSaveDto;
import com.onurbcd.cli.param.flow.SecretSaveFlowParam;
import com.onurbcd.cli.persistency.entity.Secret;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression guard: types that hold passwords or crypto keys must keep the default {@code Object#toString()}, so a
 * stray {@code log.info("{}", obj)} or string concatenation can never print the sensitive value.
 */
@DisplayName("sensitive data exposure")
class SensitiveDataExposureTest {

    private static final String PASSWORD = "s3cr3t-P@ssw0rd";
    private static final String KEY = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    @Test
    @DisplayName("Secret entity toString should not contain the password")
    void givenSecret_whenToString_shouldNotContainPassword() {
        var secret = new Secret();
        secret.setPassword(PASSWORD);

        assertThat(secret.toString()).doesNotContain(PASSWORD);
    }

    @Test
    @DisplayName("SecretDto toString should not contain the password")
    void givenSecretDto_whenToString_shouldNotContainPassword() {
        var dto = new SecretDto();
        dto.setPassword(PASSWORD);

        assertThat(dto.toString()).doesNotContain(PASSWORD);
    }

    @Test
    @DisplayName("SecretSaveDto toString should not contain the password")
    void givenSecretSaveDto_whenToString_shouldNotContainPassword() {
        var dto = SecretSaveDto.builder().password(PASSWORD).build();

        assertThat(dto.toString()).doesNotContain(PASSWORD);
    }

    @Test
    @DisplayName("SecretSaveFlowParam toString should not contain the password")
    void givenSecretSaveFlowParam_whenToString_shouldNotContainPassword() {
        var param = SecretSaveFlowParam.builder().password(PASSWORD).build();

        assertThat(param.toString()).doesNotContain(PASSWORD);
    }

    @Test
    @DisplayName("crypto properties toString should not contain the key")
    void givenCryptoProperties_whenToString_shouldNotContainKey() {
        var crypto = new CryptoProperties();
        crypto.setKey(KEY);
        var admin = new AdminProperties();
        admin.setCrypto(crypto);

        assertThat(crypto.toString()).doesNotContain(KEY);
        assertThat(admin.toString()).doesNotContain(KEY);
    }
}
