package com.example.ProyectoTingeso.Repositories;

import com.example.ProyectoTingeso.Entities.ToolEntity;
import com.example.ProyectoTingeso.Entities.TypeToolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToolRepository extends JpaRepository<ToolEntity,Long> {

    @Override
    Optional<ToolEntity> findById(Long id);

    Optional<ToolEntity> findFirstByStateAndTypeTool(String state, TypeToolEntity typetool);

    List<ToolEntity> findAll();

    List<ToolEntity> findAllByTypeTool(TypeToolEntity typetool);
}
