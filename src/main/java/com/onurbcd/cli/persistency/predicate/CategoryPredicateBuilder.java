package com.onurbcd.cli.persistency.predicate;

import com.onurbcd.cli.dto.filter.CategoryFilter;
import com.onurbcd.cli.persistency.entity.QCategory;
import com.querydsl.core.types.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.util.UUID;

public class CategoryPredicateBuilder extends BasePredicateBuilder {

    public CategoryPredicateBuilder() {
        super(QCategory.category.name, QCategory.category.active);
    }

    public static Predicate id(UUID id) {
        return new CategoryPredicateBuilder().idEq(id).build();
    }

    public static Predicate all(CategoryFilter filter) {
        return new CategoryPredicateBuilder()
                .parentId(filter.getParentId())
                .level(filter.getLevel())
                .lastBranch(filter.getLastBranch())
                .search(filter.getSearch())
                .active(filter.isActive())
                .build();
    }

    @Override
    public CategoryPredicateBuilder search(String search) {
        if (StringUtils.isNotBlank(search)) {
            builder().and(QCategory.category.name.containsIgnoreCase(search)
                    .or(QCategory.category.description.containsIgnoreCase(search)));
        }

        return this;
    }

    private CategoryPredicateBuilder idEq(UUID id) {
        builder().and(QCategory.category.id.eq(id));
        return this;
    }

    private CategoryPredicateBuilder parentId(@Nullable UUID parentId) {
        if (parentId != null) {
            builder().and(QCategory.category.parent.id.eq(parentId));
        }

        return this;
    }

    private CategoryPredicateBuilder level(@Nullable Short level) {
        if (level != null) {
            builder().and(QCategory.category.level.eq(level));
        }

        return this;
    }

    private CategoryPredicateBuilder lastBranch(@Nullable Boolean lastBranch) {
        if (lastBranch != null) {
            builder().and(QCategory.category.lastBranch.eq(lastBranch));
        }

        return this;
    }
}
