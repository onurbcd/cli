package com.onurbcd.cli.api.controller;

/*import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.filter.Filterable;
import com.onurbcd.cli.service.CrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.validation.Valid;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RequiredArgsConstructor
@Validated
public class PrimeController<S extends Dtoable, P extends Dtoable, F extends Filterable> {

    protected final CrudService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void post(@Valid @RequestBody S dto) {
        service.save(dto, null);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void put(@PathVariable("id") UUID id, @Valid @RequestBody S dto) {
        service.save(dto, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") UUID id) {
        service.delete(id);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Dtoable get(@PathVariable("id") UUID id) {
        return service.getById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<Dtoable> getAll(@PageableDefault(sort = "name") Pageable pageable, F filter) {
        return service.getAll(pageable, filter);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void patch(@PathVariable("id") UUID id, @Valid @RequestBody P dto) {
        service.update(dto, id);
    }

    protected ResponseEntity<Void> getCreated(UUID id) {
        var location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(location).build();
    }
}*/
