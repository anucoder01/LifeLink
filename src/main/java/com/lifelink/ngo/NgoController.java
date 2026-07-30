package com.lifelink.ngo;

import com.lifelink.common.dto.PaginatedResponse;
import com.lifelink.common.util.PaginationUtil;
import com.lifelink.ngo.dto.CreateNgoDto;
import com.lifelink.ngo.dto.NgoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Tag(name = "NGO", description = "NGO registration and management")
@RestController
@RequestMapping("/api/v1/ngos")
@RequiredArgsConstructor
public class NgoController {

    private final NgoService ngoService;

    @Operation(summary = "List all NGOs with pagination")
    @GetMapping
    public ResponseEntity<PaginatedResponse<NgoDto>> getAll(
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        Page<NgoDto> page = ngoService.getAll(pageable);
        return ResponseEntity.ok(PaginationUtil.fromPage(page));
    }

    @Operation(summary = "Get NGO by ID")
    @GetMapping("/{id}")
    public ResponseEntity<NgoDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ngoService.getById(id));
    }

    @Operation(summary = "Register a new NGO")
    @PostMapping
    public ResponseEntity<NgoDto> create(@Valid @RequestBody CreateNgoDto dto) {
        NgoDto created = ngoService.create(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Update NGO details")
    @PutMapping("/{id}")
    public ResponseEntity<NgoDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateNgoDto dto) {
        return ResponseEntity.ok(ngoService.update(id, dto));
    }

    @Operation(summary = "Delete an NGO")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        ngoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
