package com.example.ProyectoTingeso.Controllers;

import com.example.ProyectoTingeso.Entities.CustomerEntity;
import com.example.ProyectoTingeso.Entities.LoanEntity;
import com.example.ProyectoTingeso.Repositories.CustomerRepository;
import com.example.ProyectoTingeso.Repositories.LoanRepository;
import com.example.ProyectoTingeso.Services.LoanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/loans")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:8008",
        "http://localhost:8070",
        "http://localhost:3000"
})
public class LoanController {

    private final LoanService loanService;
    private final CustomerRepository customerRepository;
    private final LoanRepository loanRepo;

    public LoanController(LoanService loanService, CustomerRepository customerRepository, LoanRepository loanRepo) {
        this.loanService = loanService;
        this.customerRepository = customerRepository;
        this.loanRepo = loanRepo;
    }

    /**
     * RF 2.1
     */
    @PostMapping("/createLoan")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Object> createLoan(@RequestBody Map<String, Object> payload) {
        try {
            Long customerId = ((Number) payload.get("customerId")).longValue();

            List<Long> typeToolIds = ((List<?>) payload.get("typeToolIds")).stream()
                    .map(id -> ((Number) id).longValue())
                    .toList();

            LocalDate deliveryDate = LocalDate.parse((String) payload.get("deliveryDate"));
            LocalDate returnDate = LocalDate.parse((String) payload.get("returnDate"));

            CustomerEntity customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + customerId));

            LoanEntity createdLoan = loanService.createLoan(typeToolIds, customer, deliveryDate, returnDate);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdLoan);

        } catch (IllegalArgumentException | jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Cliente no encontrado")) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocurrió un error al procesar el préstamo: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocurrió un error al procesar el préstamo: " + e.getMessage());
        }
    }

    /**
     * RF 2.3
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/return")
    public ResponseEntity<Object> returnLoan(
            @PathVariable("id") Long loanId,
            @RequestBody Map<String, Object> payload) {

        Object toolStatesObject = payload.get("toolStates");

        if (!(toolStatesObject instanceof List)) {
            return ResponseEntity.badRequest().body("El campo 'toolStates' es obligatorio y debe ser una lista.");
        }

        List<?> rawToolStates = (List<?>) toolStatesObject;
        Map<Long, String> processedToolStates = new HashMap<>();

        for (Object item : rawToolStates) {
            if (!(item instanceof Map)) {
                return ResponseEntity.badRequest()
                        .body("Cada elemento en 'toolStates' debe ser un objeto con 'toolId' y 'state'.");
            }
            Map<String, Object> toolStateMap = (Map<String, Object>) item;

            Object toolIdObj = toolStateMap.get("toolId");
            Object stateObj = toolStateMap.get("state");

            if (!(toolIdObj instanceof Number) || !(stateObj instanceof String)) {
                return ResponseEntity.badRequest()
                        .body("Cada objeto debe tener 'toolId' (un número) y 'state' (un texto).");
            }

            Long toolId = ((Number) toolIdObj).longValue();
            String state = (String) stateObj;
            processedToolStates.put(toolId, state);
        }

        LoanEntity updatedLoan = loanService.toolReturn(loanId, processedToolStates);
        return ResponseEntity.ok(updatedLoan);
    }

    @PostMapping("/calculateRentalAmount")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Object> calculateRentalAmount(@RequestBody Map<String, Object> payload) {
        try {
            List<Long> typeToolIds = ((List<?>) payload.get("typeToolIds")).stream()
                    .map(id -> ((Number) id).longValue())
                    .toList();

            LocalDate deliveryDate = LocalDate.parse((String) payload.get("deliveryDate"));
            LocalDate returnDate = LocalDate.parse((String) payload.get("returnDate"));

            int amount = loanService.calculateRentalAmount(typeToolIds, deliveryDate, returnDate);
            return ResponseEntity.status(HttpStatus.CREATED).body(amount);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocurrió un error al procesar " + e.getMessage());
        }
    }

    /**
     * RF 6.1
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/loanActiveAndExpire")
    public ResponseEntity<List<LoanEntity>> getloanActiveAndExpire() {
        List<LoanEntity> loans = loanService.loanActiveAndExpire();
        return ResponseEntity.ok(loans);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}")
    public ResponseEntity<LoanEntity> getLoanById(@PathVariable("id") Long loanId) {
        Optional<LoanEntity> loanOptional = loanRepo.findById(loanId);
        if (loanOptional.isPresent()) {
            return ResponseEntity.ok(loanOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/report")
    public ResponseEntity<List<Map<String, Object>>> getTopToolsReport() {
        return ResponseEntity.ok(loanService.getTopToolsReport());
    }

    @GetMapping("/reportDate")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Object> getTopToolsReportDate(@RequestParam("startDate") String startDateParam,
            @RequestParam("endDate") String endDateParam) {
        try {
            LocalDate startDate = LocalDate.parse(startDateParam);
            LocalDate endDate = LocalDate.parse(endDateParam);
            return ResponseEntity.ok(loanService.getTopToolsReportDate(startDate, endDate));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getAllLoans")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<LoanEntity>> getAllLoans() {
        List<LoanEntity> loans = loanRepo.findAll();
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/countLoansActive")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Integer> countActiveLoans() {
        int number = loanService.countLoansByState("ACTIVE");
        return ResponseEntity.ok(number);
    }

    @GetMapping("/countLoansExpired")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Integer> countExpiredLoans() {
        int number = loanService.countLoansByState("EXPIRED");
        return ResponseEntity.ok(number);
    }

    @GetMapping("/loanActiveAndExpireFilterDate")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Object> loanActiveAndExpireFilterDate(@RequestParam("startDate") String startDateParam,
            @RequestParam("endDate") String endDateParam) {
        try {
            LocalDate startDate = LocalDate.parse(startDateParam);
            LocalDate endDate = LocalDate.parse(endDateParam);
            List<LoanEntity> loans = loanService.loanActiveAndExpireFilterDate(startDate, endDate);
            return ResponseEntity.ok(loans);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/countByDeliveryDateBetweenActive")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Object> countByDeliveryDateBetweenActive(@RequestParam("startDate") String startDateParam,
            @RequestParam("endDate") String endDateParam) {
        try {
            LocalDate startDate = LocalDate.parse(startDateParam);
            LocalDate endDate = LocalDate.parse(endDateParam);
            int number = loanService.countLoansByDeliveryDateBetweenAndState(startDate, endDate, "ACTIVE");
            return ResponseEntity.ok(number);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/countByDeliveryDateBetweenExpired")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Object> countByDeliveryDateBetweenExpired(@RequestParam("startDate") String startDateParam,
            @RequestParam("endDate") String endDateParam) {
        try {
            LocalDate startDate = LocalDate.parse(startDateParam);
            LocalDate endDate = LocalDate.parse(endDateParam);
            int number = loanService.countLoansByDeliveryDateBetweenAndState(startDate, endDate, "EXPIRED");
            return ResponseEntity.ok(number);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
