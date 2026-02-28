package com.example.ProyectoTingeso.Entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KardexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idKardex;

    private String typeMove; // PRESTAMO, DEVOLUCIONES, REPARACIONES, BAJAS, REGISTRO

    private LocalDateTime date;

    private int quantity;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id") 
    private LoanEntity loan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "user_id")
    private UserEntity user;

    @OneToMany(
            mappedBy = "kardex",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<KardexDetailEntity> kardexDetail= new ArrayList<>();

    public void addDetail(KardexDetailEntity detail) {
        kardexDetail.add(detail);
        detail.setKardex(this);

    }

}
