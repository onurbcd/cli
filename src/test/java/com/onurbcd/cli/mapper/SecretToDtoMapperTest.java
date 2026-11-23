package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.secret.SecretDto;
import com.onurbcd.cli.helper.CryptoHelper;
import com.onurbcd.cli.persistency.entity.Secret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecretToDtoMapperTest {

    private static final String ENCRYPTED = "encrypted";
    private static final String DECRYPTED = "decrypted";
    private static final LocalDateTime CREATED = LocalDateTime.of(2024, 1, 2, 3, 4);
    private static final LocalDateTime MODIFIED = LocalDateTime.of(2024, 5, 6, 7, 8);

    @Mock
    private CryptoHelper cryptoHelper;

    private SecretToDtoMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new SecretToDtoMapperImpl(cryptoHelper);
    }

    private static Secret createSecret() {
        return Secret.builder()
                .id(ID)
                .name("secret")
                .active(true)
                .createdDate(CREATED)
                .lastModifiedDate(MODIFIED)
                .description("description")
                .link("https://example.com")
                .username("user")
                .password(ENCRYPTED)
                .build();
    }

    @Nested
    @DisplayName("apply")
    class Apply {

        @Test
        @DisplayName("when secret is complete, should map the fields and decrypt the password")
        void givenFullSecret_whenApply_shouldMapFieldsAndDecryptPassword() {
            var secret = createSecret();
            when(cryptoHelper.decrypt(ENCRYPTED)).thenReturn(DECRYPTED);

            var result = mapper.apply(secret);

            var expected = new SecretDto();
            expected.setId(ID);
            expected.setName("secret");
            expected.setActive(true);
            expected.setCreatedDate(CREATED);
            expected.setLastModifiedDate(MODIFIED);
            expected.setDescription("description");
            expected.setLink("https://example.com");
            expected.setUsername("user");
            expected.setPassword(DECRYPTED);
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
            assertThat(secret).usingRecursiveComparison().isEqualTo(createSecret());
            verify(cryptoHelper).decrypt(ENCRYPTED);
            verifyNoMoreInteractions(cryptoHelper);
        }

        @Test
        @DisplayName("when secret is null, should return an empty dto without decrypting")
        void givenNullSecret_whenApply_shouldReturnEmptyDto() {
            var result = mapper.apply(null);

            assertThat(result).usingRecursiveComparison().isEqualTo(new SecretDto());
            verifyNoInteractions(cryptoHelper);
        }
    }
}
