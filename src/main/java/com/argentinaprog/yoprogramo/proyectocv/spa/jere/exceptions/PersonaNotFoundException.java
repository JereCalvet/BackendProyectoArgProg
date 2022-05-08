package com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class PersonaNotFoundException extends RuntimeException {

    private static final String PERSONA_ID_NOT_FOUND_ERROR_MSG = "Persona id %d no encontrada.";

    public PersonaNotFoundException(Long personaId) {
        super(String.format(PERSONA_ID_NOT_FOUND_ERROR_MSG, personaId));
    }
}
