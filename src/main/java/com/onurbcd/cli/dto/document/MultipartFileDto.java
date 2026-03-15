package com.onurbcd.cli.dto.document;

import com.onurbcd.cli.model.MultipartFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MultipartFileDto {

    private String path;

    private MultipartFile[] multipartFiles;

    private String name;

    private MultipartFile multipartFile;
}
