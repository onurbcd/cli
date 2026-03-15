package com.onurbcd.cli.dto.filter;

public interface Filterable {

    Boolean isActive();

    void setActive(Boolean active);

    String getSearch();

    void setSearch(String search);
}
