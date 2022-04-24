package com.argentinaprog.yoprogramo.proyectocv.spa.jere.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
public class Trabajo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String empresa;
    private String cargo;
    private String lugar;

    @Column(name = "fecha_inicio")
    private LocalDate desde;
    @Column(name = "fecha_fin")
    private LocalDate hasta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

}
