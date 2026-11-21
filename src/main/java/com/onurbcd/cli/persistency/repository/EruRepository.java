package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.ItemDto;
import com.onurbcd.cli.persistency.entity.Entityable;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface EruRepository<E extends Entityable, D extends Dtoable> extends JpaRepository<E, UUID>,
        QuerydslPredicateExecutor<E>, CustomRepository<D, E> {

    default int deleteUsingId(@Param("id") UUID id) {
        return 0;
    }

    int updateActive(@Param("id") UUID id, @Param("active") Boolean active);

    default Optional<E> get(@Param("id") UUID id) {
        return Optional.empty();
    }

    default List<ItemDto> getItems(@Nullable @Param("id") UUID id) {
        return Collections.emptyList();
    }
}
