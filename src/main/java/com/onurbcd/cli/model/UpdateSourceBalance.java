package com.onurbcd.cli.model;

import com.onurbcd.cli.persistency.entity.Source;
import lombok.*;

import java.math.BigDecimal;
import java.util.function.BinaryOperator;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UpdateSourceBalance {

    private Source source;
    private BinaryOperator<BigDecimal> func;
    private BigDecimal value;
}
