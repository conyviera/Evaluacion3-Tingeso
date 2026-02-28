package com.example.ProyectoTingeso.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

public class

TypeToolEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTypeTool;

    private String name;

    @Column(nullable = false)
    private String category;

    private int replacementValue;

    private int dailyRate;

    @Column(nullable = false)
    private int debtRate;

    private int stock;

}
