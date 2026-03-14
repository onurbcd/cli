package com.onurbcd.eruservice.dto.secret;

import com.onurbcd.eruservice.dto.PrimeSaveDto;
import com.onurbcd.eruservice.util.Constant;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.URL;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.validation.annotation.Validated;

import static com.onurbcd.eruservice.util.Constant.*;
import static com.onurbcd.eruservice.util.ParamUtil.getBoolean;
import static com.onurbcd.eruservice.util.FlowUtil.getString;
import static com.onurbcd.eruservice.util.StringUtil.normalizeSpace;

@SuperBuilder
@Getter
@Setter
@Validated
public class SecretSaveDto extends PrimeSaveDto {

    @Size(min = 5, max = 250, message = "Description must be between {min} and {max} characters.")
    private String description;

    @Size(min = 7, max = 2048, message = "Link must be between {min} and {max} characters.")
    @URL(regexp = Constant.REGEXP_URL, message = "Link must be a valid URL.")
    private String link;

    @NotNull(message = "Username is required.")
    @Size(min = 3, max = 50, message = "Username must be between {min} and {max} characters.")
    private String username;

    @NotNull(message = "Password is required.")
    private String password;

    public static SecretSaveDto of(ComponentContext<?> context, @Nullable SecretDto secretDto) {
        return SecretSaveDto.builder()
                .name(normalizeSpace(getString(context, NAME_ID)))
                .active(getBoolean(secretDto, SecretDto::isActive))
                .description(normalizeSpace(getString(context, DESCRIPTION_ID)))
                .link(normalizeSpace(getString(context, LINK_ID)))
                .username(normalizeSpace(getString(context, USERNAME_ID)))
                .password(normalizeSpace(getString(context, PASSWORD_ID)))
                .build();
    }
}
