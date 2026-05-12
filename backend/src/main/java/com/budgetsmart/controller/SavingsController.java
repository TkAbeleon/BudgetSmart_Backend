package com.budgetsmart.controller;

import com.budgetsmart.dto.BudgetDtos.*;
import com.budgetsmart.service.SavingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/savings")
@RequiredArgsConstructor
public class SavingsController {

    private final SavingsService savingsService;

    @PostMapping
    public ResponseEntity<SavingsResponse> create(@Valid @RequestBody SavingsRequest req) {
        return ResponseEntity.status(201).body(savingsService.create(req));
    }

    @GetMapping
    public ResponseEntity<List<SavingsResponse>> list() {
        return ResponseEntity.ok(savingsService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingsResponse> get(@PathVariable Integer id) {
        return ResponseEntity.ok(savingsService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsResponse> update(@PathVariable Integer id,
                                                   @Valid @RequestBody SavingsRequest req) {
        return ResponseEntity.ok(savingsService.update(id, req));
    }

    @PostMapping("/{id}/add")
    public ResponseEntity<SavingsResponse> addAmount(@PathVariable Integer id,
                                                      @Valid @RequestBody SavingsAddRequest req) {
        return ResponseEntity.ok(savingsService.addAmount(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        savingsService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
