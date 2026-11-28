package com.onurbcd.cli.config.property;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CryptoProperties {

    /** Base64 encoding of the 32 random bytes of the AES-256 key. */
    private String key;
}
