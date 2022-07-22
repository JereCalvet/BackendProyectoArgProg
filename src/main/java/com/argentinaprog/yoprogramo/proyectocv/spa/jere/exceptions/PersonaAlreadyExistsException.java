package com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class PersonaAlreadyExistsException extends RuntimeException {

    private static final String PERSONA_ALREADY_EXISTS_ERROR_MSG = "El usuario %s ya tiene una persona creada.";

    public PersonaAlreadyExistsException(String nombreUsuario) {
        super(String.format(PERSONA_ALREADY_EXISTS_ERROR_MSG, nombreUsuario));
    }
}
