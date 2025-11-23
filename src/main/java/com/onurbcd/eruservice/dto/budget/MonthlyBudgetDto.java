package com.onurbcd.eruservice.dto.budget;

import java.util.UUID;

public record MonthlyBudgetDto(UUID id, String name, Short sequence) {

    public String compoundName() {
        return sequence + " - " + name;
    }
}
