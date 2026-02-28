package com.example.ProyectoTingeso.Repositories;

import com.example.ProyectoTingeso.Entities.TypeToolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TypeToolRepository extends JpaRepository<TypeToolEntity,Long>{

    Optional<TypeToolEntity> findById(Long id);

    Optional<TypeToolEntity> findByNameAndCategoryAndReplacementValueAndDailyRate(String name, String category, int replacementValue, int dailyRate);

    List<TypeToolEntity> findAll();

    @Query("select distinct t.category from TypeToolEntity t")
    List<String> findDistinctCategory();


}
