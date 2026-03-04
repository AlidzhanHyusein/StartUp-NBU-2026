package com.StartUp.exception;

import org.springframework.beans.factory.parsing.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.lang.module.ResolutionException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex){
        Map<String,String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            errors.put(field,error.getDefaultMessage());
        });

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Грешка при валидация");
        pd.setProperty("errors",errors);
        return pd;
    }

    @ExceptionHandler(AppExceptions.ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(AppExceptions.ResourceNotFoundException ex){
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Не е намерен");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(AppExceptions.EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailExist(AppExceptions.EmailAlreadyExistsException ex){
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Конфликт");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex){
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle("Грешни данни");
        pd.setDetail("Невалиден email или парола.");
        return pd;
    }

    @ExceptionHandler(AppExceptions.InvalidTokenException.class)
    public ProblemDetail handleInvalidToken(AppExceptions.InvalidTokenException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle("Невалиден токен");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler({AppExceptions.AccountBlockedException.class, LockedException.class})
    public ProblemDetail handleBlocked(RuntimeException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Достъпът е забранен");
        pd.setDetail("Акаунтът е блокиран.");
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Нямате права");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(AppExceptions.BadRequestException.class)
    public ProblemDetail handleBadRequest(AppExceptions.BadRequestException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Лоша заявка");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatus(ResponseStatusException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(ex.getStatusCode());
        pd.setTitle("Грешка");
        pd.setDetail(ex.getReason());
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Грешка в сървъра");
        pd.setDetail("Нещо се обърка. Моля опитайте по-късно.");
        return pd;
    }
}
