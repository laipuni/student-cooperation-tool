package com.stool.studentcooperationtools.domain.api.controller;


import com.stool.studentcooperationtools.domain.api.ApiExceptionResponse;
import com.stool.studentcooperationtools.exception.global.DuplicateDataException;
import com.stool.studentcooperationtools.exception.global.UnAuthorizationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionController {

    //요청값을 바인딩할 때 발생하는 예외를 처리한다.
    //@Valid가 붙은 파라미터를 바인딩할 때 발생하는 예외이다.(ex @NotBlank로 설정한 값이 빈칸,null일 경우)
    @ExceptionHandler(value = BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse<Object> bindException(BindException exception){
        log.trace("[ {} ] message = {}", exception.getClass().getSimpleName(), exception.getMessage());
        return ApiExceptionResponse.of(
            HttpStatus.BAD_REQUEST,
                exception.getBindingResult()
                        .getAllErrors().get(0)
                        .getDefaultMessage(),
                null
        );
    }

    //클라이언트의 요청 값이 유효하지 않을 경우
    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse<Object> IllegalArgumentException(IllegalArgumentException exception){
        log.trace("[ {} ] message = {}", exception.getClass().getSimpleName(), exception.getMessage());
        return ApiExceptionResponse.of(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                null
        );
    }

    //권한을 가지고 있지 않은 사용자가 권한이 필요한 요청을 했을 경우
    @ExceptionHandler(value = UnAuthorizationException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public ApiExceptionResponse<Object> AccessDeniedException(UnAuthorizationException exception){
        log.trace("[ {} ] message = {}", exception.getClass().getSimpleName(), exception.getMessage());
        return ApiExceptionResponse.of(
                HttpStatus.FORBIDDEN,
                exception.getMessage(),
                null
        );
    }


    //DB에 유니크로 설정한 값이 중복으로 존재할 경우
    @ExceptionHandler(DuplicateDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiExceptionResponse<Object> DataIntegrityViolationException(DuplicateDataException exception){
        log.trace("[ {} ] message = {}", exception.getClass().getSimpleName(), exception.getMessage());
        return ApiExceptionResponse.of(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                null
        );
    };
}
