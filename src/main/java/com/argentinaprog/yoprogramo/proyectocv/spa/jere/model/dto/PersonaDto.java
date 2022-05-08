package com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.dto;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@Getter
public class PersonaDto implements Serializable {
    private final String nombres;
    private final String apellidos;
    private final LocalDate fechaNacimiento;
    private final Nacionalidades nacionalidad;
    private final String email;
    private final String descripcion;
    private final String imagen;
    private final String ocupacion;
    private final Usuario usuario;
    private final List<Educacion> estudios;
    private final List<Habilidad> habilidades;
    private final List<TrabajoDto> experienciasLaborales;
    private final List<Proyecto> proyectos;
}
