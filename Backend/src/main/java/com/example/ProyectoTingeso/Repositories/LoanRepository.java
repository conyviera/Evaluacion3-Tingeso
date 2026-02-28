package com.example.ProyectoTingeso.Repositories;

import com.example.ProyectoTingeso.Entities.CustomerEntity;
import com.example.ProyectoTingeso.Entities.LoanEntity;
import com.example.ProyectoTingeso.Entities.TypeToolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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

    int countLoansByState(String state);



}
