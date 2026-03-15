package com.onurbcd.cli.persistency.entity;

import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "income_source")
@NoArgsConstructor
@SuperBuilder
public class IncomeSource extends Prime {

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
