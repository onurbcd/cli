package com.onurbcd.cli.helper;

import com.onurbcd.cli.annotation.Helper;
import com.onurbcd.cli.config.property.AdminProperties;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import org.mapstruct.Named;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Authenticated encryption (AES-256-GCM) for secrets stored in the database.
 *
 * <p>Stored format: {@code v2:} + Base64(IV (12 bytes) || cipher text || tag (16 bytes)). A new random IV is
 * generated on every call, so the same plain text never produces the same value.
 */
@Helper
public final class CryptoHelper {

    static final String PREFIX = "v2:";

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final Pattern KEY_PATTERN = Pattern.compile("[A-Za-z0-9+/]{43}=");
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom random;

    public CryptoHelper(AdminProperties config, SecureRandom random) {
        this.random = random;
        this.key = new SecretKeySpec(decodeKey(config.getCrypto().getKey()), ALGORITHM);
    }

    @Named("encrypt")
    public String encrypt(String plainText) {
        var iv = new byte[IV_BYTES];
        random.nextBytes(iv);

        var cipherText = doFinal(Cipher.ENCRYPT_MODE, iv, plainText.getBytes(StandardCharsets.UTF_8), 0);
        var payload = ByteBuffer.allocate(IV_BYTES + cipherText.length).put(iv).put(cipherText).array();
        return PREFIX + Base64.getEncoder().encodeToString(payload);
    }

    @Named("decrypt")
    public String decrypt(String encoded) {
        if (!encoded.startsWith(PREFIX)) {
            throw new ApiException(ErrorCode.CRYPTO, "unsupported secret format");
        }

        byte[] payload;

        try {
            payload = Base64.getDecoder().decode(encoded.substring(PREFIX.length()));
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.CRYPTO, e);
        }

        if (payload.length < IV_BYTES + TAG_BITS / Byte.SIZE) {
            throw new ApiException(ErrorCode.CRYPTO, "malformed secret");
        }

        var iv = Arrays.copyOfRange(payload, 0, IV_BYTES);
        var plainText = doFinal(Cipher.DECRYPT_MODE, iv, payload, IV_BYTES);
        return new String(plainText, StandardCharsets.UTF_8);
    }

    private byte[] doFinal(int mode, byte[] iv, byte[] input, int offset) {
        try {
            var cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(mode, key, new GCMParameterSpec(TAG_BITS, iv));
            return cipher.doFinal(input, offset, input.length - offset);
        } catch (GeneralSecurityException e) {
            throw new ApiException(ErrorCode.CRYPTO, e);
        }
    }

    private static byte[] decodeKey(String encodedKey) {
        // Messages never include the configured value; the pattern check also avoids a parser message echoing it.
        if (encodedKey == null || !KEY_PATTERN.matcher(encodedKey).matches()) {
            throw new ApiException(ErrorCode.CRYPTO, "key must be the Base64 encoding of 32 random bytes");
        }

        return Base64.getDecoder().decode(encodedKey);
    }
}
