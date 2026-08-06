package com.spring_security.course_spring_security.repository;

import com.spring_security.course_spring_security.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RolRepository extends JpaRepository<Rol, UUID> {
    boolean existesByName (String name);
}
