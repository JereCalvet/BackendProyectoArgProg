package com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.ACCEPTED)
public class UsuarioNotFoundException extends RuntimeException {

    private static final String USUARIO_NOT_FOUND_ERROR_MSG = "Usuario no encontrado.";

    public UsuarioNotFoundException() {
        super(USUARIO_NOT_FOUND_ERROR_MSG);
    }
}
