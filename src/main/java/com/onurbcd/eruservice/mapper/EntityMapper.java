package com.onurbcd.eruservice.mapper;

import com.onurbcd.eruservice.dto.Dtoable;
import com.onurbcd.eruservice.persistency.entity.Entityable;

import java.util.function.Function;

public interface EntityMapper<D extends Dtoable, E extends Entityable> extends Function<D, E> {
}
