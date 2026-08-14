package com.course.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserDetailsService userDetailsService;
    private final JwtAuthFIlter authFIlter;

    public SecurityConfig(UserDetailsService userDetailsService, JwtAuthFIlter authFIlter) {
        this.userDetailsService = userDetailsService;
        this.authFIlter = authFIlter;
    }

    @Bean
    public SecurityFilterChain filterCHain(HttpSecurity htpp) throws Exception {
        htpp
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(
                    auth -> auth
                            .requestMatchers("/api/v1/auth").permitAll()
                            .anyRequest().authenticated()
                )
                .addFilterBefore(authFIlter, UsernamePasswordAuthenticationFilter.class);
                return htpp.build();
    }
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(
                userDetailsService
        );
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
