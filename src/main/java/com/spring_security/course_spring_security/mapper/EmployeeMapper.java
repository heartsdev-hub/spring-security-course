package com.spring_security.course_spring_security.mapper;

import com.spring_security.course_spring_security.dto.employee.request.EmployeeCreateRequest;
import com.spring_security.course_spring_security.dto.employee.response.EmployeeResponse;
import com.spring_security.course_spring_security.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {RolMapper.class})
public interface EmployeeMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles",ignore = true)
    @Mapping(target = "created_at",ignore = true)
    @Mapping(target = "updated_at", ignore = true)
    Employee employeeCreateToEmployee(EmployeeCreateRequest employeeCreateRequest);

    EmployeeResponse emmployeeToEmployeeResponse(Employee employee);
}
