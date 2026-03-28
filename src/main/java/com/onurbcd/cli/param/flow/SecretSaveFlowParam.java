package com.onurbcd.cli.param.flow;

import com.onurbcd.cli.dto.secret.SecretDto;
import com.onurbcd.cli.enums.FlowType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;

import static com.onurbcd.cli.util.ParamUtil.getString;

@Builder
@Getter
public class SecretSaveFlowParam implements Paramable {

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

    @Override
    public FlowType getType() {
        return FlowType.SECRET;
    }
}
