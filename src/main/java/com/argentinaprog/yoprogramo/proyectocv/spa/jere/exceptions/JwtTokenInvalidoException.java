package com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
public class JwtTokenInvalidoException extends RuntimeException {

    private static final String TOKEN_INVALIDO_ERROR_MSG = "Token %s invalido. Motivo: %s";

    public JwtTokenInvalidoException(String token, String mensaje) {
        super(String.format(TOKEN_INVALIDO_ERROR_MSG, token, mensaje));
    }
}
