package com.argentinaprog.yoprogramo.proyectocv.spa.jere.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @OneToOne(mappedBy = "persona", optional = false, orphanRemoval = true)
    private Usuario usuario;

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Educacion> estudios = new ArrayList<>();

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Habilidad> habilidades = new ArrayList<>();

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("trabajo.fecha_inicio DESC")
    private List<Trabajo> experienciaLaboral = new ArrayList<>();

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Proyecto> proyectos = new ArrayList<>();

}
