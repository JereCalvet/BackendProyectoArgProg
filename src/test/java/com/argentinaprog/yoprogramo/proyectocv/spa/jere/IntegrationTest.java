package com.argentinaprog.yoprogramo.proyectocv.spa.jere;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.*;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.dto.*;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.repositories.*;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.security.JwtConfig;
import com.auth0.jwt.JWT;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class IntegrationTest extends AbstractContainerBaseTest {

    private final Faker faker = new Faker();

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

    @Autowired
    private JwtConfig jwtConfig;

    @LocalServerPort
    int randomServerPort;

    private static final String API_URL = "/api/v1";

    @AfterEach
    void tearDown() {
        log.info("Starting data deletion...");
        personaRepository.deleteAll();
        usuarioRepository.deleteAll();
        educacionRepository.deleteAll();
        habilidadRepository.deleteAll();
        proyectoRepository.deleteAll();
        log.info("Finished data deletion...");
    }

    @BeforeEach
    void setUp() {
        setUpFakeData();
    }

    private void setUpFakeData() {
        log.info("Starting data initialization...");
        for (int i = 0; i < 10; i++) {
            LocalDate fechaNacimiento = faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            LocalDate trabajoDesde = addRandomAmountOfYearsBetween(fechaNacimiento, 12, 20); //facha nacimiento + random(min max  -> años estudiando)
            LocalDate trabajoHasta = addRandomAmountOfYearsBetween(trabajoDesde, 1, 30); //trabajo desde + random(min max  -> años trabajando)
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
        log.info("Finished data initialization...");
    }

    private LocalDate addRandomAmountOfYearsBetween(LocalDate dateToModify, int min, int max) {
        long randomYearsToAdd = faker.number().numberBetween(min, max);
        return dateToModify.plusYears(randomYearsToAdd);
    }

    @Test
    void testSetup_dbShouldHave10FakePersonasAndUsers() {
        assertThat(personaRepository.findAll()).hasSize(10);
        assertThat(usuarioRepository.findAll()).hasSize(10);
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
                    .body("experienciasLaborales.hasta", is(List.of(trabajoPersonaInDb.getHasta().toString())))
                    .body("size()", is(14));
        };

        //when
        //then
        personaRepository.findAll().forEach(hitPersonaByIdEndpointAndCheckValuesConsumer);
    }

    @Test
    void getPersonById_WhenPersonaNotFound_ShouldReturnError() {
        //given
        final long nonExistentPersonaId = Long.MAX_VALUE;
        final String errorMsg = String.format("Persona id %d no encontrada.", nonExistentPersonaId);
        final String requestPath = API_URL + "/persona/find/" + nonExistentPersonaId;

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .when()
                .get(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    @Test
    void addPersona_ShouldAddNewPersona() {
        //given
        final String nombres = "Jeremias";
        final String apellidos = "Calvet";
        final String email = "jere_calvet@gmail.com";
        final LocalDate fechaNacimiento = LocalDate.of(2000, 8, 27);
        final Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        final String descripcion = "acerca de test";
        final String imagen = "assets/imagen.jpg";
        final String ocupacion = "tester";

        PersonaDto addPersonaRequestDto = new PersonaDto(
                nombres,
                apellidos,
                fechaNacimiento,
                nacionalidad,
                email,
                descripcion,
                imagen,
                ocupacion,
                null,
                null,
                null,
                null,
                null
        );

        Usuario user = Usuario.builder()
                .username(email)
                .build();
        usuarioRepository.save(user);
        String accessToken = JWT.create()
                .withSubject(email)
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .contentType(ContentType.JSON)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .body(addPersonaRequestDto)
                .when()
                .post(API_URL + "/persona/add")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .contentType(ContentType.JSON)
                .body("id", isA(Integer.class))
                .body("nombres", is(nombres))
                .body("apellidos", is(apellidos))
                .body("fechaNacimiento", is(fechaNacimiento.toString()))
                .body("nacionalidad", is(nacionalidad.toString()))
                .body("email", is(email))
                .body("descripcion", is(descripcion))
                .body("imagen", is(imagen))
                .body("estudios", emptyCollectionOf(Educacion.class))
                .body("habilidades", emptyCollectionOf(Habilidad.class))
                .body("proyectos", emptyCollectionOf(Proyecto.class))
                .body("experienciasLaborales", emptyCollectionOf(Trabajo.class))
                .body("size()", is(14));
    }

    @Test
    void addPersona_WhenUnauthenticated_ShouldBeForbiddenToAddNewPersona() {
        //given
        final String nombres = "Jeremias";
        final String apellidos = "Calvet";
        final String email = "jere_calvet@gmail.com";
        final LocalDate fechaNacimiento = LocalDate.of(2000, 8, 27);
        final Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        final String descripcion = "acerca de test";
        final String imagen = "assets/imagen.jpg";
        final String ocupacion = "tester";

        PersonaDto addPersonaRequestDto = new PersonaDto(
                nombres,
                apellidos,
                fechaNacimiento,
                nacionalidad,
                email,
                descripcion,
                imagen,
                ocupacion,
                null,
                null,
                null,
                null,
                null
        );

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .contentType(ContentType.JSON)
                .body(addPersonaRequestDto)
                .when()
                .post(API_URL + "/persona/add")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void addPersona_WhenCurrentUserAlreadyHasAPersona_ShouldReturnError() {
        //given
        final String nombres = "Jeremias";
        final String apellidos = "Calvet";
        final String email = "jere_calvet@gmail.com";
        final LocalDate fechaNacimiento = LocalDate.of(2000, 8, 27);
        final Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        final String descripcion = "acerca de test";
        final String imagen = "assets/imagen.jpg";
        final String ocupacion = "tester";

        var personaAlreadyInDb = Persona.builder()
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .nacionalidad(nacionalidad)
                .email(email)
                .ocupacion(ocupacion)
                .descripcion(descripcion)
                .imagen(imagen)
                .build();
        var usuario = Usuario.builder()
                .username(email)
                .enabled(true)
                .locked(false)
                .password(faker.internet().password())
                .build();
        personaAlreadyInDb.setUsuario(usuario);
        usuario.setPersona(personaAlreadyInDb);
        personaRepository.saveAndFlush(personaAlreadyInDb);

        PersonaDto addPersonaRequestDto = new PersonaDto(
                nombres,
                apellidos,
                fechaNacimiento,
                nacionalidad,
                email,
                descripcion,
                imagen,
                ocupacion,
                null,
                null,
                null,
                null,
                null
        );

        String accessToken = JWT.create()
                .withSubject(email)
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final String errorMsg = String.format("El usuario %s ya tiene una persona creada.", email);
        final String requestPath = API_URL + "/persona/add";

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .contentType(ContentType.JSON)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .body(addPersonaRequestDto)
                .when()
                .post(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.CONFLICT.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.CONFLICT.value()))
                .body("error", is(HttpStatus.CONFLICT.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    @Test
    void updatePersona_ShouldUpdatePersona() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final String nombresUpdate = "Jeremias";
        final String apellidosUpdate = "Calvet";
        final String emailUpdate = "jere_calvet@gmail.com";
        final LocalDate fechaNacimientoUpdate = LocalDate.of(2000, 8, 27);
        final Nacionalidades nacionalidadUpdate = Nacionalidades.ARGENTINA;
        final String descripcionUpdate = "acerca de test";
        final String imagenUpdate = "assets/imagen.jpg";
        final String ocupacionUpdate = "tester";

        PersonaDto updatePersonaRequestDto = new PersonaDto(
                nombresUpdate,
                apellidosUpdate,
                fechaNacimientoUpdate,
                nacionalidadUpdate,
                emailUpdate,
                descripcionUpdate,
                imagenUpdate,
                ocupacionUpdate,
                null,
                null,
                null,
                null,
                null
        );

        String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Educacion estudioPersonaInDb = educacionRepository.findAll().stream().filter(e -> e.getPersona().getId().equals(personaInDb.getId())).findFirst().get();
        final Habilidad habilidadPersonaInDb = habilidadRepository.findAll().stream().filter(h -> h.getPersona().getId().equals(personaInDb.getId())).findFirst().get();
        final Proyecto proyectoPersonaInDb = proyectoRepository.findAll().stream().filter(pr -> pr.getPersona().getId().equals(personaInDb.getId())).findFirst().get();
        final Trabajo trabajoPersonaInDb = trabajoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaInDb.getId())).findFirst().get();

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .contentType(ContentType.JSON)
                .body(updatePersonaRequestDto)
                .when()
                .put(API_URL + "/persona/update/" + personaInDb.getId())
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", is(personaInDb.getId().intValue()))
                .body("nombres", is(nombresUpdate))
                .body("nombres", not(personaInDb.getNombres()))
                .body("apellidos", is(apellidosUpdate))
                .body("apellidos", not(personaInDb.getNombres()))
                .body("fechaNacimiento", is(fechaNacimientoUpdate.toString()))
                .body("fechaNacimiento", not(personaInDb.getFechaNacimiento().toString()))
                .body("nacionalidad", is(nacionalidadUpdate.toString()))
                .body("email", is(emailUpdate))
                .body("email", not(personaInDb.getEmail()))
                .body("descripcion", is(descripcionUpdate))
                .body("descripcion", not(personaInDb.getDescripcion()))
                .body("imagen", is(imagenUpdate))
                .body("imagen", not(personaInDb.getImagen()))
                .body("ocupacion", is(ocupacionUpdate))
                .body("ocupacion", not(personaInDb.getOcupacion()))
                .body("usuario.username", is(personaInDb.getUsuario().getUsername()))
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
                .body("experienciasLaborales.hasta", is(List.of(trabajoPersonaInDb.getHasta().toString())))
                .body("size()", is(14));
    }

    @Test
    void updatePersona_WhenPersonaIdIsNotFound_ShouldReturnError() {
        //given
        final long nonExistentPersonaId = Long.MAX_VALUE;
        final String errorMsg = String.format("Persona id %d no encontrada.", nonExistentPersonaId);
        final String requestPath = API_URL + "/persona/update/" + nonExistentPersonaId;

        final String nombresUpdate = "Jeremias";
        final String apellidosUpdate = "Calvet";
        final String emailUpdate = "jere_calvet@gmail.com";
        final LocalDate fechaNacimientoUpdate = LocalDate.of(2000, 8, 27);
        final Nacionalidades nacionalidadUpdate = Nacionalidades.ARGENTINA;
        final String descripcionUpdate = "acerca de test";
        final String imagenUpdate = "assets/imagen.jpg";
        final String ocupacionUpdate = "tester";
        PersonaDto updatePersonaRequestDto = new PersonaDto(
                nombresUpdate,
                apellidosUpdate,
                fechaNacimientoUpdate,
                nacionalidadUpdate,
                emailUpdate,
                descripcionUpdate,
                imagenUpdate,
                ocupacionUpdate,
                null,
                null,
                null,
                null,
                null
        );

        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .contentType(ContentType.JSON)
                .body(updatePersonaRequestDto)
                .when()
                .put(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    @Test
    void deletePersona_ShouldDeletePersona() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToDelete = personaInDb.getId();

        String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .when()
                .delete(API_URL + "/persona/delete/" + personaIdToDelete)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    void deletePersona_WhenUnauthenticated_ShouldBeForbiddenToDeletePersona() {
        //given
        final Long personaIdToDelete = personaRepository.findAll().stream().findFirst().get().getId();

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .when()
                .delete(API_URL + "/persona/delete/" + personaIdToDelete)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void deletePersona_WhenPersonaIdIsNotFound_ShouldReturnError() {
        //given
        final long nonExistentPersonaId = Long.MAX_VALUE;
        final String errorMsg = String.format("Persona id %d no encontrada.", nonExistentPersonaId);
        final String requestPath = API_URL + "/persona/delete/" + nonExistentPersonaId;

        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .when()
                .delete(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
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

    @Test
    void getCurrentPersona_WhenLoggedUserHasGotPersona_ShouldReturnHisPersona() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Educacion estudioPersonaInDb = educacionRepository.findAll().stream().filter(e -> e.getPersona().getId().equals(personaInDb.getId())).findFirst().get();
        final Habilidad habilidadPersonaInDb = habilidadRepository.findAll().stream().filter(h -> h.getPersona().getId().equals(personaInDb.getId())).findFirst().get();
        final Proyecto proyectoPersonaInDb = proyectoRepository.findAll().stream().filter(pr -> pr.getPersona().getId().equals(personaInDb.getId())).findFirst().get();
        final Trabajo trabajoPersonaInDb = trabajoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaInDb.getId())).findFirst().get();
        final Usuario userOfCurrentPersonaInDb = personaInDb.getUsuario();
        String accessToken = JWT.create()
                .withSubject(userOfCurrentPersonaInDb.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .when()
                .get(API_URL + "/persona/current")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", is(personaInDb.getId().intValue()))
                .body("nombres", is(personaInDb.getNombres()))
                .body("apellidos", is(personaInDb.getApellidos()))
                .body("fechaNacimiento", is(personaInDb.getFechaNacimiento().toString()))
                .body("nacionalidad", is(personaInDb.getNacionalidad().toString()))
                .body("email", is(personaInDb.getEmail()))
                .body("descripcion", is(personaInDb.getDescripcion()))
                .body("imagen", is(personaInDb.getImagen()))
                .body("usuario.username", is(personaInDb.getUsuario().getUsername()))
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
                .body("experienciasLaborales.hasta", is(List.of(trabajoPersonaInDb.getHasta().toString())))
                .body("size()", is(14));
    }

    @Test
    void getCurrentPersona_WhenLoggedUserHasNotGotPersona_ShouldReturnHisPersona() {
        //given
        final String username = faker.internet().emailAddress();
        Usuario newUserWithoutPerson = Usuario.builder()
                .username(username)
                .enabled(true)
                .locked(false)
                .password(faker.internet().password())
                .build();
        usuarioRepository.saveAndFlush(newUserWithoutPerson);

        String accessToken = JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final String errorMsg = String.format("El usuario %s no tiene una persona creada.", username);
        final String requestPath = API_URL + "/persona/current";

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .when()
                .get(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    @Test
    void getCurrentPersona_WhenUnauthenticated_ShouldReturnNoPersonaMessage() {
        //given
        final String msg = "Usuario no encontrado.";
        final String requestPath = API_URL + "/persona/current";

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .when()
                .get(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.ACCEPTED.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.ACCEPTED.value()))
                .body("error", is(HttpStatus.ACCEPTED.getReasonPhrase()))
                .body("message", is(msg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    // ------------------- Trabajos -----------------------------

    @Test
    void addTrabajo_ShouldAddNewTrabajo() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToAddTrabajo = personaInDb.getId();

        final LocalDate desdeInTrabajoToAdd = LocalDate.now();
        final LocalDate hastaInTrabajoToAdd = addRandomAmountOfYearsBetween(desdeInTrabajoToAdd, 1, 30);
        final String empresaInTrabajoToAdd = faker.company().name();
        final String cargoInTrabajoToAdd = faker.job().position();
        final String lugarInTrabajoToAdd = faker.address().city();
        final TrabajoDto addTrabajoRequestDto = new TrabajoDto(
                empresaInTrabajoToAdd,
                cargoInTrabajoToAdd,
                lugarInTrabajoToAdd,
                desdeInTrabajoToAdd,
                hastaInTrabajoToAdd
        );

        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Educacion estudioPersonaInDb = educacionRepository.findAll().stream().filter(e -> e.getPersona().getId().equals(personaIdToAddTrabajo)).findFirst().get();
        final Habilidad habilidadPersonaInDb = habilidadRepository.findAll().stream().filter(h -> h.getPersona().getId().equals(personaIdToAddTrabajo)).findFirst().get();
        final Proyecto proyectoPersonaInDb = proyectoRepository.findAll().stream().filter(pr -> pr.getPersona().getId().equals(personaIdToAddTrabajo)).findFirst().get();
        final Trabajo trabajoPersonaInDb = trabajoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToAddTrabajo)).findFirst().get();

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .contentType(ContentType.JSON)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .body(addTrabajoRequestDto)
                .when()
                .post(String.format("%s/persona/add/%s/trabajos/", API_URL, personaIdToAddTrabajo))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .contentType(ContentType.JSON)
                .body("id", is(personaInDb.getId().intValue()))
                .body("nombres", is(personaInDb.getNombres()))
                .body("apellidos", is(personaInDb.getApellidos()))
                .body("fechaNacimiento", is(personaInDb.getFechaNacimiento().toString()))
                .body("nacionalidad", is(personaInDb.getNacionalidad().toString()))
                .body("email", is(personaInDb.getEmail()))
                .body("descripcion", is(personaInDb.getDescripcion()))
                .body("imagen", is(personaInDb.getImagen()))
                .body("usuario.username", is(personaInDb.getUsuario().getUsername()))
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
                .body("experienciasLaborales.id", hasSize(2))
                .body("experienciasLaborales.empresa", hasItems(trabajoPersonaInDb.getEmpresa(), empresaInTrabajoToAdd))
                .body("experienciasLaborales.cargo", hasItems(trabajoPersonaInDb.getCargo(), cargoInTrabajoToAdd))
                .body("experienciasLaborales.lugar", hasItems(trabajoPersonaInDb.getLugar(), lugarInTrabajoToAdd))
                .body("experienciasLaborales.desde", hasItems(trabajoPersonaInDb.getDesde().toString(), desdeInTrabajoToAdd.toString()))
                .body("experienciasLaborales.hasta", hasItems(trabajoPersonaInDb.getHasta().toString(), hastaInTrabajoToAdd.toString()))
                .body("size()", is(14));
    }

    @Test
    void addTrabajo_WhenUnauthenticated_ShouldBeForbiddenToAddTrabajo() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToAddTrabajo = personaInDb.getId();

        final TrabajoDto addTrabajoRequestDto = new TrabajoDto(
                faker.company().name(),
                faker.job().position(),
                faker.address().city(),
                LocalDate.now(),
                addRandomAmountOfYearsBetween(LocalDate.now(), 1, 30)
        );

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .contentType(ContentType.JSON)
                .body(addTrabajoRequestDto)
                .when()
                .post(String.format("%s/persona/add/%s/trabajos/", API_URL, personaIdToAddTrabajo))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void addTrabajo_WhenPersonaIdIsNotFound_ShouldReturnError() {
        //given
        final String username = faker.internet().emailAddress();
        Usuario newUserWithoutPerson = Usuario.builder()
                .username(username)
                .enabled(true)
                .locked(false)
                .password(faker.internet().password())
                .build();
        usuarioRepository.saveAndFlush(newUserWithoutPerson);

        final String accessToken = JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final TrabajoDto addTrabajoRequestDto = new TrabajoDto(
                faker.company().name(),
                faker.job().position(),
                faker.address().city(),
                LocalDate.now(),
                addRandomAmountOfYearsBetween(LocalDate.now(), 1, 30)
        );

        final long nonExistentPersonaId = Long.MAX_VALUE;
        final String errorMsg = String.format("Persona id %d no encontrada.", nonExistentPersonaId);
        final String requestPath = String.format("%s/persona/add/%s/trabajos/", API_URL, nonExistentPersonaId);

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .contentType(ContentType.JSON)
                .body(addTrabajoRequestDto)
                .when()
                .post(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    @Test
    void updateTrabajo_ShouldUpdateTrabajo() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToUpdateTrabajo = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Educacion estudioPersonaInDb = educacionRepository.findAll().stream().filter(e -> e.getPersona().getId().equals(personaIdToUpdateTrabajo)).findFirst().get();
        final Habilidad habilidadPersonaInDb = habilidadRepository.findAll().stream().filter(h -> h.getPersona().getId().equals(personaIdToUpdateTrabajo)).findFirst().get();
        final Proyecto proyectoPersonaInDb = proyectoRepository.findAll().stream().filter(pr -> pr.getPersona().getId().equals(personaIdToUpdateTrabajo)).findFirst().get();
        final Trabajo trabajoPersonaInDb = trabajoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToUpdateTrabajo)).findFirst().get();
        final Long trabajoIdToUpdate = trabajoPersonaInDb.getId();

        final LocalDate desdeInTrabajoToUpdate = LocalDate.now();
        final LocalDate hastaInTrabajoToUpdate = addRandomAmountOfYearsBetween(desdeInTrabajoToUpdate, 1, 30);
        final String empresaInTrabajoToUpdate = faker.company().name();
        final String cargoInTrabajoToUpdate = faker.job().position();
        final String lugarInTrabajoToUpdate = faker.address().city();
        TrabajoDto updateTrabajoRequestDto = new TrabajoDto(
                empresaInTrabajoToUpdate,
                cargoInTrabajoToUpdate,
                lugarInTrabajoToUpdate,
                desdeInTrabajoToUpdate,
                hastaInTrabajoToUpdate
        );

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .contentType(ContentType.JSON)
                .body(updateTrabajoRequestDto)
                .when()
                .put(String.format("%s/persona/update/%s/trabajos/%s", API_URL, personaIdToUpdateTrabajo, trabajoIdToUpdate))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", is(personaIdToUpdateTrabajo.intValue()))
                .body("nombres", is(personaInDb.getNombres()))
                .body("apellidos", is(personaInDb.getApellidos()))
                .body("fechaNacimiento", is(personaInDb.getFechaNacimiento().toString()))
                .body("nacionalidad", is(personaInDb.getNacionalidad().toString()))
                .body("email", is(personaInDb.getEmail()))
                .body("descripcion", is(personaInDb.getDescripcion()))
                .body("imagen", is(personaInDb.getImagen()))
                .body("usuario.username", is(personaInDb.getUsuario().getUsername()))
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
                .body("experienciasLaborales.empresa", is(List.of(empresaInTrabajoToUpdate)))
                .body("experienciasLaborales.cargo", is(List.of(cargoInTrabajoToUpdate)))
                .body("experienciasLaborales.lugar", is(List.of(lugarInTrabajoToUpdate)))
                .body("experienciasLaborales.desde", is(List.of(desdeInTrabajoToUpdate.toString())))
                .body("experienciasLaborales.hasta", is(List.of(hastaInTrabajoToUpdate.toString())))
                .body("size()", is(14));
    }

    @Test
    void updateTrabajo_WhenUnauthenticated_ShouldBeForbiddenToUpdateTrabajo() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToUpdateTrabajo = personaInDb.getId();

        final Trabajo trabajoPersonaInDb = trabajoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToUpdateTrabajo)).findFirst().get();
        final Long trabajoIdToUpdate = trabajoPersonaInDb.getId();

        TrabajoDto updateTrabajoRequestDto = new TrabajoDto(
                faker.company().name(),
                faker.job().position(),
                faker.address().city(),
                LocalDate.now(),
                addRandomAmountOfYearsBetween(LocalDate.now(), 1, 30)
        );

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .contentType(ContentType.JSON)
                .body(updateTrabajoRequestDto)
                .when()
                .put(String.format("%s/persona/update/%s/trabajos/%s", API_URL, personaIdToUpdateTrabajo, trabajoIdToUpdate))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void updateTrabajo_WhenPersonaIdIsNotFound_ShouldReturnError() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToUpdateTrabajo = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Long existentTrabajoIdToUpdate = trabajoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToUpdateTrabajo)).findFirst().get().getId();
        TrabajoDto updateTrabajoRequestDto = new TrabajoDto(
                faker.company().name(),
                faker.job().position(),
                faker.address().city(),
                LocalDate.now(),
                addRandomAmountOfYearsBetween(LocalDate.now(), 1, 30)
        );

        final long nonExistentPersonaId = Long.MAX_VALUE;
        final String errorMsg = String.format("Persona id %d no encontrada.", nonExistentPersonaId);
        final String requestPath = String.format("%s/persona/update/%s/trabajos/%s", API_URL, nonExistentPersonaId, existentTrabajoIdToUpdate);

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .contentType(ContentType.JSON)
                .body(updateTrabajoRequestDto)
                .when()
                .put(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    @Test
    void updateTrabajo_WhenTrabajoIdIsNotFound_ShouldReturnError() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToUpdateTrabajo = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        TrabajoDto updateTrabajoRequestDto = new TrabajoDto(
                faker.company().name(),
                faker.job().position(),
                faker.address().city(),
                LocalDate.now(),
                addRandomAmountOfYearsBetween(LocalDate.now(), 1, 30)
        );

        final long nonExistentTrabajoIdToUpdate = Long.MAX_VALUE;
        final String errorMsg = String.format("Trabajo id %d no encontrado.", nonExistentTrabajoIdToUpdate);
        final String requestPath = String.format("%s/persona/update/%s/trabajos/%s", API_URL, personaIdToUpdateTrabajo, nonExistentTrabajoIdToUpdate);

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .contentType(ContentType.JSON)
                .body(updateTrabajoRequestDto)
                .when()
                .put(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    @Test
    void removeTrabajo_ShouldDeleteTrabajo() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToDeleteTrabajo = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Trabajo trabajoPersonaInDb = trabajoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToDeleteTrabajo)).findFirst().get();
        final Long trabajoIdToDelete = trabajoPersonaInDb.getId();

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .when()
                .delete(String.format("%s/persona/remove/%s/trabajos/%s", API_URL, personaIdToDeleteTrabajo, trabajoIdToDelete))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(trabajoRepository.findAll().stream().anyMatch(t -> t.getPersona().getId().equals(personaIdToDeleteTrabajo))).isFalse();
    }

    @Test
    void removeTrabajo_WhenUnauthenticated_ShouldBeForbiddenToDeleteTrabajo() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToDeleteTrabajo = personaInDb.getId();
        final Trabajo trabajoPersonaInDb = trabajoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToDeleteTrabajo)).findFirst().get();
        final Long trabajoIdToDelete = trabajoPersonaInDb.getId();

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .when()
                .delete(String.format("%s/persona/remove/%s/trabajos/%s", API_URL, personaIdToDeleteTrabajo, trabajoIdToDelete))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void removeTrabajo_WhenPersonaIdIsNotFound_ShouldReturnError() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToDeleteTrabajo = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Long existentTrabajoIdToDelete = trabajoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToDeleteTrabajo)).findFirst().get().getId();

        final long nonExistentPersonaId = Long.MAX_VALUE;
        final String errorMsg = String.format("Persona id %d no encontrada.", nonExistentPersonaId);
        final String requestPath = String.format("%s/persona/remove/%s/trabajos/%s", API_URL, nonExistentPersonaId, existentTrabajoIdToDelete);

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .when()
                .delete(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    @Test
    void removeTrabajo_WhenTrabajoIdIsNotFound_ShouldReturnError() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToDeleteTrabajo = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final long nonExistentTrabajoIdToDelete = Long.MAX_VALUE;
        final String errorMsg = String.format("Trabajo id %d no encontrado.", nonExistentTrabajoIdToDelete);
        final String requestPath = String.format("%s/persona/remove/%s/trabajos/%s", API_URL, personaIdToDeleteTrabajo, nonExistentTrabajoIdToDelete);

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .when()
                .delete(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    // ------------------- Educación -----------------------------

    @Test
    void addEstudio_ShouldAddNewEstudio() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToAddEstudio = personaInDb.getId();

        final String institucionInEstudioToAdd = faker.educator().university();
        final String tituloInEstudioToAdd = faker.educator().course();
        final String lugarInEstudioToAdd = faker.address().city();
        final ProgresoEducacion progresoInEstudioToAdd = ProgresoEducacion.values()[faker.number().numberBetween(0, ProgresoEducacion.values().length)];
        final EducacionDto addEstudioRequestDto = new EducacionDto(
                institucionInEstudioToAdd,
                tituloInEstudioToAdd,
                lugarInEstudioToAdd,
                progresoInEstudioToAdd
        );

        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Educacion estudioPersonaInDb = educacionRepository.findAll().stream().filter(e -> e.getPersona().getId().equals(personaIdToAddEstudio)).findFirst().get();
        final Habilidad habilidadPersonaInDb = habilidadRepository.findAll().stream().filter(h -> h.getPersona().getId().equals(personaIdToAddEstudio)).findFirst().get();
        final Proyecto proyectoPersonaInDb = proyectoRepository.findAll().stream().filter(pr -> pr.getPersona().getId().equals(personaIdToAddEstudio)).findFirst().get();
        final Trabajo trabajoPersonaInDb = trabajoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToAddEstudio)).findFirst().get();

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .contentType(ContentType.JSON)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .body(addEstudioRequestDto)
                .when()
                .post(String.format("%s/persona/add/%s/estudios/", API_URL, personaIdToAddEstudio))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .contentType(ContentType.JSON)
                .body("id", is(personaInDb.getId().intValue()))
                .body("nombres", is(personaInDb.getNombres()))
                .body("apellidos", is(personaInDb.getApellidos()))
                .body("fechaNacimiento", is(personaInDb.getFechaNacimiento().toString()))
                .body("nacionalidad", is(personaInDb.getNacionalidad().toString()))
                .body("email", is(personaInDb.getEmail()))
                .body("descripcion", is(personaInDb.getDescripcion()))
                .body("imagen", is(personaInDb.getImagen()))
                .body("usuario.username", is(personaInDb.getUsuario().getUsername()))
                .body("estudios.id", hasSize(2))
                .body("estudios.institucion", hasItems(estudioPersonaInDb.getInstitucion(), institucionInEstudioToAdd))
                .body("estudios.titulo", hasItems(estudioPersonaInDb.getTitulo(), tituloInEstudioToAdd))
                .body("estudios.lugar", hasItems(estudioPersonaInDb.getLugar(), lugarInEstudioToAdd))
                .body("estudios.estado", hasItems(estudioPersonaInDb.getEstado().toString(), progresoInEstudioToAdd.toString()))
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
                .body("experienciasLaborales.hasta", is(List.of(trabajoPersonaInDb.getHasta().toString())))
                .body("size()", is(14));
    }

    @Test
    void addEstudio_WhenUnauthenticated_ShouldBeForbiddenToAddEstudio() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToAddEstudio = personaInDb.getId();

        final EducacionDto addEstudioRequestDto = new EducacionDto(
                faker.educator().university(),
                faker.educator().course(),
                faker.address().city(),
                ProgresoEducacion.values()[faker.number().numberBetween(0, ProgresoEducacion.values().length)]
        );

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .contentType(ContentType.JSON)
                .body(addEstudioRequestDto)
                .when()
                .post(String.format("%s/persona/add/%s/estudios/", API_URL, personaIdToAddEstudio))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void addEstudio_WhenPersonaIdIsNotFound_ShouldReturnError() {
        //given
        final String username = faker.internet().emailAddress();
        Usuario newUserWithoutPerson = Usuario.builder()
                .username(username)
                .enabled(true)
                .locked(false)
                .password(faker.internet().password())
                .build();
        usuarioRepository.saveAndFlush(newUserWithoutPerson);

        final String accessToken = JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final EducacionDto addEstudioRequestDto = new EducacionDto(
                faker.educator().university(),
                faker.educator().course(),
                faker.address().city(),
                ProgresoEducacion.values()[faker.number().numberBetween(0, ProgresoEducacion.values().length)]
        );

        final long nonExistentPersonaId = Long.MAX_VALUE;
        final String errorMsg = String.format("Persona id %d no encontrada.", nonExistentPersonaId);
        final String requestPath = String.format("%s/persona/add/%s/estudios/", API_URL, nonExistentPersonaId);

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .contentType(ContentType.JSON)
                .body(addEstudioRequestDto)
                .when()
                .post(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    @Test
    void updateEstudio_ShouldUpdateEstudio() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToUpdateEstudio = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Educacion estudioPersonaInDb = educacionRepository.findAll().stream().filter(e -> e.getPersona().getId().equals(personaIdToUpdateEstudio)).findFirst().get();
        final Habilidad habilidadPersonaInDb = habilidadRepository.findAll().stream().filter(h -> h.getPersona().getId().equals(personaIdToUpdateEstudio)).findFirst().get();
        final Proyecto proyectoPersonaInDb = proyectoRepository.findAll().stream().filter(pr -> pr.getPersona().getId().equals(personaIdToUpdateEstudio)).findFirst().get();
        final Trabajo trabajoPersonaInDb = trabajoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToUpdateEstudio)).findFirst().get();
        final Long estudioIdToUpdate = estudioPersonaInDb.getId();

        final String institucionInEstudioToUpdate = faker.educator().university();
        final String tituloInEstudioToUpdate = faker.educator().course();
        final String lugarInEstudioToUpdate = faker.address().city();
        final ProgresoEducacion progresoInEstudioToUpdate = ProgresoEducacion.values()[faker.number().numberBetween(0, ProgresoEducacion.values().length)];
        final EducacionDto updateEstudioRequestDto = new EducacionDto(
                institucionInEstudioToUpdate,
                tituloInEstudioToUpdate,
                lugarInEstudioToUpdate,
                progresoInEstudioToUpdate
        );

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .contentType(ContentType.JSON)
                .body(updateEstudioRequestDto)
                .when()
                .put(String.format("%s/persona/update/%s/estudios/%s", API_URL, personaIdToUpdateEstudio, estudioIdToUpdate))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", is(personaIdToUpdateEstudio.intValue()))
                .body("nombres", is(personaInDb.getNombres()))
                .body("apellidos", is(personaInDb.getApellidos()))
                .body("fechaNacimiento", is(personaInDb.getFechaNacimiento().toString()))
                .body("nacionalidad", is(personaInDb.getNacionalidad().toString()))
                .body("email", is(personaInDb.getEmail()))
                .body("descripcion", is(personaInDb.getDescripcion()))
                .body("imagen", is(personaInDb.getImagen()))
                .body("usuario.username", is(personaInDb.getUsuario().getUsername()))
                .body("estudios.id", is(List.of(estudioPersonaInDb.getId().intValue())))
                .body("estudios.institucion", is(Collections.singletonList(institucionInEstudioToUpdate)))
                .body("estudios.titulo", is(Collections.singletonList(tituloInEstudioToUpdate)))
                .body("estudios.lugar", is(Collections.singletonList(lugarInEstudioToUpdate)))
                .body("estudios.estado", is(Collections.singletonList(progresoInEstudioToUpdate.toString())))
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
                .body("experienciasLaborales.hasta", is(List.of(trabajoPersonaInDb.getHasta().toString())))
                .body("size()", is(14));
    }

    @Test
    void updateEstudio_WhenUnauthenticated_ShouldBeForbiddenToUpdateEstudio() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToUpdateEstudio = personaInDb.getId();

        final Educacion estudioPersonaInDb = educacionRepository.findAll().stream().filter(e -> e.getPersona().getId().equals(personaIdToUpdateEstudio)).findFirst().get();
        final Long estudioIdToUpdate = estudioPersonaInDb.getId();

        final EducacionDto updateEstudioRequestDto = new EducacionDto(
                faker.educator().university(),
                faker.educator().course(),
                faker.address().city(),
                ProgresoEducacion.values()[faker.number().numberBetween(0, ProgresoEducacion.values().length)]
        );

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .contentType(ContentType.JSON)
                .body(updateEstudioRequestDto)
                .when()
                .put(String.format("%s/persona/update/%s/estudios/%s", API_URL, personaIdToUpdateEstudio, estudioIdToUpdate))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void updateEstudio_WhenPersonaIdIsNotFound_ShouldReturnError() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToUpdateEstudio = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Long existentEstudioIdToUpdate = educacionRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToUpdateEstudio)).findFirst().get().getId();

        final EducacionDto updateEstudioRequestDto = new EducacionDto(
                faker.educator().university(),
                faker.educator().course(),
                faker.address().city(),
                ProgresoEducacion.values()[faker.number().numberBetween(0, ProgresoEducacion.values().length)]
        );

        final long nonExistentPersonaId = Long.MAX_VALUE;
        final String errorMsg = String.format("Persona id %d no encontrada.", nonExistentPersonaId);
        final String requestPath = String.format("%s/persona/update/%s/estudios/%s", API_URL, nonExistentPersonaId, existentEstudioIdToUpdate);

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .contentType(ContentType.JSON)
                .body(updateEstudioRequestDto)
                .when()
                .put(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    @Test
    void updateEstudio_WhenEstudioIdIsNotFound_ShouldReturnError() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToUpdateEstudio = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final EducacionDto updateEstudioRequestDto = new EducacionDto(
                faker.educator().university(),
                faker.educator().course(),
                faker.address().city(),
                ProgresoEducacion.values()[faker.number().numberBetween(0, ProgresoEducacion.values().length)]
        );

        final long nonExistentEstudioIdToUpdate = Long.MAX_VALUE;
        final String errorMsg = String.format("Estudio id %d no encontrado.", nonExistentEstudioIdToUpdate);
        final String requestPath = String.format("%s/persona/update/%s/estudios/%s", API_URL, personaIdToUpdateEstudio, nonExistentEstudioIdToUpdate);

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .contentType(ContentType.JSON)
                .body(updateEstudioRequestDto)
                .when()
                .put(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    @Test
    void removeEstudio_ShouldDeleteEstudio() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToDeleteEstudio = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Educacion estudioPersonaInDb = educacionRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToDeleteEstudio)).findFirst().get();
        final Long estudioIdToDelete = estudioPersonaInDb.getId();

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .when()
                .delete(String.format("%s/persona/remove/%s/estudios/%s", API_URL, personaIdToDeleteEstudio, estudioIdToDelete))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(educacionRepository.findAll().stream().anyMatch(t -> t.getPersona().getId().equals(personaIdToDeleteEstudio))).isFalse();
    }

    @Test
    void removeEstudio_WhenUnauthenticated_ShouldBeForbiddenToDeleteEstudio() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToDeleteEstudio = personaInDb.getId();
        final Educacion estudioPersonaInDb = educacionRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToDeleteEstudio)).findFirst().get();
        final Long estudioIdToDelete = estudioPersonaInDb.getId();

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .when()
                .delete(String.format("%s/persona/remove/%s/estudios/%s", API_URL, personaIdToDeleteEstudio, estudioIdToDelete))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void removeEstudio_WhenPersonaIdIsNotFound_ShouldReturnError() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToDeleteEstudio = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Long existentEstudioIdToDelete = educacionRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToDeleteEstudio)).findFirst().get().getId();

        final long nonExistentPersonaId = Long.MAX_VALUE;
        final String errorMsg = String.format("Persona id %d no encontrada.", nonExistentPersonaId);
        final String requestPath = String.format("%s/persona/remove/%s/estudios/%s", API_URL, nonExistentPersonaId, existentEstudioIdToDelete);

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .when()
                .delete(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    @Test
    void removeEstudio_WhenEstudioIdIsNotFound_ShouldReturnError() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToDeleteEstudio = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final long nonExistentEstudioIdToDelete = Long.MAX_VALUE;
        final String errorMsg = String.format("Estudio id %d no encontrado.", nonExistentEstudioIdToDelete);
        final String requestPath = String.format("%s/persona/remove/%s/estudios/%s", API_URL, personaIdToDeleteEstudio, nonExistentEstudioIdToDelete);

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .when()
                .delete(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    // ------------------- Proyecto -----------------------------

    @Test
    void addProyecto_ShouldAddNewProyecto() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToAddProyecto = personaInDb.getId();

        final String nombreInProyectoToAdd = faker.funnyName().name();
        final String descripcionInProyectoToAdd = String.join(" ", faker.lorem().words(10));
        final ProyectoDto addProyectoRequestDto = new ProyectoDto(
                nombreInProyectoToAdd,
                descripcionInProyectoToAdd
        );

        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Educacion estudioPersonaInDb = educacionRepository.findAll().stream().filter(e -> e.getPersona().getId().equals(personaIdToAddProyecto)).findFirst().get();
        final Habilidad habilidadPersonaInDb = habilidadRepository.findAll().stream().filter(h -> h.getPersona().getId().equals(personaIdToAddProyecto)).findFirst().get();
        final Proyecto proyectoPersonaInDb = proyectoRepository.findAll().stream().filter(pr -> pr.getPersona().getId().equals(personaIdToAddProyecto)).findFirst().get();
        final Trabajo trabajoPersonaInDb = trabajoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToAddProyecto)).findFirst().get();

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .contentType(ContentType.JSON)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .body(addProyectoRequestDto)
                .when()
                .post(String.format("%s/persona/add/%s/proyectos/", API_URL, personaIdToAddProyecto))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .contentType(ContentType.JSON)
                .body("id", is(personaInDb.getId().intValue()))
                .body("nombres", is(personaInDb.getNombres()))
                .body("apellidos", is(personaInDb.getApellidos()))
                .body("fechaNacimiento", is(personaInDb.getFechaNacimiento().toString()))
                .body("nacionalidad", is(personaInDb.getNacionalidad().toString()))
                .body("email", is(personaInDb.getEmail()))
                .body("descripcion", is(personaInDb.getDescripcion()))
                .body("imagen", is(personaInDb.getImagen()))
                .body("usuario.username", is(personaInDb.getUsuario().getUsername()))
                .body("estudios.id", is(List.of(estudioPersonaInDb.getId().intValue())))
                .body("estudios.institucion", is(Collections.singletonList(estudioPersonaInDb.getInstitucion())))
                .body("estudios.titulo", is(Collections.singletonList(estudioPersonaInDb.getTitulo())))
                .body("estudios.lugar", is(Collections.singletonList(estudioPersonaInDb.getLugar())))
                .body("estudios.estado", is(Collections.singletonList(estudioPersonaInDb.getEstado().toString())))
                .body("habilidades.id", is(List.of(habilidadPersonaInDb.getId().intValue())))
                .body("habilidades.nombre", is(Collections.singletonList(habilidadPersonaInDb.getNombre())))
                .body("habilidades.nivel", is(Collections.singletonList(habilidadPersonaInDb.getNivel())))
                .body("habilidades.descripcion", is(Collections.singletonList(habilidadPersonaInDb.getDescripcion())))
                .body("proyectos.id", hasSize(2))
                .body("proyectos.nombre", hasItems(proyectoPersonaInDb.getNombre(), nombreInProyectoToAdd))
                .body("proyectos.descripcion", hasItems(proyectoPersonaInDb.getDescripcion(), descripcionInProyectoToAdd))
                .body("experienciasLaborales.id", is(List.of(trabajoPersonaInDb.getId().intValue())))
                .body("experienciasLaborales.empresa", is(List.of(trabajoPersonaInDb.getEmpresa())))
                .body("experienciasLaborales.cargo", is(List.of(trabajoPersonaInDb.getCargo())))
                .body("experienciasLaborales.lugar", is(List.of(trabajoPersonaInDb.getLugar())))
                .body("experienciasLaborales.desde", is(List.of(trabajoPersonaInDb.getDesde().toString())))
                .body("experienciasLaborales.hasta", is(List.of(trabajoPersonaInDb.getHasta().toString())))
                .body("size()", is(14));
    }

    @Test
    void addProyecto_WhenUnauthenticated_ShouldBeForbiddenToAddProyecto() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToAddProyecto = personaInDb.getId();

        final ProyectoDto addProyectoRequestDto = new ProyectoDto(
                faker.funnyName().name(),
                String.join(" ", faker.lorem().words(10))
        );

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .contentType(ContentType.JSON)
                .body(addProyectoRequestDto)
                .when()
                .post(String.format("%s/persona/add/%s/proyectos/", API_URL, personaIdToAddProyecto))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void addProyecto_WhenPersonaIdIsNotFound_ShouldReturnError() {
        //given
        final String username = faker.internet().emailAddress();
        Usuario newUserWithoutPerson = Usuario.builder()
                .username(username)
                .enabled(true)
                .locked(false)
                .password(faker.internet().password())
                .build();
        usuarioRepository.saveAndFlush(newUserWithoutPerson);

        final String accessToken = JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final ProyectoDto addProyectoRequestDto = new ProyectoDto(
                faker.funnyName().name(),
                String.join(" ", faker.lorem().words(10))
        );

        final long nonExistentPersonaId = Long.MAX_VALUE;
        final String errorMsg = String.format("Persona id %d no encontrada.", nonExistentPersonaId);
        final String requestPath = String.format("%s/persona/add/%s/proyectos/", API_URL, nonExistentPersonaId);

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .contentType(ContentType.JSON)
                .body(addProyectoRequestDto)
                .when()
                .post(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    @Test
    void updateProyecto_ShouldUpdateProyecto() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToUpdateProyecto = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Educacion estudioPersonaInDb = educacionRepository.findAll().stream().filter(e -> e.getPersona().getId().equals(personaIdToUpdateProyecto)).findFirst().get();
        final Habilidad habilidadPersonaInDb = habilidadRepository.findAll().stream().filter(h -> h.getPersona().getId().equals(personaIdToUpdateProyecto)).findFirst().get();
        final Proyecto proyectoPersonaInDb = proyectoRepository.findAll().stream().filter(pr -> pr.getPersona().getId().equals(personaIdToUpdateProyecto)).findFirst().get();
        final Trabajo trabajoPersonaInDb = trabajoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToUpdateProyecto)).findFirst().get();
        final Long proyectoIdToUpdate = proyectoPersonaInDb.getId();

        final String nombreInProyectoToUpdate = faker.funnyName().name();
        final String descripcionInProyectoToUpdate = String.join(" ", faker.lorem().words(10));
        final ProyectoDto updateProyectoRequestDto = new ProyectoDto(
                nombreInProyectoToUpdate,
                descripcionInProyectoToUpdate
        );

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .contentType(ContentType.JSON)
                .body(updateProyectoRequestDto)
                .when()
                .put(String.format("%s/persona/update/%s/proyectos/%s", API_URL, personaIdToUpdateProyecto, proyectoIdToUpdate))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", is(personaIdToUpdateProyecto.intValue()))
                .body("nombres", is(personaInDb.getNombres()))
                .body("apellidos", is(personaInDb.getApellidos()))
                .body("fechaNacimiento", is(personaInDb.getFechaNacimiento().toString()))
                .body("nacionalidad", is(personaInDb.getNacionalidad().toString()))
                .body("email", is(personaInDb.getEmail()))
                .body("descripcion", is(personaInDb.getDescripcion()))
                .body("imagen", is(personaInDb.getImagen()))
                .body("usuario.username", is(personaInDb.getUsuario().getUsername()))
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
                .body("proyectos.nombre", is(List.of(nombreInProyectoToUpdate)))
                .body("proyectos.descripcion", is(List.of(descripcionInProyectoToUpdate)))
                .body("experienciasLaborales.id", is(List.of(trabajoPersonaInDb.getId().intValue())))
                .body("experienciasLaborales.empresa", is(List.of(trabajoPersonaInDb.getEmpresa())))
                .body("experienciasLaborales.cargo", is(List.of(trabajoPersonaInDb.getCargo())))
                .body("experienciasLaborales.lugar", is(List.of(trabajoPersonaInDb.getLugar())))
                .body("experienciasLaborales.desde", is(List.of(trabajoPersonaInDb.getDesde().toString())))
                .body("experienciasLaborales.hasta", is(List.of(trabajoPersonaInDb.getHasta().toString())))
                .body("size()", is(14));
    }

    @Test
    void updateProyecto_WhenUnauthenticated_ShouldBeForbiddenToUpdateProyecto() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToUpdateProyecto = personaInDb.getId();

        final Proyecto proyectoPersonaInDb = proyectoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToUpdateProyecto)).findFirst().get();
        final Long proyectoIdToUpdate = proyectoPersonaInDb.getId();

        final ProyectoDto updateProyectoRequestDto = new ProyectoDto(
                faker.funnyName().name(),
                String.join(" ", faker.lorem().words(10))
        );

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .contentType(ContentType.JSON)
                .body(updateProyectoRequestDto)
                .when()
                .put(String.format("%s/persona/update/%s/proyectos/%s", API_URL, personaIdToUpdateProyecto, proyectoIdToUpdate))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void updateProyecto_WhenPersonaIdIsNotFound_ShouldReturnError() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToUpdateProyecto = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Long existentProyectoIdToUpdate = proyectoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToUpdateProyecto)).findFirst().get().getId();
        final ProyectoDto updateProyectoRequestDto = new ProyectoDto(
                faker.funnyName().name(),
                String.join(" ", faker.lorem().words(10))
        );

        final long nonExistentPersonaId = Long.MAX_VALUE;
        final String errorMsg = String.format("Persona id %d no encontrada.", nonExistentPersonaId);
        final String requestPath = String.format("%s/persona/update/%s/proyectos/%s", API_URL, nonExistentPersonaId, existentProyectoIdToUpdate);

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .contentType(ContentType.JSON)
                .body(updateProyectoRequestDto)
                .when()
                .put(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    @Test
    void updateProyecto_WhenProyectoIdIsNotFound_ShouldReturnError() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToUpdateProyecto = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final ProyectoDto updateProyectoRequestDto = new ProyectoDto(
                faker.funnyName().name(),
                String.join(" ", faker.lorem().words(10))
        );

        final long nonExistentProyectoIdToUpdate = Long.MAX_VALUE;
        final String errorMsg = String.format("Proyecto id %d no encontrado.", nonExistentProyectoIdToUpdate);
        final String requestPath = String.format("%s/persona/update/%s/proyectos/%s", API_URL, personaIdToUpdateProyecto, nonExistentProyectoIdToUpdate);

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .contentType(ContentType.JSON)
                .body(updateProyectoRequestDto)
                .when()
                .put(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    @Test
    void removeProyecto_ShouldDeleteProyecto() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToDeleteProyecto = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Proyecto proyectoPersonaInDb = proyectoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToDeleteProyecto)).findFirst().get();
        final Long proyectoIdToDelete = proyectoPersonaInDb.getId();

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .when()
                .delete(String.format("%s/persona/remove/%s/proyectos/%s", API_URL, personaIdToDeleteProyecto, proyectoIdToDelete))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(proyectoRepository.findAll().stream().anyMatch(t -> t.getPersona().getId().equals(personaIdToDeleteProyecto))).isFalse();
    }

    @Test
    void removeProyecto_WhenUnauthenticated_ShouldBeForbiddenToDeleteProyecto() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToDeleteProyecto = personaInDb.getId();
        final Proyecto proyectoPersonaInDb = proyectoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToDeleteProyecto)).findFirst().get();
        final Long proyectoIdToDelete = proyectoPersonaInDb.getId();

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .when()
                .delete(String.format("%s/persona/remove/%s/proyectos/%s", API_URL, personaIdToDeleteProyecto, proyectoIdToDelete))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void removeProyecto_WhenPersonaIdIsNotFound_ShouldReturnError() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToDeleteProyecto = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Long existentProyectoIdToDelete = proyectoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToDeleteProyecto)).findFirst().get().getId();

        final long nonExistentPersonaId = Long.MAX_VALUE;
        final String errorMsg = String.format("Persona id %d no encontrada.", nonExistentPersonaId);
        final String requestPath = String.format("%s/persona/remove/%s/proyectos/%s", API_URL, nonExistentPersonaId, existentProyectoIdToDelete);

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .when()
                .delete(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    @Test
    void removeProyecto_WhenProyectoIdIsNotFound_ShouldReturnError() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToDeleteProyecto = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final long nonExistentProyectoIdToDelete = Long.MAX_VALUE;
        final String errorMsg = String.format("Proyecto id %d no encontrado.", nonExistentProyectoIdToDelete);
        final String requestPath = String.format("%s/persona/remove/%s/proyectos/%s", API_URL, personaIdToDeleteProyecto, nonExistentProyectoIdToDelete);

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .when()
                .delete(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    // ------------------- Habilidades -----------------------------

    @Test
    void addHabilidad_ShouldAddNewHabilidad() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToAddHabilidad = personaInDb.getId();

        final String nombreInHabilidadToAdd = faker.job().keySkills();
        final int nivelInHabilidadToAdd = faker.number().numberBetween(1, 100);
        final String descripcionInHabilidadToAdd = String.join(" ", faker.lorem().words(10));
        final HabilidadDto addHabilidadRequestDto = new HabilidadDto(
                nombreInHabilidadToAdd,
                nivelInHabilidadToAdd,
                descripcionInHabilidadToAdd
        );

        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Educacion estudioPersonaInDb = educacionRepository.findAll().stream().filter(e -> e.getPersona().getId().equals(personaIdToAddHabilidad)).findFirst().get();
        final Habilidad habilidadPersonaInDb = habilidadRepository.findAll().stream().filter(h -> h.getPersona().getId().equals(personaIdToAddHabilidad)).findFirst().get();
        final Proyecto proyectoPersonaInDb = proyectoRepository.findAll().stream().filter(pr -> pr.getPersona().getId().equals(personaIdToAddHabilidad)).findFirst().get();
        final Trabajo trabajoPersonaInDb = trabajoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToAddHabilidad)).findFirst().get();

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .contentType(ContentType.JSON)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .body(addHabilidadRequestDto)
                .when()
                .post(String.format("%s/persona/add/%s/habilidades/", API_URL, personaIdToAddHabilidad))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .contentType(ContentType.JSON)
                .body("id", is(personaInDb.getId().intValue()))
                .body("nombres", is(personaInDb.getNombres()))
                .body("apellidos", is(personaInDb.getApellidos()))
                .body("fechaNacimiento", is(personaInDb.getFechaNacimiento().toString()))
                .body("nacionalidad", is(personaInDb.getNacionalidad().toString()))
                .body("email", is(personaInDb.getEmail()))
                .body("descripcion", is(personaInDb.getDescripcion()))
                .body("imagen", is(personaInDb.getImagen()))
                .body("usuario.username", is(personaInDb.getUsuario().getUsername()))
                .body("estudios.id", is(List.of(estudioPersonaInDb.getId().intValue())))
                .body("estudios.institucion", is(Collections.singletonList(estudioPersonaInDb.getInstitucion())))
                .body("estudios.titulo", is(Collections.singletonList(estudioPersonaInDb.getTitulo())))
                .body("estudios.lugar", is(Collections.singletonList(estudioPersonaInDb.getLugar())))
                .body("estudios.estado", is(Collections.singletonList(estudioPersonaInDb.getEstado().toString())))
                .body("habilidades.id", hasSize(2))
                .body("habilidades.nombre", hasItems(habilidadPersonaInDb.getNombre(), nombreInHabilidadToAdd))
                .body("habilidades.nivel", hasItems(habilidadPersonaInDb.getNivel(), nivelInHabilidadToAdd))
                .body("habilidades.descripcion", hasItems(habilidadPersonaInDb.getDescripcion(), descripcionInHabilidadToAdd))
                .body("proyectos.id", is(List.of(proyectoPersonaInDb.getId().intValue())))
                .body("proyectos.nombre", is(Collections.singletonList(proyectoPersonaInDb.getNombre())))
                .body("proyectos.descripcion", is(Collections.singletonList(proyectoPersonaInDb.getDescripcion())))
                .body("experienciasLaborales.id", is(List.of(trabajoPersonaInDb.getId().intValue())))
                .body("experienciasLaborales.empresa", is(List.of(trabajoPersonaInDb.getEmpresa())))
                .body("experienciasLaborales.cargo", is(List.of(trabajoPersonaInDb.getCargo())))
                .body("experienciasLaborales.lugar", is(List.of(trabajoPersonaInDb.getLugar())))
                .body("experienciasLaborales.desde", is(List.of(trabajoPersonaInDb.getDesde().toString())))
                .body("experienciasLaborales.hasta", is(List.of(trabajoPersonaInDb.getHasta().toString())))
                .body("size()", is(14));
    }

    @Test
    void addHabilidad_WhenUnauthenticated_ShouldBeForbiddenToAddHabilidad() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToAddHabilidad = personaInDb.getId();

        final HabilidadDto addHabilidadRequestDto = new HabilidadDto(
                faker.job().keySkills(),
                faker.number().numberBetween(1, 100),
                String.join(" ", faker.lorem().words(10))
        );

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .contentType(ContentType.JSON)
                .body(addHabilidadRequestDto)
                .when()
                .post(String.format("%s/persona/add/%s/habilidades/", API_URL, personaIdToAddHabilidad))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void addHabilidad_WhenPersonaIdIsNotFound_ShouldReturnError() {
        //given
        final String username = faker.internet().emailAddress();
        Usuario newUserWithoutPerson = Usuario.builder()
                .username(username)
                .enabled(true)
                .locked(false)
                .password(faker.internet().password())
                .build();
        usuarioRepository.saveAndFlush(newUserWithoutPerson);

        final String accessToken = JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final HabilidadDto addHabilidadRequestDto = new HabilidadDto(
                faker.job().keySkills(),
                faker.number().numberBetween(1, 100),
                String.join(" ", faker.lorem().words(10))
        );

        final long nonExistentPersonaId = Long.MAX_VALUE;
        final String errorMsg = String.format("Persona id %d no encontrada.", nonExistentPersonaId);
        final String requestPath = String.format("%s/persona/add/%s/habilidades/", API_URL, nonExistentPersonaId);

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .contentType(ContentType.JSON)
                .body(addHabilidadRequestDto)
                .when()
                .post(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    @Test
    void updateHabilidad_ShouldUpdateHabilidad() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToUpdateHabilidad = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Educacion estudioPersonaInDb = educacionRepository.findAll().stream().filter(e -> e.getPersona().getId().equals(personaIdToUpdateHabilidad)).findFirst().get();
        final Habilidad habilidadPersonaInDb = habilidadRepository.findAll().stream().filter(h -> h.getPersona().getId().equals(personaIdToUpdateHabilidad)).findFirst().get();
        final Proyecto proyectoPersonaInDb = proyectoRepository.findAll().stream().filter(pr -> pr.getPersona().getId().equals(personaIdToUpdateHabilidad)).findFirst().get();
        final Trabajo trabajoPersonaInDb = trabajoRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToUpdateHabilidad)).findFirst().get();
        final Long habilidadIdToUpdate = habilidadPersonaInDb.getId();

        final String nombreInHabilidadToUpdate = faker.job().keySkills();
        final int nivelInHabilidadToUpdate = faker.number().numberBetween(1, 100);
        final String descripcionInHabilidadToUpdate = String.join(" ", faker.lorem().words(10));
        final HabilidadDto updateHabilidadRequestDto = new HabilidadDto(
                nombreInHabilidadToUpdate,
                nivelInHabilidadToUpdate,
                descripcionInHabilidadToUpdate
        );

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .contentType(ContentType.JSON)
                .body(updateHabilidadRequestDto)
                .when()
                .put(String.format("%s/persona/update/%s/habilidades/%s", API_URL, personaIdToUpdateHabilidad, habilidadIdToUpdate))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", is(personaIdToUpdateHabilidad.intValue()))
                .body("nombres", is(personaInDb.getNombres()))
                .body("apellidos", is(personaInDb.getApellidos()))
                .body("fechaNacimiento", is(personaInDb.getFechaNacimiento().toString()))
                .body("nacionalidad", is(personaInDb.getNacionalidad().toString()))
                .body("email", is(personaInDb.getEmail()))
                .body("descripcion", is(personaInDb.getDescripcion()))
                .body("imagen", is(personaInDb.getImagen()))
                .body("usuario.username", is(personaInDb.getUsuario().getUsername()))
                .body("estudios.id", is(List.of(estudioPersonaInDb.getId().intValue())))
                .body("estudios.institucion", is(Collections.singletonList(estudioPersonaInDb.getInstitucion())))
                .body("estudios.titulo", is(Collections.singletonList(estudioPersonaInDb.getTitulo())))
                .body("estudios.lugar", is(Collections.singletonList(estudioPersonaInDb.getLugar())))
                .body("estudios.estado", is(Collections.singletonList(estudioPersonaInDb.getEstado().toString())))
                .body("habilidades.id", is(List.of(habilidadPersonaInDb.getId().intValue())))
                .body("habilidades.nombre", is(List.of(nombreInHabilidadToUpdate)))
                .body("habilidades.nivel", is(List.of(nivelInHabilidadToUpdate)))
                .body("habilidades.descripcion", is(List.of(descripcionInHabilidadToUpdate)))
                .body("proyectos.id", is(List.of(proyectoPersonaInDb.getId().intValue())))
                .body("proyectos.nombre", is(Collections.singletonList(proyectoPersonaInDb.getNombre())))
                .body("proyectos.descripcion", is(Collections.singletonList(proyectoPersonaInDb.getDescripcion())))
                .body("experienciasLaborales.id", is(List.of(trabajoPersonaInDb.getId().intValue())))
                .body("experienciasLaborales.id", is(List.of(trabajoPersonaInDb.getId().intValue())))
                .body("experienciasLaborales.empresa", is(List.of(trabajoPersonaInDb.getEmpresa())))
                .body("experienciasLaborales.cargo", is(List.of(trabajoPersonaInDb.getCargo())))
                .body("experienciasLaborales.lugar", is(List.of(trabajoPersonaInDb.getLugar())))
                .body("experienciasLaborales.desde", is(List.of(trabajoPersonaInDb.getDesde().toString())))
                .body("experienciasLaborales.hasta", is(List.of(trabajoPersonaInDb.getHasta().toString())))
                .body("size()", is(14));
    }

    @Test
    void updateHabilidad_WhenUnauthenticated_ShouldBeForbiddenToUpdateHabilidad() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToUpdateHabilidad = personaInDb.getId();

        final Habilidad habilidadPersonaInDb = habilidadRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToUpdateHabilidad)).findFirst().get();
        final Long habilidadIdToUpdate = habilidadPersonaInDb.getId();

        final HabilidadDto updateHabilidadRequestDto = new HabilidadDto(
                faker.job().keySkills(),
                faker.number().numberBetween(1, 100),
                String.join(" ", faker.lorem().words(10))
        );

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .contentType(ContentType.JSON)
                .body(updateHabilidadRequestDto)
                .when()
                .put(String.format("%s/persona/update/%s/habilidades/%s", API_URL, personaIdToUpdateHabilidad, habilidadIdToUpdate))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void updateHabilidad_WhenPersonaIdIsNotFound_ShouldReturnError() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToUpdateHabilidad = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Long existentHabilidadIdToUpdate = habilidadRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToUpdateHabilidad)).findFirst().get().getId();

        final HabilidadDto updateHabilidadRequestDto = new HabilidadDto(
                faker.job().keySkills(),
                faker.number().numberBetween(1, 100),
                String.join(" ", faker.lorem().words(10))
        );

        final long nonExistentPersonaId = Long.MAX_VALUE;
        final String errorMsg = String.format("Persona id %d no encontrada.", nonExistentPersonaId);
        final String requestPath = String.format("%s/persona/update/%s/habilidades/%s", API_URL, nonExistentPersonaId, existentHabilidadIdToUpdate);

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .contentType(ContentType.JSON)
                .body(updateHabilidadRequestDto)
                .when()
                .put(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    @Test
    void updateHabilidad_WhenHabilidadIdIsNotFound_ShouldReturnError() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToUpdateHabilidad = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final HabilidadDto updateHabilidadRequestDto = new HabilidadDto(
                faker.job().keySkills(),
                faker.number().numberBetween(1, 100),
                String.join(" ", faker.lorem().words(10))
        );

        final long nonExistentHabilidadIdToUpdate = Long.MAX_VALUE;
        final String errorMsg = String.format("Habilidad id %d no encontrado.", nonExistentHabilidadIdToUpdate);
        final String requestPath = String.format("%s/persona/update/%s/habilidades/%s", API_URL, personaIdToUpdateHabilidad, nonExistentHabilidadIdToUpdate);

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .contentType(ContentType.JSON)
                .body(updateHabilidadRequestDto)
                .when()
                .put(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    @Test
    void removeHabilidad_ShouldDeleteHabilidad() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToDeleteHabilidad = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Habilidad habilidadPersonaInDb = habilidadRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToDeleteHabilidad)).findFirst().get();
        final Long habilidadIdToDelete = habilidadPersonaInDb.getId();

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .when()
                .delete(String.format("%s/persona/remove/%s/habilidades/%s", API_URL, personaIdToDeleteHabilidad, habilidadIdToDelete))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(habilidadRepository.findAll().stream().anyMatch(t -> t.getPersona().getId().equals(personaIdToDeleteHabilidad))).isFalse();
    }

    @Test
    void removeHabilidad_WhenUnauthenticated_ShouldBeForbiddenToDeleteHabilidad() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToDeleteHabilidad = personaInDb.getId();
        final Habilidad habilidadPersonaInDb = habilidadRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToDeleteHabilidad)).findFirst().get();
        final Long habilidadIdToDelete = habilidadPersonaInDb.getId();

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .when()
                .delete(String.format("%s/persona/remove/%s/habilidades/%s", API_URL, personaIdToDeleteHabilidad, habilidadIdToDelete))
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void removeHabilidad_WhenPersonaIdIsNotFound_ShouldReturnError() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToDeleteHabilidad = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final Long existentHabilidadIdToDelete = habilidadRepository.findAll().stream().filter(t -> t.getPersona().getId().equals(personaIdToDeleteHabilidad)).findFirst().get().getId();

        final long nonExistentPersonaId = Long.MAX_VALUE;
        final String errorMsg = String.format("Persona id %d no encontrada.", nonExistentPersonaId);
        final String requestPath = String.format("%s/persona/remove/%s/habilidades/%s", API_URL, nonExistentPersonaId, existentHabilidadIdToDelete);

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .when()
                .delete(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }

    @Test
    void removeHabilidad_WhenHabilidadIdIsNotFound_ShouldReturnError() {
        //given
        final Persona personaInDb = personaRepository.findAll().stream().findFirst().get();
        final Long personaIdToDeleteHabilidad = personaInDb.getId();
        final String accessToken = JWT.create()
                .withSubject(personaInDb.getUsuario().getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .withIssuer("integration test")
                .withClaim("roles", List.of("ROLE_USER"))
                .sign(jwtConfig.algorithmWithSecret());

        final long nonExistentHabilidadIdToDelete = Long.MAX_VALUE;
        final String errorMsg = String.format("Habilidad id %d no encontrado.", nonExistentHabilidadIdToDelete);
        final String requestPath = String.format("%s/persona/remove/%s/habilidades/%s", API_URL, personaIdToDeleteHabilidad, nonExistentHabilidadIdToDelete);

        //when
        //then
        RestAssured.given()
                .log().all()
                .port(randomServerPort)
                .header("Authorization", String.format("Bearer %s", accessToken))
                .when()
                .delete(requestPath)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("timestamp", is(LocalDate.now().toString()))
                .body("status", is(HttpStatus.NOT_FOUND.value()))
                .body("error", is(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .body("message", is(errorMsg))
                .body("path", is(requestPath))
                .body("size()", is(5));
    }
}
