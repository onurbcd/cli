package com.onurbcd.cli.dto.budget;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class RefDto {

    private Short year;
    private Short month;

    public static RefDto of(Short year, Short month) {
        return new RefDto(year, month);
    }
}
