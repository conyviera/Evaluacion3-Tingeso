package com.example.ProyectoTingeso.Controllers;

import com.example.ProyectoTingeso.Entities.ToolEntity;
import com.example.ProyectoTingeso.Services.ToolService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tool")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:8008",
        "http://localhost:8070",
        "http://localhost:3000"
})
public class ToolController {

    private final ToolService toolService;

    public ToolController(ToolService toolService) {
        this.toolService = toolService;
    }

    /**
     * RF: 1.1
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<Object> registerTool(@RequestBody Map<String, Object> payload) {
        try {
            String name = (String) payload.get("name");
            String category = (String) payload.get("category");

            int replacementValue = payload.get("replacementValue") != null
                    ? ((Number) payload.get("replacementValue")).intValue()
                    : -1;
            int dailyRate = payload.get("dailyRate") != null ? ((Number) payload.get("dailyRate")).intValue() : -1;
            int debtRate = payload.get("debtRate") != null ? ((Number) payload.get("debtRate")).intValue() : -1;
            int quantity = payload.get("quantity") != null ? ((Number) payload.get("quantity")).intValue() : -1;

            List<ToolEntity> newTools = toolService.registerLotTool(name, category, replacementValue, dailyRate,
                    debtRate, quantity);
            return ResponseEntity.ok(newTools);

        } catch (IllegalArgumentException | IllegalStateException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error interno: " + e.getMessage());
        }
    }

    /**
     * RF 1.2: Changes the status of a tool to "DECOMMISSIONED."
     */
    @PutMapping("/deactivateUnusedTool/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deactivateUnusedTool(@PathVariable("id") Long idTool) {
        try {
            ToolEntity tool = toolService.deactivateUnusedTool(idTool);
            return ResponseEntity.ok(tool);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocurrió un error inesperado: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/findAllbyTypeTool/{id}")
    public ResponseEntity<Object> findAllByTypeTool(@PathVariable("id") Long idTypeTool) {
        try {
            List<ToolEntity> toolList = toolService.findAllByTypeTool(idTypeTool);
            return ResponseEntity.ok(toolList);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al buscar herramientas: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/findById/{id}")
    public ResponseEntity<Object> findByIdTool(@PathVariable("id") Long idTool) {
        try {
            ToolEntity tool = toolService.findToolById(idTool);
            return ResponseEntity.ok(tool);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno: " + e.getMessage());
        }
    }
}