package com.example.ProyectoTingeso.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

public class ToolEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long idTool;

    @ManyToOne
    @JoinColumn(name = "type_tool_id_type_tool")
    private TypeToolEntity typeTool;

    /**
     * State:
     * AVAILABLE (Disponible)
     * ON_LOAN (Prestada)
     * UNDER_REPAIR (En reparación)
     * DECOMMISSIONED (D ada de baja)
     */
    private String state;
}
