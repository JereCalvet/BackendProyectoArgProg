package com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;

@AllArgsConstructor
@Getter
public class ProyectoDto implements Serializable {

    @NotEmpty(message = "El nombre del proyecto no puede estar vacío")
    @Size(max = 40, message = "El nombre del proyecto no puede tener más de 40 caracteres")
    private final String nombre;
    private final String descripcion;
}
