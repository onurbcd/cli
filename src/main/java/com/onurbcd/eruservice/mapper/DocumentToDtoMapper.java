package com.onurbcd.eruservice.mapper;

import com.onurbcd.eruservice.dto.document.DocumentDto;
import com.onurbcd.eruservice.persistency.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.function.Function;

@Mapper(config = DefaultMapperConfig.class)
public interface DocumentToDtoMapper extends Function<Document, DocumentDto> {

    @Override
    @Mapping(target = "storagePath", ignore = true)
    @Mapping(target = "hyperlink", ignore = true)
    DocumentDto apply(Document document);
}
