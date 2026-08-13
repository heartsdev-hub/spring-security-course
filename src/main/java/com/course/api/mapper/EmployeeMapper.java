package com.course.api.mapper;

import com.course.api.dto.employee.request.EmployeeCreateRequest;
import com.course.api.dto.employee.response.EmployeeResponse;
import com.course.api.entity.Employee;
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
