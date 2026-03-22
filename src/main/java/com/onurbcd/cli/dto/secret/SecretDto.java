package com.onurbcd.cli.dto.secret;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.onurbcd.cli.dto.PrimeDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SecretDto extends PrimeDto {

    private String description;
    private String link;
    private String username;
    private String password;
}
