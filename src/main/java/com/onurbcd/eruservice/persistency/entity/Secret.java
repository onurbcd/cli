package com.onurbcd.eruservice.persistency.entity;

import com.onurbcd.eruservice.constant.DtoConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.URL;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Secret extends Prime {

    @Size(min = DtoConstant.SIZE_5, max = DtoConstant.SIZE_250)
    private String description;

    @URL(regexp = DtoConstant.REGEXP_URL)
    @Size(min = DtoConstant.SIZE_7, max = DtoConstant.SIZE_2048)
    private String link;

    @NotNull
    @Size(min = DtoConstant.SIZE_3, max = DtoConstant.SIZE_50)
    private String username;

    @NotNull
    private String password;

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
