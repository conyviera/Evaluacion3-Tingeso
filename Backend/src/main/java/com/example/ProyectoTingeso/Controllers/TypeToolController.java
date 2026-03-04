package com.example.ProyectoTingeso.Controllers;

import com.example.ProyectoTingeso.Entities.TypeToolEntity;
import com.example.ProyectoTingeso.Services.TypeToolService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/type-tools")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:8008",
        "http://localhost:8070",
        "http://localhost:3000"
})
public class TypeToolController {

    private static final String ERROR_INTERNO = "Error interno: ";

    private final TypeToolService typeToolService;

    public TypeToolController(TypeToolService typeToolService) {
        this.typeToolService = typeToolService;
    }

    /**
     * RF 4.1
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/configurationDailyRateTypeTool/{idTypeTool}")
    public ResponseEntity<Object> configurationDailyRateTypeTool(
            @PathVariable Long idTypeTool,
            @RequestBody Map<String, Object> payload) {
        try {
            int dailyRate = payload.get("dailyRate") != null ? ((Number) payload.get("dailyRate")).intValue() : -1;
            return ResponseEntity.ok(typeToolService.configurationDailyRateTypeTool(idTypeTool, dailyRate));

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ERROR_INTERNO + e.getMessage());
        }
    }

    /**
     * RF 4.2
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/configurationDebtTypeTool/{idTypeTool}")
    public ResponseEntity<Object> configurationDebtTypeTool(
            @PathVariable Long idTypeTool,
            @RequestBody Map<String, Object> payload) {
        try {
            int debtRate = payload.get("debtRate") != null ? ((Number) payload.get("debtRate")).intValue() : -1;
            return ResponseEntity.ok(typeToolService.configurationDebtTypeTool(idTypeTool, debtRate));

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ERROR_INTERNO + e.getMessage());
        }
    }

    /**
     * RF 4.3
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/registerReplacementTypeTool/{idTypeTool}")
    public ResponseEntity<Object> registerReplacementTypeTool(
            @PathVariable Long idTypeTool,
            @RequestBody Map<String, Object> payload) {
        try {
            int replacementValue = payload.get("replacementValue") != null
                    ? ((Number) payload.get("replacementValue")).intValue()
                    : -1;
            return ResponseEntity.ok(typeToolService.registerReplacementTypeTool(idTypeTool, replacementValue));

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ERROR_INTERNO + e.getMessage());
        }
    }

    /**
     * Helper 1
     */
    @GetMapping("/getTypeToolById/{idTypeTool}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getTypeToolById(@PathVariable Long idTypeTool) {
        try {
            return ResponseEntity.ok(typeToolService.getTypeToolById(idTypeTool));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ERROR_INTERNO + e.getMessage());
        }
    }

    /**
     * Helper 2
     */
    @GetMapping("/getAllTypeTools")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<TypeToolEntity>> getAllTool() {
        return ResponseEntity.ok(typeToolService.getAllTypeTools());
    }

    /**
     * Helper 3
     */
    @GetMapping("/getAllCategory")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<String>> getTypeToolCategory() {
        return ResponseEntity.ok(typeToolService.getAllTypeToolCategory());
    }
}