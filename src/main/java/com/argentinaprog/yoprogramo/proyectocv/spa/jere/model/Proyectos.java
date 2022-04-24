package com.argentinaprog.yoprogramo.proyectocv.spa.jere.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Builder
@Entity
@NoArgsConstructor
public class Proyectos {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String nombre;

    @Column(name = "obs")
    private String descripcion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

}
