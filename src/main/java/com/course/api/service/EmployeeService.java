package com.course.api.service;

import com.course.api.dto.ApiResult;
import com.course.api.dto.employee.request.EmployeeCreateRequest;
import com.course.api.dto.employee.request.EmployeeRol;
import com.course.api.dto.employee.response.EmployeeResponse;
import com.course.api.entity.Employee;
import com.course.api.entity.Rol;
import com.course.api.exception.BadRequestException;
import com.course.api.exception.ConflictException;
import com.course.api.exception.NotFoundException;
import com.course.api.mapper.EmployeeMapper;
import com.course.api.repository.EmployeeRepository;
import com.course.api.repository.RolRepository;
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
