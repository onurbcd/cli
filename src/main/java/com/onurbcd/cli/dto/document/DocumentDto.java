package com.onurbcd.cli.dto.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.onurbcd.cli.util.FileUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DocumentDto {

    private UUID id;
    private String name;
    private String path;
    private String mimeType;
    private Long size;
    private String hash;
    private String storagePath;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String hyperlink;

    public DocumentDto(UUID id, String name, String path, String mimeType, Long size, String hash) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.mimeType = mimeType;
        this.size = size;
        this.hash = hash;
    }

    public String getHyperlink() {
        return FileUtil.getHyperlink(this);
    }

    @JsonIgnore
    public boolean isNotEmpty() {
        return id != null;
    }
}
