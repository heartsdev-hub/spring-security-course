package com.spring_security.course_spring_security.service;

import com.spring_security.course_spring_security.dto.auth.AuthRequest;
import com.spring_security.course_spring_security.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }
    public AuthResponse login (AuthRequest authRequest){
        Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
                  authRequest.getEmail(),
                  authRequest.getPassword()
          )
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token, "Bearer", jwtService.getJwtExpiration());
    }
}
