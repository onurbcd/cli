package com.onurbcd.cli.param;

import lombok.*;

import java.io.InputStream;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MultipartFile {

    private String originalFilename;
    private String contentType;
    private Long size;
    private InputStream inputStream;

    public boolean isEmpty() {
        return size == 0;
    }
}
