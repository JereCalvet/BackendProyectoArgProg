package com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class LoginRequestDtoParseException extends RuntimeException {

    private static final String JSON_TO_LOGIN_DTO_PARSE_ERROR_MSG = "Error al mapear el JSON de la request a LoginRequestDto. Error: %s";

    public LoginRequestDtoParseException(String mensaje) {
        super(String.format(JSON_TO_LOGIN_DTO_PARSE_ERROR_MSG, mensaje));
    }
}
