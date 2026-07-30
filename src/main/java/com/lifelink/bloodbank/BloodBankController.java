package com.lifelink.bloodbank;

import com.lifelink.bloodbank.dto.BloodBankDto;
import com.lifelink.bloodbank.dto.CreateBloodBankDto;
import com.lifelink.common.dto.PaginatedResponse;
import com.lifelink.common.util.PaginationUtil;
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

@Tag(name = "Blood Bank", description = "Blood bank registration and management")
@RestController
@RequestMapping("/api/v1/blood-banks")
@RequiredArgsConstructor
public class BloodBankController {

    private final BloodBankService bloodBankService;

    @Operation(summary = "List all blood banks with pagination")
    @GetMapping
    public ResponseEntity<PaginatedResponse<BloodBankDto>> getAll(
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        Page<BloodBankDto> page = bloodBankService.getAll(pageable);
        return ResponseEntity.ok(PaginationUtil.fromPage(page));
    }

    @Operation(summary = "Get blood bank by ID")
    @GetMapping("/{id}")
    public ResponseEntity<BloodBankDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(bloodBankService.getById(id));
    }

    @Operation(summary = "Register a new blood bank")
    @PostMapping
    public ResponseEntity<BloodBankDto> create(@Valid @RequestBody CreateBloodBankDto dto) {
        BloodBankDto created = bloodBankService.create(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Update blood bank details")
    @PutMapping("/{id}")
    public ResponseEntity<BloodBankDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateBloodBankDto dto) {
        return ResponseEntity.ok(bloodBankService.update(id, dto));
    }

    @Operation(summary = "Delete a blood bank")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        bloodBankService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
