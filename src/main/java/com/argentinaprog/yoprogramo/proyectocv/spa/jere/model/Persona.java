package com.argentinaprog.yoprogramo.proyectocv.spa.jere.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String nombres;
    private String apellidos;
    private String email;
    private String descripcion;
    private String imagen;
    private LocalDate fechaNacimiento;
    private String ocupacion;

    @OneToOne(cascade = CascadeType.ALL)
    private Usuario usuario;

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Educacion> estudios = new ArrayList<>();

}
