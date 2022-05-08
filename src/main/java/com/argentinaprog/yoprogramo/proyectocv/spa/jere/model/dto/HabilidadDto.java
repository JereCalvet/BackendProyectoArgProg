package com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;

@AllArgsConstructor
@Getter
public class HabilidadDto implements Serializable {

    @NotEmpty(message = "El nombre de la habilidad no puede estar vacío")
    @Size(max = 40, message = "El nombre de la habilidad no puede tener más de 40 caracteres")
    private final String nombre;
    private final int nivel;
    private final String descripcion;
}
