package com.onurbcd.cli.dto.secret;

import com.onurbcd.cli.dto.PrimeDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SecretDto extends PrimeDto {

    private String description;
    private String link;
    private String username;
    private String password;
}
