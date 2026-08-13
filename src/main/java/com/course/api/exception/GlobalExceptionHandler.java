package com.course.api.exception;

import com.course.api.dto.ApiResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Log log = LogFactory.getLog(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Map<String, String>>> handleMethodArgumentNotValidExcepiton(MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(
                error -> errors.put(error.getField(),error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ApiResult<>(
                        false, "Validation error",errors
                )
        );
    }
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResult<Void>>handleConflictException(ConflictException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ApiResult<>(
                        false, ex.getMessage(),
                        null
                )
        );
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResult<Void>>handleNotFoundException(NotFoundException exception){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiResult<>(
                        false,
                        exception.getMessage(),
                        null
                )
        );
    }
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResult<Void>>handleDataAccessException(DataAccessException exception){
        log.error("Database access error", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResult<>(false,"ocurrio un error interno",null)
        );
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResult<Void>> handleBadRequestExcepiton(BadRequestException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ApiResult<>(
                        false, exception.getMessage(),null
                )
        );
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResult<Void>> handleBadCredentialsException(BadCredentialsException exception){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ApiResult<>(
                        false, "Email or password is incorrect", null
                )
        );
    }
}
