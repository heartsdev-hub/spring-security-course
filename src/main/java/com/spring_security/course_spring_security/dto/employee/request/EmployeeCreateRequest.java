package com.spring_security.course_spring_security.dto.employee.request;

import com.spring_security.course_spring_security.dto.rol.response.RolResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EmployeeCreateRequest {
    @NotBlank(message = "The name is required")
    public String name;
    @NotBlank(message = "The name is required")
    @Email(message = "It must be in email format.")
    private String email;
    @NotBlank(message = "The password is required")
    private String password;
    public List<EmployeeRol> roles = new ArrayList<>();
}
