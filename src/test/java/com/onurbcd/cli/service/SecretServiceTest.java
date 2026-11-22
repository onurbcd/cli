package com.onurbcd.cli.service;

import com.onurbcd.cli.annotation.PrimeService;
import com.onurbcd.cli.dto.secret.SecretDto;
import com.onurbcd.cli.dto.secret.SecretSaveDto;
import com.onurbcd.cli.enums.Domain;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.helper.CryptoHelper;
import com.onurbcd.cli.mapper.SecretToDtoMapper;
import com.onurbcd.cli.mapper.SecretToEntityMapper;
import com.onurbcd.cli.persistency.entity.Secret;
import com.onurbcd.cli.persistency.repository.SecretRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecretServiceTest {

    private static final LocalDateTime CREATED = LocalDateTime.of(2024, 3, 15, 10, 30);
    private static final String PLAIN = "plain-password";
    private static final String ENCRYPTED = "encrypted-password";

    @Mock
    private SecretRepository repository;
    @Mock
    private CryptoHelper cryptoHelper;
    @Mock
    private SecretToDtoMapper toDtoMapper;
    @Mock
    private SecretToEntityMapper toEntityMapper;

    private SecretService service;

    @BeforeEach
    void setUp() {
        service = new SecretService(repository, cryptoHelper, toDtoMapper, toEntityMapper);
    }

    private static Secret secret(UUID id, String password) {
        return Secret.builder().id(id).name("name").active(true).username("user").password(password)
                .createdDate(CREATED).build();
    }

    @Test
    @DisplayName("should be registered as the secret service")
    void shouldBeQualifiedAsSecret() {
        assertThat(SecretService.class.getAnnotation(PrimeService.class).value()).isEqualTo(Domain.SECRET);
    }

    @Nested
    @DisplayName("validate")
    class Validate {

        private final SecretSaveDto dto = SecretSaveDto.builder().name("name").build();

        @Test
        @DisplayName("with null id and null entity, should not throw")
        void givenNullIdAndEntity_shouldNotThrow() {
            service.validate(dto, null, null);

            verifyNoInteractions(repository, cryptoHelper, toDtoMapper, toEntityMapper);
        }

        @Test
        @DisplayName("with id and entity, should not throw")
        void givenIdAndEntity_shouldNotThrow() {
            service.validate(dto, secret(ID, PLAIN), ID);

            verifyNoInteractions(repository, cryptoHelper, toDtoMapper, toEntityMapper);
        }

        @Test
        @DisplayName("with id and no entity, should throw not found for the id")
        void givenIdAndNoEntity_shouldThrowNotFound() {
            assertThatThrownBy(() -> service.validate(dto, null, ID))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.ENTITY_DOES_NOT_EXIST);
                        assertThat(e.getMessage()).contains(ID.toString());
                    });
        }
    }

    @Nested
    @DisplayName("fillValues")
    class FillValues {

        private final SecretSaveDto dto = SecretSaveDto.builder().name("name").build();

        @Test
        @DisplayName("with a password, should encrypt it")
        void givenPassword_shouldEncrypt() {
            var mapped = secret(PARENT_ID, PLAIN);
            when(toEntityMapper.apply(dto)).thenReturn(mapped);
            when(cryptoHelper.encrypt(PLAIN)).thenReturn(ENCRYPTED);

            var result = service.fillValues(dto, null);

            assertThat(result).isSameAs(mapped);
            assertThat(result.getPassword()).isEqualTo(ENCRYPTED);
            assertThat(result.getId()).isNull();
            assertThat(result.getCreatedDate()).isNull();
            verify(toEntityMapper).apply(dto);
            verify(cryptoHelper).encrypt(PLAIN);
            verifyNoMoreInteractions(toEntityMapper, cryptoHelper);
        }

        @ParameterizedTest(name = "[{index}] password=''{0}''")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t"})
        @DisplayName("with a blank password, should leave it untouched and not encrypt")
        void givenBlankPassword_shouldNotEncrypt(String password) {
            var mapped = secret(PARENT_ID, password);
            when(toEntityMapper.apply(dto)).thenReturn(mapped);

            var result = service.fillValues(dto, null);

            assertThat(result.getPassword()).isEqualTo(password);
            verify(toEntityMapper).apply(dto);
            verifyNoMoreInteractions(toEntityMapper);
            verifyNoInteractions(cryptoHelper);
        }

        @Test
        @DisplayName("with a current entity, should keep its id and created date")
        void givenEntity_shouldKeepIdentity() {
            var mapped = secret(PARENT_ID, PLAIN);
            mapped.setCreatedDate(null);
            when(toEntityMapper.apply(dto)).thenReturn(mapped);
            when(cryptoHelper.encrypt(PLAIN)).thenReturn(ENCRYPTED);

            var result = service.fillValues(dto, secret(ID, "old"));

            assertThat(result.getId()).isEqualTo(ID);
            assertThat(result.getCreatedDate()).isEqualTo(CREATED);
            verify(toEntityMapper).apply(dto);
            verify(cryptoHelper).encrypt(PLAIN);
            verifyNoMoreInteractions(toEntityMapper, cryptoHelper);
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should encrypt the password of the persisted secret")
        void shouldPersistEncryptedSecret() {
            var dto = SecretSaveDto.builder().name("name").build();
            var mapped = secret(PARENT_ID, PLAIN);
            when(repository.findById(ID)).thenReturn(Optional.of(secret(ID, "old")));
            when(toEntityMapper.apply(dto)).thenReturn(mapped);
            when(cryptoHelper.encrypt(PLAIN)).thenReturn(ENCRYPTED);
            when(repository.save(mapped)).thenReturn(mapped);

            var result = service.save(dto, ID);

            assertThat(result).isEqualTo(ID.toString());
            assertThat(mapped.getPassword()).isEqualTo(ENCRYPTED);
            verify(repository).findById(ID);
            verify(toEntityMapper).apply(dto);
            verify(cryptoHelper).encrypt(PLAIN);
            verify(repository).save(mapped);
            verifyNoMoreInteractions(repository, toEntityMapper, cryptoHelper);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should read through JPA and the dto mapper")
        void shouldMapWithDtoMapper() {
            var entity = secret(ID, ENCRYPTED);
            var dto = new SecretDto();
            when(repository.findById(ID)).thenReturn(Optional.of(entity));
            when(toDtoMapper.apply(entity)).thenReturn(dto);

            var result = service.getById(ID);

            assertThat(result).isSameAs(dto);
            verify(repository).findById(ID);
            verify(toDtoMapper).apply(entity);
            verifyNoMoreInteractions(repository, toDtoMapper);
        }
    }
}
