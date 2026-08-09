package com.spring_security.course_spring_security.service;

import com.spring_security.course_spring_security.dto.ApiResult;
import com.spring_security.course_spring_security.dto.employee.request.EmployeeCreateRequest;
import com.spring_security.course_spring_security.dto.employee.request.EmployeeRol;
import com.spring_security.course_spring_security.dto.employee.response.EmployeeResponse;
import com.spring_security.course_spring_security.entity.Employee;
import com.spring_security.course_spring_security.entity.Rol;
import com.spring_security.course_spring_security.exception.BadRequestException;
import com.spring_security.course_spring_security.exception.ConflictException;
import com.spring_security.course_spring_security.exception.NotFoundException;
import com.spring_security.course_spring_security.mapper.EmployeeMapper;
import com.spring_security.course_spring_security.repository.EmployeeRepository;
import com.spring_security.course_spring_security.repository.RolRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper, RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public ApiResult<List<EmployeeResponse>> allEmployee(){
        List<Employee> employees = employeeRepository.findAll();
        List<EmployeeResponse> employeeDTO= employees.stream().map(
                employeeMapper::emmployeeToEmployeeResponse
        ).toList();
        return new ApiResult<>(
                true,
                "List employees",
                employeeDTO
        );
    }
    public ApiResult<EmployeeResponse>  create (EmployeeCreateRequest employeeCreateRequest){
        Employee employee = employeeMapper.employeeCreateToEmployee(employeeCreateRequest);
        if(employeeRepository.existsByEmail(employeeCreateRequest.getEmail())){
            throw new ConflictException("The name already exists");
        }
        for(EmployeeRol employeeRol: employeeCreateRequest.getRoles()){
            UUID uuid;
            try{
                uuid = UUID.fromString(employeeRol.getId());
            }catch (IllegalArgumentException ex){
                throw new BadRequestException("UUID invalid");
            }
            Rol rol = rolRepository.findById(uuid).orElseThrow(
                    () -> new  NotFoundException("Not Found Rol ")
            );
            if(!employeeRol.getName().trim().toUpperCase().equals(rol.getName())){
                throw new BadRequestException("The rol name is invalid.");
            }
            employee.getRoles().add(rol);
        }
        employee.setPassword(passwordEncoder.encode(employeeCreateRequest.getPassword()));
        Employee savedEmployee = employeeRepository.save(employee);
        return new ApiResult<>(
                true,"Employee created successfully.",
                employeeMapper.emmployeeToEmployeeResponse(savedEmployee)
        );
    }
}
