package com.budgetsmart.controller;

import com.budgetsmart.dto.BudgetDtos.*;
import com.budgetsmart.service.RevenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/revenues")
@RequiredArgsConstructor
public class RevenueController {

    private final RevenueService revenueService;

    @PostMapping
    public ResponseEntity<RevenueResponse> create(@Valid @RequestBody RevenueRequest req) {
        return ResponseEntity.status(201).body(revenueService.create(req));
    }

    @GetMapping
    public ResponseEntity<Page<RevenueResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(revenueService.findAll(PageRequest.of(page, size, sort)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RevenueResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(revenueService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RevenueResponse> update(@PathVariable Long id, @Valid @RequestBody RevenueRequest req) {
        return ResponseEntity.ok(revenueService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        revenueService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
