package com.example.ProyectoTingeso.Controllers;

import com.example.ProyectoTingeso.Entities.DebtsEntity;
import com.example.ProyectoTingeso.Entities.ToolEntity;
import com.example.ProyectoTingeso.Services.DebtsService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/debts")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:8008",
        "http://localhost:8070",
        "http://localhost:3000"
})
public class DebtsController {

    private final DebtsService debtsService;
    public DebtsController(DebtsService debtsService) { this.debtsService = debtsService; }

    @PutMapping("/assessToolDamage/{idTool}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assessToolDamage(@PathVariable Long idTool,
                                               @RequestBody Map<String, Object> payload
    ) {
        try {

            if (!payload.containsKey("outcome") || payload.get("outcome") == null) {
                return ResponseEntity.badRequest().body("El campo 'outcome' es obligatorio y no puede ser nulo.");
            }
            if (!(payload.get("outcome") instanceof String)) {
                return ResponseEntity.badRequest().body("El campo 'outcome' debe ser de tipo String.");
            }
            String outcome = (String) payload.get("outcome");


            if (!payload.containsKey("damageCharge") || payload.get("damageCharge") == null) {
                return ResponseEntity.badRequest().body("El campo 'damageCharge' es obligatorio y no puede ser nulo.");
            }
            if (!(payload.get("damageCharge") instanceof Integer)) {
                return ResponseEntity.badRequest().body("El campo 'damageCharge' debe ser un número entero (Integer).");
            }
            int damageCharge = (Integer) payload.get("damageCharge");

            if (damageCharge < 0) {
                return ResponseEntity.badRequest().body("El campo 'damageCharge' no puede ser negativo.");
            }

            ToolEntity updatedTool = debtsService.assessToolDamage(idTool, outcome, damageCharge);
            return ResponseEntity.ok(updatedTool);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor: " + e.getMessage());
        }
    }

    /** Marca deuda como pagada (usa el id) */
    @PutMapping("/pay/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<DebtsEntity> pay(@PathVariable("id") Long idDebts) {
        return ResponseEntity.ok(debtsService.debtPaid(idDebts));
    }

    @GetMapping("/loan/{idLoan}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<DebtsEntity>> getDebtsByLoanId(@PathVariable Long idLoan) {
        List<DebtsEntity> debts = debtsService.getDebtsByIdLoan(idLoan);
        return ResponseEntity.ok(debts);
    }

}
