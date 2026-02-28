package com.example.ProyectoTingeso.Repositories;

import com.example.ProyectoTingeso.Entities.CustomerEntity;
import com.example.ProyectoTingeso.Entities.KardexDetailEntity;
import com.example.ProyectoTingeso.Entities.KardexEntity;
import com.example.ProyectoTingeso.Entities.ToolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KardexDetailRepository extends JpaRepository<KardexDetailEntity, Long> {

    List<KardexDetailEntity> findAllByTool(ToolEntity tool);


}
