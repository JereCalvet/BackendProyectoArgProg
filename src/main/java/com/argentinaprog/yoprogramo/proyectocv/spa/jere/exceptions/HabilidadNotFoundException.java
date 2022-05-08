package com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class HabilidadNotFoundException extends RuntimeException {

    private static final String HABILIDAD_ID_NOT_FOUND_ERROR_MSG = "Habilidad id %d no encontrado.";

    public HabilidadNotFoundException(Long idHabilidad) {
        super(String.format(HABILIDAD_ID_NOT_FOUND_ERROR_MSG, idHabilidad));
    }
}
