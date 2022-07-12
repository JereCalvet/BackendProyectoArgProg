package com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class EmailAlreadyTakenException extends RuntimeException {

    private static final String EMAIL_ALREADY_TAKEN_ERROR_MSG = "Ya existe un usuario con este email %s.";

    public EmailAlreadyTakenException(String email) {
        super(String.format(EMAIL_ALREADY_TAKEN_ERROR_MSG, email));
    }
}
