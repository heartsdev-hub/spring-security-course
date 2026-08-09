package com.spring_security.course_spring_security.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuthResponse {
    private String token;
    private String tokenType;
    private long expiresIn;
}
