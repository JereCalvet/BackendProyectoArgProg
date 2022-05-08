package com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TrabajoDto implements Serializable {

    @NotEmpty(message = "El nombre de la empresa no puede estar vacío")
    @Size(max = 40, message = "El nombre de la empresa no puede tener más de 40 caracteres")
    private final String empresa;

    @NotEmpty(message = "El cargo/puesto no puede estar vacío")
    private final String cargo;
    private final String lugar;
    private final LocalDate desde;
    private final LocalDate hasta;
}
