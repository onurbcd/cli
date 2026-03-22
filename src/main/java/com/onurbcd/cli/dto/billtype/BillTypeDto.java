package com.onurbcd.cli.dto.billtype;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.onurbcd.cli.dto.PrimeDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BillTypeDto extends PrimeDto {

    private String path;
    private UUID categoryId;
    private String categoryName;
}
