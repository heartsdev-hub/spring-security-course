package com.spring_security.course_spring_security.dto.rol.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RolResponse {
    private UUID id;
    private String name;
    private LocalDate created_at;
    private LocalDate updated_at;
}
