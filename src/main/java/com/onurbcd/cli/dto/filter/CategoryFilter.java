package com.onurbcd.cli.dto.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@SuperBuilder
@Getter
@Setter
public class CategoryFilter extends AbstractFilterable {

    private UUID parentId;
    private Short level;
    private Boolean lastBranch;

    public static CategoryFilter of(Boolean active, String search, UUID parentId, Short level, Boolean lastBranch) {
        return CategoryFilter
                .builder()
                .active(active)
                .search(search)
                .parentId(parentId)
                .level(level)
                .lastBranch(lastBranch)
                .build();
    }
}
