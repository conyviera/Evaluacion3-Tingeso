package com.example.ProyectoTingeso.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KardexDetailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JoinColumn(name = "id_kardexDetail", nullable = false)
    private Long idKardexDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kardex", nullable = false)
    @JsonBackReference
    private KardexEntity kardex;

    @ManyToOne
    @JoinColumn(name = "id_tool")
    private ToolEntity tool;

    public void setKardex(KardexEntity kardex) {
        this.kardex = kardex;
    }

}
