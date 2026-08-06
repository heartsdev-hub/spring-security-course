package com.spring_security.course_spring_security.exception;

import com.spring_security.course_spring_security.dto.ApiResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Log log = LogFactory.getLog(GlobalExceptionHandler.class);

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
}
