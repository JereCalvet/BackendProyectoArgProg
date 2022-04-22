package com.argentinaprog.yoprogramo.proyectocv.spa.jere.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@Entity
public class Educacion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private ProgresoEducacion estado;
    private String institucion;
    private String titulo;
    private String lugar;

    @ManyToOne(optional = false)
    @JoinColumn(name = "personaj_id", nullable = false)
    private Persona persona;

}