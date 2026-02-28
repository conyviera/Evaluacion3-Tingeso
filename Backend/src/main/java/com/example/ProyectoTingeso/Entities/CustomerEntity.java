package com.example.ProyectoTingeso.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import java.util.List;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCustomer;

    @Column(name = "name", nullable = false)
    private String name;

    private String phoneNumber;

    @Column(name = "rut", nullable = false)
    private String rut;

    private String email;

    /**
     * State:
     * ACTIVE
     * RESTRICTED
     */
    @Column(name = "state", nullable = false)
    private String state;


}


