package com.spring_security.course_spring_security.mapper;

import com.spring_security.course_spring_security.dto.rol.request.RolCreateRequest;
import com.spring_security.course_spring_security.dto.rol.response.RolResponse;
import com.spring_security.course_spring_security.entity.Rol;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RolMapper {
    Rol rolCreateToRol(RolCreateRequest rolCreateRequest);
    RolResponse rolToRolResponse(Rol rol);
}
