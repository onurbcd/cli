package com.onurbcd.cli.persistency.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.domain.Persistable;
import org.springframework.data.util.ProxyUtils;

import java.util.UUID;

@Entity
@Table(name = "document")
@NoArgsConstructor
@Getter
@Setter
public class Document implements Persistable<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Size(max = 100)
    @Column(name = "name", nullable = false, updatable = false, length = 100)
    private String name;

    @NotNull
    @Size(max = 250)
    @Column(name = "path", nullable = false, updatable = false, length = 250)
    private String path;

    @NotNull
    @Size(max = 255)
    @Column(name = "mime_type", nullable = false, updatable = false)
    private String mimeType;

    @NotNull
    @Column(name = "size", nullable = false, updatable = false)
    private Long size;

    @NotNull
    @Size(max = 32)
    @Column(name = "hash", nullable = false, updatable = false, length = 32)
    private String hash;

    @Override
    public boolean equals(Object o) {
        // getId(), not the field: for a Hibernate proxy the field is null
        return this == o || (o instanceof Document document && ProxyUtils.getUserClass(this) == ProxyUtils.getUserClass(o)
                && new EqualsBuilder().append(getId(), document.getId()).isEquals());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }

    @Override
    public UUID getId() {
        return id;
    }

    /**
     * Prevent Spring Data doing a select-before-insert - this particular entity is never updated.
     *
     * @return true
     */
    @Override
    public boolean isNew() {
        return true;
    }
}
