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
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class CryptoHelperTest {

    private static final int IV_BYTES = 12;
    private static final int TAG_BYTES = 16;
    private static final String PREFIX = "v2:";
    private static final byte[] KEY_BYTES = sequence(32, 1);
    private static final String KEY = Base64.getEncoder().encodeToString(KEY_BYTES);
    private static final String OTHER_KEY = Base64.getEncoder().encodeToString(sequence(32, 100));
    private static final String PLAIN_TEXT = "secret-text";
    private static final String KEY_ERROR = "key must be the Base64 encoding of 32 random bytes";

    private final SecureRandom random = new SecureRandom();

    private CryptoHelper helper;

    @BeforeEach
    void setUp() {
        helper = new CryptoHelper(configWith(KEY), random);
    }

    private static byte[] sequence(int length, int start) {
        var bytes = new byte[length];
        IntStream.range(0, length).forEach(i -> bytes[i] = (byte) (start + i));
        return bytes;
    }

    private static AdminProperties configWith(String key) {
        var crypto = new CryptoProperties();
        crypto.setKey(key);

        var properties = new AdminProperties();
        properties.setCrypto(crypto);
        return properties;
    }

    private static byte[] payloadOf(String encoded) {
        return Base64.getDecoder().decode(encoded.substring(PREFIX.length()));
    }

    private static String encodedOf(byte[] payload) {
        return PREFIX + Base64.getEncoder().encodeToString(payload);
    }

    private static Cipher referenceCipher(int mode, byte[] iv) throws GeneralSecurityException {
        var cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(mode, new SecretKeySpec(KEY_BYTES, "AES"), new GCMParameterSpec(128, iv));
        return cipher;
    }

    private static void assertCryptoError(Throwable thrown, String expectedArg, Class<? extends Throwable> expectedCause) {
        assertThat(thrown).isInstanceOfSatisfying(ApiException.class, e -> {
            assertThat(e.getError()).isEqualTo(ErrorCode.CRYPTO);

            if (expectedArg != null) {
                assertThat(e.getMessage()).isEqualTo(ErrorCode.CRYPTO.format(expectedArg));
            }

            if (expectedCause == null) {
                assertThat(e.getCause()).isNull();
            } else {
                assertThat(e.getCause()).isInstanceOf(expectedCause);
                assertThat(e.getMessage()).isEqualTo(ErrorCode.CRYPTO.format(e.getCause().getMessage()));
            }
        });
    }

    static Stream<Arguments> validKeys() {
        return Stream.of(
                Arguments.of("sequential bytes", KEY),
                Arguments.of("alphabet with plus and slash", Base64.getEncoder().encodeToString(
                        sequence(32, 0xE0))));
    }

    static Stream<String> invalidKeys() {
        return Stream.of(
                "",
                "   ",
                "%%%",
                "c2hvcnQ=",
                KEY.substring(0, 43),
                KEY + "A",
                KEY.substring(0, 42) + "==",
                KEY.substring(0, 20) + "%" + KEY.substring(21),
                KEY.substring(0, 20) + " " + KEY.substring(21),
                " " + KEY,
                KEY + " ",
                "\n" + KEY,
                Base64.getEncoder().encodeToString(sequence(33, 1)),
                Base64.getEncoder().encodeToString(sequence(31, 1)));
    }

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.onurbcd.cli.helper.CryptoHelperTest#validKeys")
        @DisplayName("when key is the Base64 of 32 bytes, should create a helper that round-trips")
        void givenValidKey_whenCreate_shouldRoundTrip(String label, String key) {
            var created = new CryptoHelper(configWith(key), random);

            assertThat(created.decrypt(created.encrypt(PLAIN_TEXT))).isEqualTo(PLAIN_TEXT);
        }

        @ParameterizedTest(name = "[{index}] key={0}")
        @NullSource
        @MethodSource("com.onurbcd.cli.helper.CryptoHelperTest#invalidKeys")
        @DisplayName("when key is missing, malformed or not 32 bytes, should throw CRYPTO ApiException without the key")
        void givenInvalidKey_whenCreate_shouldThrowCryptoError(String key) {
            var config = configWith(key);

            assertThatThrownBy(() -> new CryptoHelper(config, random))
                    .satisfies(e -> assertCryptoError(e, KEY_ERROR, null))
                    .satisfies(e -> {
                        if (key != null && !key.isBlank()) {
                            assertThat(e.getMessage()).doesNotContain(key.strip());
                        }
                    });
        }
    }

    @Nested
    @DisplayName("encrypt")
    class Encrypt {

        @Test
        @DisplayName("should return v2 prefix plus Base64 of IV, cipher text and tag")
        void givenPlainText_whenEncrypt_shouldReturnVersionedPayload() {
            var result = helper.encrypt(PLAIN_TEXT);

            assertThat(result).startsWith(PREFIX);
            assertThat(payloadOf(result)).hasSize(IV_BYTES + PLAIN_TEXT.length() + TAG_BYTES);
        }

        @Test
        @DisplayName("should be decryptable by an independent AES-256-GCM implementation using the embedded IV")
        void givenPlainText_whenEncrypt_shouldMatchReferenceAesGcm() throws GeneralSecurityException {
            var payload = payloadOf(helper.encrypt(PLAIN_TEXT));
            var iv = Arrays.copyOfRange(payload, 0, IV_BYTES);

            var plain = referenceCipher(Cipher.DECRYPT_MODE, iv).doFinal(payload, IV_BYTES, payload.length - IV_BYTES);

            assertThat(new String(plain, StandardCharsets.UTF_8)).isEqualTo(PLAIN_TEXT);
        }

        @Test
        @DisplayName("should take a 12-byte IV from the injected SecureRandom and store it before the cipher text")
        void givenFixedRandom_whenEncrypt_shouldUseInjectedIvAndMatchReference() throws GeneralSecurityException {
            var iv = sequence(IV_BYTES, 50);
            var fixedRandom = mock(SecureRandom.class);
            doAnswer(invocation -> {
                System.arraycopy(iv, 0, invocation.<byte[]>getArgument(0), 0, IV_BYTES);
                return null;
            }).when(fixedRandom).nextBytes(any(byte[].class));
            var fixed = new CryptoHelper(configWith(KEY), fixedRandom);
            var expectedCipherText = referenceCipher(Cipher.ENCRYPT_MODE, iv)
                    .doFinal(PLAIN_TEXT.getBytes(StandardCharsets.UTF_8));

            var result = fixed.encrypt(PLAIN_TEXT);

            var expectedPayload = new byte[IV_BYTES + expectedCipherText.length];
            System.arraycopy(iv, 0, expectedPayload, 0, IV_BYTES);
            System.arraycopy(expectedCipherText, 0, expectedPayload, IV_BYTES, expectedCipherText.length);
            assertThat(result).isEqualTo(encodedOf(expectedPayload));
            verify(fixedRandom).nextBytes(any(byte[].class));
            verifyNoMoreInteractions(fixedRandom);
        }

        @Test
        @DisplayName("when text is empty, should return only IV and tag")
        void givenEmptyText_whenEncrypt_shouldReturnIvAndTagOnly() {
            assertThat(payloadOf(helper.encrypt(""))).hasSize(IV_BYTES + TAG_BYTES);
        }

        @Test
        @DisplayName("when text has multi-byte characters, should encrypt the UTF-8 bytes")
        void givenUnicodeText_whenEncrypt_shouldEncryptUtf8Bytes() {
            var text = "senha-ação-€";

            var payload = payloadOf(helper.encrypt(text));

            assertThat(payload).hasSize(IV_BYTES + text.getBytes(StandardCharsets.UTF_8).length + TAG_BYTES);
        }

        @Test
        @DisplayName("when called repeatedly with the same input, should use a different IV and cipher text each time")
        void givenSameInput_whenEncryptRepeatedly_shouldNeverRepeat() {
            var results = new HashSet<String>();
            var ivs = new HashSet<String>();

            for (var i = 0; i < 20; i++) {
                var result = helper.encrypt(PLAIN_TEXT);
                results.add(result);
                ivs.add(Base64.getEncoder().encodeToString(Arrays.copyOfRange(payloadOf(result), 0, IV_BYTES)));
            }

            assertThat(results).hasSize(20);
            assertThat(ivs).hasSize(20);
        }

        @Test
        @DisplayName("should not expose the plain text in the stored value")
        void givenPlainText_whenEncrypt_shouldNotExposePlainText() {
            var result = helper.encrypt(PLAIN_TEXT);

            assertThat(result).doesNotContain(PLAIN_TEXT);
            assertThat(new String(payloadOf(result), StandardCharsets.ISO_8859_1)).doesNotContain(PLAIN_TEXT);
        }
    }

    @Nested
    @DisplayName("decrypt")
    class Decrypt {

        @Test
        @DisplayName("when value was produced by an independent AES-256-GCM implementation, should return the plain text")
        void givenReferenceCipherText_whenDecrypt_shouldReturnPlainText() throws GeneralSecurityException {
            var iv = sequence(IV_BYTES, 50);
            var cipherText = referenceCipher(Cipher.ENCRYPT_MODE, iv).doFinal(PLAIN_TEXT.getBytes(StandardCharsets.UTF_8));
            var payload = new byte[IV_BYTES + cipherText.length];
            System.arraycopy(iv, 0, payload, 0, IV_BYTES);
            System.arraycopy(cipherText, 0, payload, IV_BYTES, cipherText.length);

            var result = helper.decrypt(encodedOf(payload));

            assertThat(result).isEqualTo(PLAIN_TEXT);
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @ValueSource(strings = {"", "secret-text", "senha-ação-€ 日本語", "a"})
        @DisplayName("when value was produced by encrypt, should return the original text")
        void givenEncryptedText_whenDecrypt_shouldRoundTrip(String text) {
            assertThat(helper.decrypt(helper.encrypt(text))).isEqualTo(text);
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @ValueSource(strings = {"", "rVaau9ylrZUxOflUQKVVEg==", "v1:AAAA", "V2:AAAA", " v2:AAAA", "v2AAAA"})
        @DisplayName("when value does not start with the v2 prefix, should throw CRYPTO ApiException")
        void givenValueWithoutPrefix_whenDecrypt_shouldThrowUnsupportedFormat(String value) {
            assertThatThrownBy(() -> helper.decrypt(value))
                    .satisfies(e -> assertCryptoError(e, "unsupported secret format", null));
        }

        @Test
        @DisplayName("when payload is not valid Base64, should throw CRYPTO ApiException chaining the parse failure")
        void givenInvalidBase64_whenDecrypt_shouldThrowCryptoError() {
            assertThatThrownBy(() -> helper.decrypt(PREFIX + "%%%"))
                    .satisfies(e -> assertCryptoError(e, null, IllegalArgumentException.class));
        }

        @ParameterizedTest(name = "[{index}] {0} bytes")
        @ValueSource(ints = {0, 1, 11, 12, 27})
        @DisplayName("when payload is shorter than IV plus tag, should throw CRYPTO ApiException")
        void givenTruncatedPayload_whenDecrypt_shouldThrowMalformed(int length) {
            var value = encodedOf(new byte[length]);

            assertThatThrownBy(() -> helper.decrypt(value))
                    .satisfies(e -> assertCryptoError(e, "malformed secret", null));
        }

        @Test
        @DisplayName("when payload has exactly IV plus tag but they do not authenticate, should fail the tag check")
        void givenMinimumLengthGarbage_whenDecrypt_shouldFailAuthentication() {
            var value = encodedOf(new byte[IV_BYTES + TAG_BYTES]);

            assertThatThrownBy(() -> helper.decrypt(value))
                    .satisfies(e -> assertCryptoError(e, null, AEADBadTagException.class));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.onurbcd.cli.helper.CryptoHelperTest#tamperedPositions")
        @DisplayName("when any part of the stored value was changed, should fail authentication")
        void givenTamperedValue_whenDecrypt_shouldFailAuthentication(String label, int position) {
            var payload = payloadOf(helper.encrypt(PLAIN_TEXT));
            payload[position < 0 ? payload.length + position : position] ^= 1;
            var tampered = encodedOf(payload);

            assertThatThrownBy(() -> helper.decrypt(tampered))
                    .satisfies(e -> assertCryptoError(e, null, AEADBadTagException.class));
        }

        @Test
        @DisplayName("when stored value has trailing bytes, should fail authentication")
        void givenExtraTrailingByte_whenDecrypt_shouldFailAuthentication() {
            var payload = payloadOf(helper.encrypt(PLAIN_TEXT));
            var extended = encodedOf(Arrays.copyOf(payload, payload.length + 1));

            assertThatThrownBy(() -> helper.decrypt(extended))
                    .satisfies(e -> assertCryptoError(e, null, AEADBadTagException.class));
        }

        @Test
        @DisplayName("when value was encrypted with a different key, should fail authentication")
        void givenValueFromOtherKey_whenDecrypt_shouldFailAuthentication() {
            var encrypted = new CryptoHelper(configWith(OTHER_KEY), random).encrypt(PLAIN_TEXT);

            assertThatThrownBy(() -> helper.decrypt(encrypted))
                    .satisfies(e -> assertCryptoError(e, null, AEADBadTagException.class));
        }

        @Test
        @DisplayName("when decryption fails, should not expose the plain text or key in the error")
        void givenFailure_whenDecrypt_shouldNotLeakSecrets() {
            var payload = payloadOf(helper.encrypt(PLAIN_TEXT));
            payload[IV_BYTES] ^= 1;
            var tampered = encodedOf(payload);

            assertThatThrownBy(() -> helper.decrypt(tampered))
                    .hasMessageNotContaining(PLAIN_TEXT)
                    .hasMessageNotContaining(KEY);
        }
    }

    static Stream<Arguments> tamperedPositions() {
        return Stream.of(
                Arguments.of("first IV byte", 0),
                Arguments.of("last IV byte", IV_BYTES - 1),
                Arguments.of("first cipher text byte", IV_BYTES),
                Arguments.of("first tag byte", -TAG_BYTES),
                Arguments.of("last tag byte", -1));
    }
}
