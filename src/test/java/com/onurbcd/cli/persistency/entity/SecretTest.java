package com.onurbcd.cli.persistency.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;

class SecretTest {

    private static final UUID OTHER_ID = UUID.fromString("99999999-9999-9999-0000-000000000001");

    @Test
    @DisplayName("the builder fills every field")
    void givenBuilder_whenBuild_shouldFillEveryField() {
        var secret = Secret.builder()
                .id(ID)
                .name("mail")
                .active(true)
                .description("personal mail")
                .link("https://mail.example.com")
                .username("user")
                .password("pass")
                .build();

        assertThat(secret.getId()).isEqualTo(ID);
        assertThat(secret.getName()).isEqualTo("mail");
        assertThat(secret.getActive()).isTrue();
        assertThat(secret.getDescription()).isEqualTo("personal mail");
        assertThat(secret.getLink()).isEqualTo("https://mail.example.com");
        assertThat(secret.getUsername()).isEqualTo("user");
        assertThat(secret.getPassword()).isEqualTo("pass");
    }

    @Test
    @DisplayName("the all-args constructor keeps every field in order")
    void givenAllArgs_whenConstruct_shouldKeepEveryField() {
        var secret = new Secret("description", "https://link.example.com", "user", "pass");

        assertThat(secret.getDescription()).isEqualTo("description");
        assertThat(secret.getLink()).isEqualTo("https://link.example.com");
        assertThat(secret.getUsername()).isEqualTo("user");
        assertThat(secret.getPassword()).isEqualTo("pass");
    }

    @Test
    @DisplayName("setters store each field")
    void givenValues_whenSet_shouldReturnThemFromGetters() {
        var secret = new Secret();

        secret.setDescription("text");
        secret.setLink("https://x.example.com");
        secret.setUsername("name");
        secret.setPassword("word");

        assertThat(secret.getDescription()).isEqualTo("text");
        assertThat(secret.getLink()).isEqualTo("https://x.example.com");
        assertThat(secret.getUsername()).isEqualTo("name");
        assertThat(secret.getPassword()).isEqualTo("word");
    }

    @Test
    @DisplayName("equality follows the id")
    void givenIds_whenEqualsAndHashCode_shouldFollowId() {
        var secret = Secret.builder().id(ID).build();

        assertThat(secret).isEqualTo(Secret.builder().id(ID).build()).hasSameHashCodeAs(Secret.builder().id(ID).build());
        assertThat(secret).isNotEqualTo(Secret.builder().id(OTHER_ID).build())
                .doesNotHaveSameHashCodeAs(Secret.builder().id(OTHER_ID).build());
    }
}
