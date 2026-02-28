package com.example.ProyectoTingeso.Repositories;

import com.example.ProyectoTingeso.Entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByKeycloakId(String keycloakId);
}