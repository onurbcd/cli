package com.onurbcd.cli.persistency.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
public class Prime extends Audit implements Entityable {

    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(unique = true)
    @NotNull
    @Size(min = 3, max = 50)
    @Getter
    @Setter
    private String name;

    @NotNull
    @Getter
    private Boolean active;

    @Override
    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Prime prime = (Prime) o;

        return new EqualsBuilder().append(id, prime.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }
}
