package com.course.api.mapper;

import com.course.api.dto.rol.request.RolCreateRequest;
import com.course.api.dto.rol.response.RolResponse;
import com.course.api.entity.Rol;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RolMapper {
    Rol rolCreateToRol(RolCreateRequest rolCreateRequest);
    RolResponse rolToRolResponse(Rol rol);
}
