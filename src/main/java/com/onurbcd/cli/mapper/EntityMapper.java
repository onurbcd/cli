package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.persistency.entity.Entityable;

import java.util.function.Function;

public interface EntityMapper<D extends Dtoable, E extends Entityable> extends Function<D, E> {
}
