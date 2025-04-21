package com.onurbcd.eruservice.api.controller;

/*import com.onurbcd.eruservice.dto.budget.BudgetPatchDto;
import com.onurbcd.eruservice.dto.budget.BudgetSaveDto;
import com.onurbcd.eruservice.dto.budget.CopyBudgetDto;
import com.onurbcd.eruservice.dto.budget.SumDto;
import com.onurbcd.eruservice.dto.enums.Direction;
import com.onurbcd.eruservice.service.BudgetService;
import com.onurbcd.eruservice.dto.filter.BudgetFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/budget")
public class BudgetController extends PrimeController<BudgetSaveDto, BudgetPatchDto, BudgetFilter>
        implements SequenceApi {

    private final BudgetService budgetService;

    public BudgetController(BudgetService service) {
        super(service);
        this.budgetService = service;
    }

    @Override
    public void swapPosition(UUID id, Short targetSequence) {
        budgetService.swapPosition(id, targetSequence);
    }

    @GetMapping("/sum-month")
    @ResponseStatus(HttpStatus.OK)
    public Set<SumDto> getSumByMonth(BudgetFilter filter) {
        return budgetService.getSumByMonth(filter);
    }

    @PostMapping("/copy")
    @ResponseStatus(HttpStatus.CREATED)
    public void copy(@RequestBody CopyBudgetDto copyBudgetDto) {
        budgetService.copy(copyBudgetDto);
    }

    @DeleteMapping("/{year}/{month}/all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll(@PathVariable("year") Short refYear, @PathVariable("month") Short refMonth) {
        budgetService.deleteAll(refYear, refMonth);
    }
}*/
