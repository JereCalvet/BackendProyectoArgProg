package com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class TrabajoNotFoundException extends RuntimeException {

    private static final String TRABAJO_ID_NOT_FOUND_ERROR_MSG = "Trabajo id %d no encontrado.";

    public TrabajoNotFoundException(Long trabajoId) {
        super(String.format(TRABAJO_ID_NOT_FOUND_ERROR_MSG, trabajoId));
    }
}
