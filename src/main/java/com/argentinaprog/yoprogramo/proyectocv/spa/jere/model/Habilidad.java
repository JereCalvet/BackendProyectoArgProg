package com.argentinaprog.yoprogramo.proyectocv.spa.jere.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Habilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String nombre;

    @Column(name = "porcentaje")
    private int nivel;

    @Column(name = "obs")
    private String descripcion;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

}
