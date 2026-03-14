package com.onurbcd.eruservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.validation.annotation.Validated;

@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@Validated
public abstract class PrimeSaveDto implements Dtoable {

    @NotBlank(message = "Name is required.")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters.")
    private String name;

    @Getter(AccessLevel.NONE)
    private Boolean active;

    @Override
    public Boolean isActive() {
        return active;
    }
}
