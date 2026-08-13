package com.course.api.dto.rol.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RolCreateRequest {
    @NotBlank(message = "El nombre se requiere")
    public String name;
}
