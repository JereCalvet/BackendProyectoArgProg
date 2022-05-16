package com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class LoginRequestDto implements Serializable {

    @NotEmpty(message = "El usuario no puede estar vacio.")
    private String username;

    @NotEmpty(message = "La contraseña no puede estar vacia.")
    private String password;
}
