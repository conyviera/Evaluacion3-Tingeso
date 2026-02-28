package com.example.ProyectoTingeso.Repositories;


import com.example.ProyectoTingeso.Entities.CustomerEntity;
import com.example.ProyectoTingeso.Entities.DebtsEntity;
import com.example.ProyectoTingeso.Entities.LoanEntity;
import com.example.ProyectoTingeso.Entities.ToolEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface DebtsRepository extends JpaRepository<DebtsEntity,Long> {

    Optional<DebtsEntity> findByCustomerAndStatus(CustomerEntity customer, String status);

    Optional<DebtsEntity> findById(Long id);

    List<DebtsEntity> findByLoan_IdLoan(Long idLoan);

    Optional<DebtsEntity> findByToolAndStatusAndType(ToolEntity tool, String status, String type);

    boolean existsByCustomerAndStatus(CustomerEntity customer, String pendingAssessment);

    Optional<DebtsEntity> findByLoanAndStatus(LoanEntity loan, String status);
}


