package com.course.api.controller;

import com.course.api.dto.ApiResult;
import com.course.api.dto.employee.request.EmployeeCreateRequest;
import com.course.api.dto.employee.response.EmployeeResponse;
import com.course.api.service.EmployeeService;
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
