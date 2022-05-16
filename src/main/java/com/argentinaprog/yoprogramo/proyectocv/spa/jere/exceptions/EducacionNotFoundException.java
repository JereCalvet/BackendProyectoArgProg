package com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class EducacionNotFoundException extends RuntimeException {

    private static final String EDUCACION_ID_NOT_FOUND_ERROR_MSG = "Estudio id %d no encontrado.";

    public EducacionNotFoundException(Long educacionId) {
        super(String.format(EDUCACION_ID_NOT_FOUND_ERROR_MSG, educacionId));
    }
}
