package com.spring_security.course_spring_security.controller;

import com.spring_security.course_spring_security.dto.auth.AuthRequest;
import com.spring_security.course_spring_security.service.AuthResponse;
import com.spring_security.course_spring_security.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/auth")
@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest authRequest){
        return ResponseEntity.ok(authService.login(authRequest));
    }
}
