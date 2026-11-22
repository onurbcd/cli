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

        assertThat(secret)
                .returns(ID, Secret::getId)
                .returns("mail", Secret::getName)
                .returns(true, Secret::getActive)
                .returns("personal mail", Secret::getDescription)
                .returns("https://mail.example.com", Secret::getLink)
                .returns("user", Secret::getUsername)
                .returns("pass", Secret::getPassword);
    }

    @Test
    @DisplayName("the all-args constructor keeps every field in order")
    void givenAllArgs_whenConstruct_shouldKeepEveryField() {
        var secret = new Secret("description", "https://link.example.com", "user", "pass");

        assertThat(secret)
                .returns("description", Secret::getDescription)
                .returns("https://link.example.com", Secret::getLink)
                .returns("user", Secret::getUsername)
                .returns("pass", Secret::getPassword);
    }

    @Test
    @DisplayName("setters store each field")
    void givenValues_whenSet_shouldReturnThemFromGetters() {
        var secret = new Secret();

        secret.setDescription("text");
        secret.setLink("https://x.example.com");
        secret.setUsername("name");
        secret.setPassword("word");

        assertThat(secret)
                .returns("text", Secret::getDescription)
                .returns("https://x.example.com", Secret::getLink)
                .returns("name", Secret::getUsername)
                .returns("word", Secret::getPassword);
    }

    @Test
    @DisplayName("equality follows the id")
    void givenIds_whenEqualsAndHashCode_shouldFollowId() {
        var secret = Secret.builder().id(ID).build();

        assertThat(secret).isEqualTo(Secret.builder().id(ID).build()).hasSameHashCodeAs(Secret.builder().id(ID).build())
                .isNotEqualTo(Secret.builder().id(OTHER_ID).build())
                .doesNotHaveSameHashCodeAs(Secret.builder().id(OTHER_ID).build());
    }
}
