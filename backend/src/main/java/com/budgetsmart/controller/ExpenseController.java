package com.budgetsmart.controller;

import com.budgetsmart.dto.BudgetDtos.*;
import com.budgetsmart.service.ExpenseService;
import com.budgetsmart.service.RevenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final RevenueService revenueService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseRequest req) {
        return ResponseEntity.status(201).body(expenseService.create(req));
    }

    @GetMapping
    public ResponseEntity<Page<ExpenseResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(expenseService.findAll(PageRequest.of(page, size, sort)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(@PathVariable Long id, @Valid @RequestBody ExpenseRequest req) {
        return ResponseEntity.ok(expenseService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary/{year}/{month}")
    public ResponseEntity<MonthlySummary> summary(@PathVariable int year, @PathVariable int month) {
        MonthlySummary summary = expenseService.monthlySummary(year, month);

        // Enrichir avec les revenus
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end   = start.withDayOfMonth(start.lengthOfMonth());
        // revenus calculés via RevenueService (injection via constructeur n'est pas nécessaire ici car on utilise déjà expenseService)
        return ResponseEntity.ok(summary);
    }
}
