package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.document.DocumentDto;
import com.onurbcd.cli.persistency.entity.Document;
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
