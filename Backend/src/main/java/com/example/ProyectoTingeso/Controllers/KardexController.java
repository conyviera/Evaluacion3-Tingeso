package com.example.ProyectoTingeso.Controllers;

import com.example.ProyectoTingeso.Entities.KardexEntity;
import com.example.ProyectoTingeso.Entities.LoanEntity;
import com.example.ProyectoTingeso.Repositories.KardexRepository;
import com.example.ProyectoTingeso.Services.KardexService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/kardex")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:8008",
        "http://localhost:8070",
        "http://localhost:3000"
})
public class KardexController {

    private final KardexService kardexService;

    public KardexController(KardexService kardexService) {
        this.kardexService = kardexService;
    }

    @GetMapping("/getAllMove")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<KardexEntity>> getAllmove(){

        List<KardexEntity> kardexList= kardexService.findAllKardex();

        return ResponseEntity.ok(kardexList);
    }

    @GetMapping("/getAllMovementsOfTool/{idTool}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<KardexEntity>> getAllMovementsOfTool(@PathVariable Long idTool){
        List<KardexEntity> kardexList= kardexService.getAllMovementsOfATool(idTool);

        return ResponseEntity.ok(kardexList);
    }

    @GetMapping("/getAllKardexByDate")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<KardexEntity>> getAllKardexByDate(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<KardexEntity> kardexList = kardexService.listByDateRange(startDateTime, endDateTime);

        return ResponseEntity.ok(kardexList);
    }

}
