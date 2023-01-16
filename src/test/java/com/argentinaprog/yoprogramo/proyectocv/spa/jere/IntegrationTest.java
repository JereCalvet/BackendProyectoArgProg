package com.argentinaprog.yoprogramo.proyectocv.spa.jere;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.*;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.repositories.*;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class IntegrationTest extends AbstractContainerBaseTest {

    private Faker faker = new Faker();

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private EducacionRepository educacionRepository;

    @Autowired
    private HabilidadRepository habilidadRepository;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private TrabajoRepository trabajoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @LocalServerPort
    int randomServerPort;

    private static final String API_URL = "/api/v1";

    @AfterEach
    void tearDown() {
        log.warn("Starting data deletion...");
        personaRepository.deleteAll();
        usuarioRepository.deleteAll();
        log.warn("Finished data deletion...");
    }

    @BeforeEach
    void setUp() {
        setUpFakeData();
    }

    private void setUpFakeData() {
        log.warn("Starting data initialization...");
        for (int i = 0; i < 10; i++) {
            LocalDate fechaNacimiento = faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            LocalDate trabajoDesde = addRandomAmountOfYearsBetween(fechaNacimiento, 12, 20); //facha nacimiento + random(min max  -> años estudiando)
            LocalDate trabajoHasta = addRandomAmountOfYearsBetween(trabajoDesde, 1, 20); //trabajo desde + random(min max  -> años trabajando)
            Trabajo trabajo = Trabajo.builder()
                    .empresa(faker.company().name())
                    .cargo(faker.job().position())
                    .desde(trabajoDesde)
                    .hasta(trabajoHasta)
                    .lugar(faker.address().city())
                    .build();
            Educacion estudio = Educacion.builder()
                    .estado(ProgresoEducacion.values()[faker.number().numberBetween(0, ProgresoEducacion.values().length)])
                    .institucion(faker.educator().university())
                    .titulo(faker.educator().course())
                    .lugar(faker.address().city())
                    .build();
            Proyecto proyecto = Proyecto.builder()
                    .nombre(faker.funnyName().name())
                    .descripcion(String.join(" ", faker.lorem().words(10)))
                    .build();
            Habilidad habilidad = Habilidad.builder()
                    .nombre(faker.job().keySkills())
                    .nivel(faker.number().numberBetween(1, 100))
                    .descripcion(String.join(" ", faker.lorem().words(10)))
                    .build();
            var persona = Persona.builder()
                    .nombres(faker.name().firstName())
                    .apellidos(faker.name().lastName())
                    .fechaNacimiento(fechaNacimiento)
                    .nacionalidad(Nacionalidades.values()[faker.number().numberBetween(0, Nacionalidades.values().length)])
                    .email(faker.internet().emailAddress())
                    .ocupacion(faker.job().title())
                    .descripcion(faker.job().keySkills())
                    .imagen(faker.internet().image())
                    .experienciasLaborales(Stream.of(trabajo).collect(Collectors.toList()))
                    .estudios(Stream.of(estudio).collect(Collectors.toList()))
                    .proyectos(Stream.of(proyecto).collect(Collectors.toList()))
                    .habilidades(Stream.of(habilidad).collect(Collectors.toList()))
                    .build();
            var usuario = Usuario.builder()
                    .username(faker.internet().emailAddress())
                    .enabled(true)
                    .locked(false)
                    .password(faker.internet().password())
                    .build();
            persona.setUsuario(usuario);
            trabajo.setPersona(persona);
            proyecto.setPersona(persona);
            estudio.setPersona(persona);
            habilidad.setPersona(persona);
            usuario.setPersona(persona);

            personaRepository.save(persona);
        }
        personaRepository.flush();
        log.warn("Finished data initialization...");
    }

    private LocalDate addRandomAmountOfYearsBetween(LocalDate dateToModify, int min, int max) {
        long randomYearsToAdd = faker.number().numberBetween(min, max);
        return dateToModify.plusYears(randomYearsToAdd);
    }

    @Test
    void testSetup_dbShouldHave10FakePersonasAndUsers() {
        Assertions.assertThat(personaRepository.findAll()).hasSize(10);
        Assertions.assertThat(usuarioRepository.findAll()).hasSize(10);
    }

    @Test
    void getPersonById_ShouldReturnPersona() {
        //given
        Consumer<Persona> hitPersonaByIdEndpointAndCheckValuesConsumer = p -> {
            final Educacion estudioPersonaInDb = educacionRepository.findAll().stream().filter(e -> e.getPersona().getId().equals(p.getId())).findFirst().get();
            final Habilidad habilidadPersonaInDb = habilidadRepository.findAll().stream().filter(h -> h.getPersona().getId().equals(p.getId())).findFirst().get();
            final Proyecto proyectoPersonaInDb = proyectoRepository.findAll().stream().filter(pr -> pr.getPersona().getId().equals(p.getId())).findFirst().get();
            final Trabajo trabajoPersonaInDb = trabajoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(p.getId())).findFirst().get();

            RestAssured.given()
                    .log().all()
                    .port(randomServerPort)
                    .when()
                    .get(API_URL + "/persona/find/" + p.getId())
                    .then()
                    .log().all()
                    .assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(ContentType.JSON)
                    .body("id", is(p.getId().intValue()))
                    .body("nombres", is(p.getNombres()))
                    .body("apellidos", is(p.getApellidos()))
                    .body("fechaNacimiento", is(p.getFechaNacimiento().toString()))
                    .body("nacionalidad", is(p.getNacionalidad().toString()))
                    .body("email", is(p.getEmail()))
                    .body("descripcion", is(p.getDescripcion()))
                    .body("imagen", is(p.getImagen()))
                    .body("usuario.username", is(p.getUsuario().getUsername()))
                    .body("estudios.id", is(List.of(estudioPersonaInDb.getId().intValue())))
                    .body("estudios.institucion", is(Collections.singletonList(estudioPersonaInDb.getInstitucion())))
                    .body("estudios.titulo", is(Collections.singletonList(estudioPersonaInDb.getTitulo())))
                    .body("estudios.lugar", is(Collections.singletonList(estudioPersonaInDb.getLugar())))
                    .body("estudios.estado", is(Collections.singletonList(estudioPersonaInDb.getEstado().toString())))
                    .body("habilidades.id", is(List.of(habilidadPersonaInDb.getId().intValue())))
                    .body("habilidades.nombre", is(Collections.singletonList(habilidadPersonaInDb.getNombre())))
                    .body("habilidades.nivel", is(Collections.singletonList(habilidadPersonaInDb.getNivel())))
                    .body("habilidades.descripcion", is(Collections.singletonList(habilidadPersonaInDb.getDescripcion())))
                    .body("proyectos.id", is(List.of(proyectoPersonaInDb.getId().intValue())))
                    .body("proyectos.nombre", is(Collections.singletonList(proyectoPersonaInDb.getNombre())))
                    .body("proyectos.descripcion", is(Collections.singletonList(proyectoPersonaInDb.getDescripcion())))
                    .body("experienciasLaborales.id", is(List.of(trabajoPersonaInDb.getId().intValue())))
                    .body("experienciasLaborales.empresa", is(List.of(trabajoPersonaInDb.getEmpresa())))
                    .body("experienciasLaborales.cargo", is(List.of(trabajoPersonaInDb.getCargo())))
                    .body("experienciasLaborales.lugar", is(List.of(trabajoPersonaInDb.getLugar())))
                    .body("experienciasLaborales.desde", is(List.of(trabajoPersonaInDb.getDesde().toString())))
                    .body("experienciasLaborales.hasta", is(List.of(trabajoPersonaInDb.getHasta().toString())));
        };

        //when
        //then
        personaRepository.findAll().forEach(hitPersonaByIdEndpointAndCheckValuesConsumer);
    }

    @Test
    void getAllPersonas_ShouldReturnAllPersonas() {
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .when()
                .get(API_URL + "/persona/all")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", Matchers.is(10));
    }
}
