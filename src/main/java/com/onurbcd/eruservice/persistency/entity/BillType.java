package com.onurbcd.eruservice.persistency.entity;

import com.onurbcd.eruservice.constant.DtoConstant;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class BillType extends Prime {

    @NotNull
    @Size(min = DtoConstant.SIZE_3, max = DtoConstant.SIZE_250)
    @Pattern(regexp = DtoConstant.REGEXP_PATH)
    @Column(name = "path", nullable = false, length = DtoConstant.SIZE_250)
    private String path;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
