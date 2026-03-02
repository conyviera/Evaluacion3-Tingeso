package com.example.ProyectoTingeso.Repositories;


import com.example.ProyectoTingeso.Entities.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity,Long> {

    Optional<CustomerEntity> findByRut(String rut);

    @Override
    List<CustomerEntity> findAll();

    @Override
    Optional<CustomerEntity> findById(Long id);

    Optional<CustomerEntity> findByName(String name);

    boolean existsByRut(String rut);

    int countAllByState(String state);

    long count();

}
