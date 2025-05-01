package com.onurbcd.eruservice.service.helper.impl;

import com.onurbcd.eruservice.service.enums.Error;
import com.onurbcd.eruservice.service.exception.ApiException;
import com.onurbcd.eruservice.config.property.AdminProperties;
import com.onurbcd.eruservice.service.helper.Cryptoable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.function.BiFunction;

@Component
@RequiredArgsConstructor
@Slf4j
public class CryptoHelper implements Cryptoable {

    private final AdminProperties config;

    @Override
    public String encrypt(String plainText) {
        return crypt(this::encrypt, plainText, Cipher.ENCRYPT_MODE);
    }

    @Override
    public String decrypt(String encodedCipherText) {
        return crypt(this::decrypt, encodedCipherText, Cipher.DECRYPT_MODE);
    }

    private String crypt(BiFunction<Cipher, String, String> function, String parameter, int mode) {
        try {
            var key = new SecretKeySpec(config.getCrypto().getKey1().getBytes(StandardCharsets.UTF_8),
                    config.getCrypto().getAlgorithm());
            var iv = new IvParameterSpec(config.getCrypto().getKey2().getBytes(StandardCharsets.UTF_8));
            var cipher = Cipher.getInstance(config.getCrypto().getTransformation());
            cipher.init(mode, key, iv);
            return function.apply(cipher, parameter);
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException |
                 NoSuchPaddingException e) {

            log.error("crypt", e);
            throw new ApiException(Error.CRYPTO, e.toString());
        }
    }

    private String encrypt(Cipher cipher, String plainText) {
        try {
            var cipherText = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(cipherText);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            log.error("encrypt", e);
            throw new ApiException(Error.CRYPTO, e.toString());
        }
    }

    private String decrypt(Cipher cipher, String encodedCipherText) {
        try {
            var cipherText = Base64.getDecoder().decode(encodedCipherText);
            var plainText = cipher.doFinal(cipherText);
            return new String(plainText);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            log.error("decrypt", e);
            throw new ApiException(Error.CRYPTO, e.toString());
        }
    }
}
