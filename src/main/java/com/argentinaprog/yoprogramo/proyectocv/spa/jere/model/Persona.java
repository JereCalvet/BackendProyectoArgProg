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
@AllArgsConstructor
@NoArgsConstructor
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    private Nacionalidades nacionalidad;
    private String email;
    private String descripcion;
    private String imagen;
    private String ocupacion;

    @OneToOne(mappedBy = "persona", optional = true, orphanRemoval = true)
    private Usuario usuario;

    @Singular
    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Educacion> estudios = new ArrayList<>();

    @Singular
    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Habilidad> habilidades = new ArrayList<>();

    @Singular
    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    //@OrderBy("trabajo.fecha_inicio DESC")
    private List<Trabajo> experienciasLaborales = new ArrayList<>();

    @Singular
    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Proyecto> proyectos = new ArrayList<>();
}
