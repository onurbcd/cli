package com.onurbcd.eruservice.persistency.entity;

import com.onurbcd.eruservice.constant.DtoConstant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

@Entity
@Table(name = "document")
@NoArgsConstructor
@Getter
@Setter
public class Document implements Persistable<UUID> {

    @Id
    @GeneratedValue(generator = "uuid")
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @Size(max = DtoConstant.SIZE_100)
    @Column(name = "name", nullable = false, updatable = false, length = DtoConstant.SIZE_100)
    private String name;

    @NotNull
    @Size(max = DtoConstant.SIZE_250)
    @Column(name = "path", nullable = false, updatable = false, length = DtoConstant.SIZE_250)
    private String path;

    @NotNull
    @Size(max = DtoConstant.SIZE_255)
    @Column(name = "mime_type", nullable = false, updatable = false)
    private String mimeType;

    @NotNull
    @Column(name = "size", nullable = false, updatable = false)
    private Long size;

    @NotNull
    @Size(max = DtoConstant.SIZE_32)
    @Column(name = "hash", nullable = false, updatable = false, length = DtoConstant.SIZE_32)
    private String hash;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        var document = (Document) o;
        return new EqualsBuilder().append(id, document.id).isEquals();
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
