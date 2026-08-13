package com.course.api.repository;

import com.course.api.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RolRepository extends JpaRepository<Rol, UUID> {
    boolean existsByName (String name);
}
