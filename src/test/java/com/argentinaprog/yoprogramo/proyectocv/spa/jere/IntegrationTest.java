package com.argentinaprog.yoprogramo.proyectocv.spa.jere;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.Nacionalidades;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.Persona;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.Usuario;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.repositories.PersonaRepository;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.repositories.UsuarioRepository;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.time.ZoneId;
import java.util.List;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class IntegrationTest extends AbstractContainerBaseTest {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @LocalServerPort
    int randomServerPort;

    private static final String API_URL = "/api/v1/";

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
        var faker = new Faker();
        log.warn("Starting data initialization...");
        for (int i = 0; i < 10; i++) {
            var persona = Persona.builder()
                    .nombres(faker.name().firstName())
                    .apellidos(faker.name().lastName())
                    .fechaNacimiento(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                    .nacionalidad(Nacionalidades.values()[faker.number().numberBetween(0, Nacionalidades.values().length)])
                    .email(faker.internet().emailAddress())
                    .ocupacion(faker.job().title())
                    .descripcion(faker.job().keySkills())
                    .imagen(faker.internet().image())
                    .experienciasLaborales(List.of())
                    .estudios(List.of())
                    .proyectos(List.of())
                    .habilidades(List.of())
                    .build();
            var usuario = Usuario.builder()
                    .username(faker.internet().emailAddress())
                    .enabled(true)
                    .locked(false)
                    .password(faker.internet().password())
                    .build();
            persona.setUsuario(usuario);
            usuario.setPersona(persona);

            personaRepository.save(persona);
        }
        personaRepository.flush();
        log.warn("Finished data initialization...");
    }

    @Test
    void getAllPersonas_ShouldReturnAllPersonas() {
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .when()
                .get(API_URL + "persona/all")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("size()", Matchers.is(10));
    }
}
