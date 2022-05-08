package com.argentinaprog.yoprogramo.proyectocv.spa.jere.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
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

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

}
