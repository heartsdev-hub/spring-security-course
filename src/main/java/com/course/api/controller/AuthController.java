package com.course.api.controller;

import com.course.api.dto.auth.AuthRequest;
import com.course.api.dto.auth.AuthResponse;
import com.course.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService auhService;

    public AuthController(AuthService auhService) {
        this.auhService = auhService;
    }
    @PostMapping("")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest request){
        return ResponseEntity.ok(auhService.login(request));
    }
}
