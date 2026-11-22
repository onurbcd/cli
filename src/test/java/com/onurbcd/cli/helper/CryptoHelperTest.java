package com.onurbcd.cli.helper;

import com.onurbcd.cli.config.property.AdminProperties;
import com.onurbcd.cli.config.property.CryptoProperties;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CryptoHelperTest {

    private static final String KEY = "0123456789abcdef";
    private static final String IV = "fedcba9876543210";
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String NO_PADDING = "AES/CBC/NoPadding";
    private static final String PLAIN_TEXT = "secret-text";
    private static final String CIPHER_TEXT = "rVaau9ylrZUxOflUQKVVEg==";

    private AdminProperties config;
    private CryptoHelper helper;

    @BeforeEach
    void setUp() {
        config = configWith(KEY, IV, ALGORITHM, TRANSFORMATION);
        helper = new CryptoHelper(config);
    }

    private static AdminProperties configWith(String key1, String key2, String algorithm, String transformation) {
        var crypto = new CryptoProperties();
        crypto.setKey1(key1);
        crypto.setKey2(key2);
        crypto.setAlgorithm(algorithm);
        crypto.setTransformation(transformation);

        var properties = new AdminProperties();
        properties.setCrypto(crypto);
        return properties;
    }

    private static void assertCryptoError(Throwable thrown, Class<? extends Throwable> expectedCause) {
        assertThat(thrown).isInstanceOfSatisfying(ApiException.class, e -> {
            assertThat(e.getError()).isEqualTo(ErrorCode.CRYPTO);
            assertThat(e.getCause()).isInstanceOf(expectedCause);
            assertThat(e.getMessage()).isEqualTo(ErrorCode.CRYPTO.format(e.getCause().getMessage()));
        });
    }

    static Stream<Arguments> invalidConfigurations() {
        return Stream.of(
                Arguments.of("unknown algorithm", KEY, IV, "NOPE", TRANSFORMATION),
                Arguments.of("unknown transformation", KEY, IV, ALGORITHM, "NOPE/CBC/PKCS5Padding"),
                Arguments.of("invalid key length", "short", IV, ALGORITHM, TRANSFORMATION),
                Arguments.of("invalid iv length", KEY, "short", ALGORITHM, TRANSFORMATION));
    }

    @Nested
    @DisplayName("encrypt")
    class Encrypt {

        @Test
        @DisplayName("when configuration is valid, should return the Base64 AES cipher text")
        void givenPlainText_whenEncrypt_shouldReturnBase64CipherText() {
            var result = helper.encrypt(PLAIN_TEXT);

            assertThat(result).isEqualTo(CIPHER_TEXT);
        }

        @Test
        @DisplayName("when called twice with the same input, should be deterministic")
        void givenSameInput_whenEncryptTwice_shouldReturnSameResult() {
            assertThat(helper.encrypt(PLAIN_TEXT)).isEqualTo(helper.encrypt(PLAIN_TEXT));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.onurbcd.cli.helper.CryptoHelperTest#invalidConfigurations")
        @DisplayName("when configuration is invalid, should throw CRYPTO ApiException")
        void givenInvalidConfiguration_whenEncrypt_shouldThrowCryptoError(String label, String key1, String key2,
                                                                          String algorithm, String transformation) {
            var invalid = new CryptoHelper(configWith(key1, key2, algorithm, transformation));

            assertThatThrownBy(() -> invalid.encrypt(PLAIN_TEXT))
                    .satisfies(e -> assertCryptoError(e, GeneralSecurityException.class));
        }

        @Test
        @DisplayName("when plain text does not fit the block size without padding, should throw CRYPTO ApiException")
        void givenIllegalBlockSize_whenEncrypt_shouldThrowCryptoError() {
            var noPadding = new CryptoHelper(configWith(KEY, IV, ALGORITHM, NO_PADDING));

            assertThatThrownBy(() -> noPadding.encrypt(PLAIN_TEXT))
                    .satisfies(e -> assertCryptoError(e, IllegalBlockSizeException.class));
        }
    }

    @Nested
    @DisplayName("decrypt")
    class Decrypt {

        @Test
        @DisplayName("when cipher text is valid, should return the original plain text")
        void givenCipherText_whenDecrypt_shouldReturnPlainText() {
            var result = helper.decrypt(CIPHER_TEXT);

            assertThat(result).isEqualTo(PLAIN_TEXT);
        }

        @Test
        @DisplayName("when text with non-ASCII characters is encrypted, should round-trip as UTF-8")
        void givenUnicodeText_whenEncryptThenDecrypt_shouldRoundTrip() {
            var text = "senha-ação-€";

            var result = helper.decrypt(helper.encrypt(text));

            assertThat(result).isEqualTo(text);
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.onurbcd.cli.helper.CryptoHelperTest#invalidConfigurations")
        @DisplayName("when configuration is invalid, should throw CRYPTO ApiException")
        void givenInvalidConfiguration_whenDecrypt_shouldThrowCryptoError(String label, String key1, String key2,
                                                                          String algorithm, String transformation) {
            var invalid = new CryptoHelper(configWith(key1, key2, algorithm, transformation));

            assertThatThrownBy(() -> invalid.decrypt(CIPHER_TEXT))
                    .satisfies(e -> assertCryptoError(e, GeneralSecurityException.class));
        }

        @Test
        @DisplayName("when cipher text length is not a block multiple, should throw CRYPTO ApiException")
        void givenIllegalBlockSize_whenDecrypt_shouldThrowCryptoError() {
            var notABlock = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3});

            assertThatThrownBy(() -> helper.decrypt(notABlock))
                    .satisfies(e -> assertCryptoError(e, IllegalBlockSizeException.class));
        }

        @Test
        @DisplayName("when padding is corrupt (wrong key), should throw CRYPTO ApiException")
        void givenWrongKey_whenDecrypt_shouldThrowCryptoError() {
            var wrongKey = new CryptoHelper(configWith("abcdef0123456789", IV, ALGORITHM, TRANSFORMATION));

            assertThatThrownBy(() -> wrongKey.decrypt(CIPHER_TEXT))
                    .satisfies(e -> assertCryptoError(e, BadPaddingException.class));
        }

        @Test
        @DisplayName("when cipher text is not valid Base64, should propagate IllegalArgumentException")
        void givenInvalidBase64_whenDecrypt_shouldPropagateIllegalArgument() {
            assertThatThrownBy(() -> helper.decrypt("%%%"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
