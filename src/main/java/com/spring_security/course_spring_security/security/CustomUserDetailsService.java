package com.spring_security.course_spring_security.security;

import com.spring_security.course_spring_security.entity.Employee;
import com.spring_security.course_spring_security.repository.EmployeeRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final EmployeeRepository employeeRepository;

    public CustomUserDetailsService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("Employee not found.")
        );
        List<GrantedAuthority> authorities = employee.getRoles().stream()
                .map(rol -> rol.getName().trim().toUpperCase(Locale.ROOT))
                .map(roleName -> roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .map(authority -> (GrantedAuthority) authority)
                .toList();
        return User.builder()
                .username(employee.getEmail())
                .password(employee.getPassword())
                .authorities(authorities)
                .build();
    }
}
