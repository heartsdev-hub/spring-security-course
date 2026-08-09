package com.spring_security.course_spring_security.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuthRequest {
    @NotBlank(message = "The email is incorrect")
    @Email(message = "The email format is invalid")
    private String email;
    @NotBlank(message = "The password is incorrect")
    private String password;
}
