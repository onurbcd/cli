package com.onurbcd.cli.dto.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.core.io.ByteArrayResource;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FileDto {

    private long contentLength;

    private ByteArrayResource resource;
}
