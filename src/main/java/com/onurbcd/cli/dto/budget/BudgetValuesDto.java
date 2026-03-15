package com.onurbcd.cli.dto.budget;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetValuesDto(BigDecimal amount, UUID billTypeId, String path, Boolean paid) {
}
