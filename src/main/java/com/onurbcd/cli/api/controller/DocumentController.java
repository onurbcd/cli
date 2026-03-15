package com.onurbcd.cli.api.controller;

/*import com.onurbcd.cli.dto.document.DocumentDto;
import com.onurbcd.cli.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<DocumentDto> getAll(@PageableDefault(sort = "name") Pageable pageable) {
        return service.getAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getFile(@PathVariable("id") UUID id) {
        var fileDto = service.getFile(id);

        return ResponseEntity
                .ok()
                .headers(fileDto.getHeaders())
                .contentLength(fileDto.getContentLength())
                .contentType(fileDto.getContentType())
                .body(fileDto.getResource());
    }
}*/
