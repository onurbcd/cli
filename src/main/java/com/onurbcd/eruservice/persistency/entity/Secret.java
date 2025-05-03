package com.onurbcd.eruservice.persistency.entity;

import com.onurbcd.eruservice.util.Constant;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.URL;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Secret extends Prime {

    @Size(min = 5, max = 250)
    private String description;

    @URL(regexp = Constant.REGEXP_URL)
    @Size(min = 7, max = 2048)
    private String link;

    @NotNull
    @Size(min = 3, max = 50)
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
