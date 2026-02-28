package com.example.ProyectoTingeso.Controllers;

import com.example.ProyectoTingeso.Entities.CustomerEntity;
import com.example.ProyectoTingeso.Entities.LoanEntity;
import com.example.ProyectoTingeso.Entities.TypeToolEntity;
import com.example.ProyectoTingeso.Repositories.CustomerRepository;
import com.example.ProyectoTingeso.Repositories.LoanRepository;
import com.example.ProyectoTingeso.Repositories.ToolRepository;
import com.example.ProyectoTingeso.Repositories.TypeToolRepository;
import com.example.ProyectoTingeso.Services.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/loans")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:8008",
        "http://localhost:8070",
        "http://localhost:3000"
})

public class LoanController {

    @Autowired
    LoanService loanService;

    private CustomerRepository customerRepository;
    private TypeToolRepository typeToolRepository;
    private ToolRepository toolRepository;
    private LoanRepository loanRepo;

    public LoanController(LoanService loanService, CustomerRepository customerRepository, LoanRepository loanRepo,
            ToolRepository toolRepository) {
        this.loanService = loanService;
        this.customerRepository = customerRepository;
        this.loanRepo = loanRepo;
        this.toolRepository = toolRepository;
    }

    /**
     * RF 2.1
     * 
     * @param payload
     * @return
     */
    @PostMapping("/createLoan")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> createLoan(@RequestBody Map<String, Object> payload) {

        try {
            Long customerId = ((Number) payload.get("customerId")).longValue();

            List<Long> typeToolIds = ((List<?>) payload.get("typeToolIds")).stream()
                    .map(id -> ((Number) id).longValue())
                    .collect(Collectors.toList());

            LocalDate deliveryDate = LocalDate.parse((String) payload.get("deliveryDate"));
            LocalDate returnDate = LocalDate.parse((String) payload.get("returnDate"));

            CustomerEntity customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + customerId));

            LoanEntity createdLoan = loanService.createLoan(typeToolIds, customer, deliveryDate, returnDate);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdLoan);

        } catch (IllegalArgumentException | jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Cliente no encontrado")) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocurrió un error al procesar el préstamo: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocurrió un error al procesar el préstamo: " + e.getMessage());
        }
    }

    /**
     * RF 2.3
     * 
     * @param loanId
     * @param payload
     * @return
     */

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/return")

    public ResponseEntity<?> returnLoan(
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

    @PostMapping("/RentalAmount")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> RentalAmount(@RequestBody Map<String, Object> payload) {
        try {
            List<Long> typeToolIds = ((List<?>) payload.get("typeToolIds")).stream()
                    .map(id -> ((Number) id).longValue())
                    .collect(Collectors.toList());

            LocalDate deliveryDate = LocalDate.parse((String) payload.get("deliveryDate"));
            LocalDate returnDate = LocalDate.parse((String) payload.get("returnDate"));

            int amount = loanService.RentalAmount(typeToolIds, deliveryDate, returnDate);

            return ResponseEntity.status(HttpStatus.CREATED).body(amount);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocurrió un error al procesar " + e.getMessage());
        }

    }

    /**
     * RF 6.1
     * 
     * @return
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
        // Buscamos el préstamo en la base de datos usando el repositorio
        Optional<LoanEntity> loanOptional = loanRepo.findById(loanId);

        // Verificamos si el préstamo fue encontrado
        if (loanOptional.isPresent()) {
            // Si existe, lo devolvemos con un estado 200 OK
            return ResponseEntity.ok(loanOptional.get());
        } else {
            // Si no existe, devolvemos un estado 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/report")
    public ResponseEntity<List<Map<String, Object>>> getTopToolsReport() {
        return ResponseEntity.ok(loanService.getTopToolsReport());
    }

    // Entrega todos los prestamos
    @GetMapping("/getAllLoans")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<LoanEntity>> getAllLoans() {

        List<LoanEntity> loans = loanRepo.findAll();

        return ResponseEntity.ok(loans);
    }

    @GetMapping("/countLoans")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> countActiveLoans() {

        int number = loanService.countLoansByState();

        return ResponseEntity.ok(number);
    }

}
