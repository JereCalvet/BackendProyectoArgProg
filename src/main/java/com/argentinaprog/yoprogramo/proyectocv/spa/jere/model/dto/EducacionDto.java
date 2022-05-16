package com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.dto;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.ProgresoEducacion;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;

@AllArgsConstructor
@Getter
public class EducacionDto implements Serializable {

    @NotEmpty(message = "El nombre de la institución no puede estar vacío")
    @Size(max = 40, message = "El nombre de la institución no puede tener más de 40 caracteres")
    private final String institucion;

    @NotEmpty(message = "El nombre del titulo no puede estar vacío")
    @Size(max = 40, message = "El nombre del titulo no puede tener más de 40 caracteres")
    private final String titulo;
    private final String lugar;

    private final ProgresoEducacion estado;
}
