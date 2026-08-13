package com.course.api.dto.employee.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EmployeeRol {
    @NotBlank(message = "The name is required")
    private String name;
    @NotBlank(message = "The rol-id is required")
    private String id;
}
