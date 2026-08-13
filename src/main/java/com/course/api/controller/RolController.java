package com.course.api.controller;

import com.course.api.dto.ApiResult;
import com.course.api.dto.rol.request.RolCreateRequest;
import com.course.api.dto.rol.response.RolResponse;
import com.course.api.service.RolService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("api/v1/rol")
@RestController
public class RolController {
    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }
    @GetMapping("")
    public ResponseEntity<ApiResult<List<RolResponse>>>allRol(){
        return ResponseEntity.ok(rolService.allRol());
    }
    @PostMapping("/create")
    public ResponseEntity<ApiResult<RolResponse>> create(@RequestBody @Valid RolCreateRequest rolCreateRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(rolService.create(rolCreateRequest));
    }
}
