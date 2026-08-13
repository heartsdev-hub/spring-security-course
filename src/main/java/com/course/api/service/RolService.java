package com.course.api.service;

import com.course.api.dto.ApiResult;
import com.course.api.dto.rol.request.RolCreateRequest;
import com.course.api.dto.rol.response.RolResponse;
import com.course.api.entity.Rol;
import com.course.api.exception.ConflictException;
import com.course.api.mapper.RolMapper;
import com.course.api.repository.RolRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolService {
    public final RolRepository rolRepository;
    public final RolMapper rolMapper;

    public RolService(RolRepository rolRepository, RolMapper rolMapper) {
        this.rolRepository = rolRepository;
        this.rolMapper = rolMapper;
    }
    public ApiResult<List<RolResponse>>allRol(){
        List<Rol> roles = rolRepository.findAll();
        List<RolResponse> rolResponse = roles.stream().map(
                rolMapper::rolToRolResponse
        ).toList();
        return new ApiResult<>(true,"List Role",
                rolResponse
                );
    }
    public ApiResult<RolResponse> create (RolCreateRequest rolCreateRequest){
        String name = rolCreateRequest.getName().trim().toUpperCase();
        if(rolRepository.existsByName(name)){
            throw new ConflictException("The name already exists.");
        }
        rolCreateRequest.setName(name);
        Rol rol = rolMapper.rolCreateToRol(rolCreateRequest);
        Rol savedRol = rolRepository.save(rol);
        return new ApiResult<>(
                true,
                "The rol created successfully",
                rolMapper.rolToRolResponse(savedRol)
        );
    }
}
