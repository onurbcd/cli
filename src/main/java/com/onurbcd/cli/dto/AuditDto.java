package com.onurbcd.cli.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class AuditDto {

    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
