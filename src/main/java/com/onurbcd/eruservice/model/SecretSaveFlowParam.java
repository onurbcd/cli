package com.onurbcd.eruservice.model;

import com.onurbcd.eruservice.dto.secret.SecretDto;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;

import static com.onurbcd.eruservice.util.ParamUtil.getString;

@Builder
@Getter
public class SecretSaveFlowParam {

    private String name;
    private String description;
    private String link;
    private String username;
    private String password;

    public static SecretSaveFlowParam of(@Nullable SecretDto secretDto) {
        return SecretSaveFlowParam.builder()
                .name(getString(secretDto, SecretDto::getName))
                .description(getString(secretDto, SecretDto::getDescription))
                .link(getString(secretDto, SecretDto::getLink))
                .username(getString(secretDto, SecretDto::getUsername))
                .password(getString(secretDto, SecretDto::getPassword))
                .build();
    }
}
