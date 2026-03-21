package com.StartUp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class AppExceptions {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class ResourceNotFoundException extends  RuntimeException{
        public ResourceNotFoundException (String message){
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class EmailAlreadyExistsException extends RuntimeException{
        public EmailAlreadyExistsException(String email){
            super("Email вече е регистриран: " + email);
        }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class AccountBlockedException extends RuntimeException{
        public AccountBlockedException(){
            super("Акаунта е блокиран");
        }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class InvalidTokenException extends RuntimeException{
        public InvalidTokenException(String message){
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class BadRequestException extends  RuntimeException{
        public BadRequestException(String message){
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class UnauthorizedException extends RuntimeException{
        public UnauthorizedException(String message){
            super(message);
        }
    }
}
