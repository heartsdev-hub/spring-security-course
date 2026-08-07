package com.spring_security.course_spring_security.dto.employee.response;

import com.spring_security.course_spring_security.dto.rol.response.RolResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EmployeeResponse {
    private UUID id;
    private String name;
    private String email;
    private String password;
    private List<RolResponse> roles = new ArrayList<>();
    private LocalDate created_at;
    private LocalDate updated_at;
}
