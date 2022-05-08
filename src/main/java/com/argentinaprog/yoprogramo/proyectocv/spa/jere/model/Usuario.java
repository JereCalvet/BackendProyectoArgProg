package com.argentinaprog.yoprogramo.proyectocv.spa.jere.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
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
