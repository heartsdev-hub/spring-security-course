package com.spring_security.course_spring_security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    public String name;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "employee_id",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    public List<Rol> roles = new ArrayList<>();
    @Column(updatable = false)
    public LocalDate created_at;
    public LocalDate updated_at;
    @PrePersist
    public void onCreate(){
        this.created_at = LocalDate.now();
        this.updated_at = LocalDate.now();
    }
    @PreUpdate
    public void onUpdate(){
        this.updated_at = LocalDate.now();
    }
}
