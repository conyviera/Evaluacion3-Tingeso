package com.example.ProyectoTingeso.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class DebtsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idDebts;

    @Column(nullable = false)
    private double amount;

    /**
     * Type:
     * ARREARS (atraso)
     * DAMAGES (daño)
     */
    @Column(nullable = false)
    private String type;

    /**
     * Status:
     * PAID (pagada)
     * PENDING (pendiente)
     */
    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDate creationDate;

    @Column(nullable = true)
    private LocalDate paymentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id")
    private LoanEntity loan;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "debts_tool", joinColumns = @JoinColumn(name = "debts_id"), inverseJoinColumns = @JoinColumn(name = "tool_id"))
    private List<ToolEntity> tool;

}
