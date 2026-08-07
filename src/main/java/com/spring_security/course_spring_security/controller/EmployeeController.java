package com.spring_security.course_spring_security.controller;

import com.spring_security.course_spring_security.dto.ApiResult;
import com.spring_security.course_spring_security.dto.employee.request.EmployeeCreateRequest;
import com.spring_security.course_spring_security.dto.employee.response.EmployeeResponse;
import com.spring_security.course_spring_security.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/employee")
@RestController
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    @PostMapping("/create")
    public ResponseEntity<ApiResult<EmployeeResponse>>create(@RequestBody @Valid EmployeeCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }
    @GetMapping("")
    public ResponseEntity<ApiResult<List<EmployeeResponse>>>allEmployee(){
        return ResponseEntity.ok(employeeService.allEmployee());
    }
}
