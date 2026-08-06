package com.spring_security.course_spring_security.service;

import com.spring_security.course_spring_security.dto.ApiResult;
import com.spring_security.course_spring_security.dto.rol.request.RolCreateRequest;
import com.spring_security.course_spring_security.dto.rol.response.RolResponse;
import com.spring_security.course_spring_security.entity.Rol;
import com.spring_security.course_spring_security.exception.ConflictException;
import com.spring_security.course_spring_security.mapper.RolMapper;
import com.spring_security.course_spring_security.repository.RolRepository;
import org.springframework.stereotype.Service;

@Service
public class RolService {
    public final RolRepository rolRepository;
    public final RolMapper rolMapper;

    public RolService(RolRepository rolRepository, RolMapper rolMapper) {
        this.rolRepository = rolRepository;
        this.rolMapper = rolMapper;
    }
    public ApiResult<RolResponse> create (RolCreateRequest rolCreateRequest){
        String name = rolCreateRequest.getName().trim().toUpperCase();
        if(rolRepository.existesByName(name)){
            throw new ConflictException("The name already exists.");
        }
        rolCreateRequest.setName(name);
        Rol rol = rolMapper.rolToRolCreate(rolCreateRequest);
        Rol savedRol = rolRepository.save(rol);
        return new ApiResult<>(
                true,
                "The rol created successfully",
                rolMapper.rolToRolResponse(savedRol)
        );
    }
}
