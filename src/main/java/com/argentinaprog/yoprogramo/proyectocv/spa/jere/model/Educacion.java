package com.argentinaprog.yoprogramo.proyectocv.spa.jere.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Educacion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;
    private String institucion;
    private String titulo;
    private String lugar;

    @Enumerated(EnumType.STRING)
    private ProgresoEducacion estado;

    @JsonIgnore
    @JoinColumn(name = "persona_id", nullable = false)
    @ManyToOne(optional = false)
    private Persona persona;

}
