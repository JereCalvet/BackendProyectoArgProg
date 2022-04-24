package com.argentinaprog.yoprogramo.proyectocv.spa.jere.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String nombreUsuario;
    private String password;

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "persona_id")
    private Persona persona;

}
