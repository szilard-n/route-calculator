package com.example.routecalculator.handler;

import com.example.routecalculator.dto.ApiErrorResponse;
import com.example.routecalculator.exception.DataAccessException;
import com.example.routecalculator.exception.RouteCalculationException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    private static final String CONTACT_SUPPORT_MSG = "Something went wrong. Please contact support.";

    @ExceptionHandler(value = {ConstraintViolationException.class, RouteCalculationException.class})
    public ResponseEntity<ApiErrorResponse> validationException(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return buildResponse(request, status, ex.getMessage());
    }

    @ExceptionHandler(value = {Exception.class, DataAccessException.class})
    public ResponseEntity<ApiErrorResponse> internalErrorHandler(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return buildResponse(request, status, CONTACT_SUPPORT_MSG);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(WebRequest request, HttpStatus status, String message) {
        String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
        HttpMethod httpMethod = ((ServletWebRequest) request).getHttpMethod();
        ApiErrorResponse apiErrorDto = new ApiErrorResponse(status, message, httpMethod.name(), uri);

        log.error("{} {} -> {}", httpMethod.name(), uri, message);
        return ResponseEntity.status(status).body(apiErrorDto);
    }
}
