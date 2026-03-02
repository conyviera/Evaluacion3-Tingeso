package com.example.ProyectoTingeso.Repositories;

import com.example.ProyectoTingeso.Entities.CustomerEntity;
import com.example.ProyectoTingeso.Entities.LoanEntity;
import com.example.ProyectoTingeso.Entities.TypeToolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<LoanEntity,Long> {

    @Override
    Optional<LoanEntity> findById(Long id);

    List<LoanEntity> findByCustomerAndState(CustomerEntity customer, String state);

    List<LoanEntity> findByCustomerAndStateIsNot(CustomerEntity customer, String state);

    List<LoanEntity> findByStateIsNot(String state);

    List<LoanEntity> findAll();

    List<LoanEntity> findByState(String state);

    @Query("SELECT tt.name, COUNT(tt) " +
            "FROM LoanEntity l " +
            "JOIN l.tool t " +
            "JOIN t.typeTool tt " +
            "GROUP BY tt.idTypeTool, tt.name " +
            "ORDER BY COUNT(tt) DESC")
    List<Object[]> countLoansByType();

    @Query("SELECT tt.name, COUNT(t.idTool) " +
            "FROM LoanEntity l JOIN l.tool t JOIN t.typeTool tt " +
            "WHERE l.deliveryDate BETWEEN :startDate AND :endDate " +
            "GROUP BY tt.name " +
            "ORDER BY COUNT(t.idTool) DESC")
    List<Object[]> countLoansByTypeInDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    int countLoansByState(String state);

    int countByDeliveryDateBetweenAndState(LocalDate startDate, LocalDate endDate, String state);

    List<LoanEntity> findByDeliveryDateBetween(LocalDate start, LocalDate end);



}
