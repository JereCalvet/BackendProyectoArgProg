package com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class ProyectoNotFoundException extends RuntimeException {

    private static final String PROYECTO_ID_NOT_FOUND_ERROR_MSG = "Proyecto id %d no encontrado.";

    public ProyectoNotFoundException(Long idProyecto) {
        super(String.format(PROYECTO_ID_NOT_FOUND_ERROR_MSG, idProyecto));
    }
}
