package com.onurbcd.cli.dto.filter;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class AbstractFilterable implements Filterable {

    private Boolean active;
    private String search;

    @Override
    public Boolean isActive() {
        return active;
    }

    @Override
    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String getSearch() {
        return search != null ? search.trim() : null;
    }

    @Override
    public void setSearch(String search) {
        this.search = search;
    }
}
