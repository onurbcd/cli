package com.onurbcd.eruservice.service.mapper;

import com.onurbcd.eruservice.dto.document.DocumentDto;
import com.onurbcd.eruservice.mapper.DefaultMapperConfig;
import com.onurbcd.eruservice.persistency.entity.Document;
import org.mapstruct.Mapper;

import java.util.function.Function;

@Mapper(config = DefaultMapperConfig.class)
public interface DocumentToDtoMapper extends Function<Document, DocumentDto> {
}
