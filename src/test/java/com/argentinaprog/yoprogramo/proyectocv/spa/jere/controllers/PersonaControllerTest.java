package com.argentinaprog.yoprogramo.proyectocv.spa.jere.controllers;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions.PersonaAlreadyExistsException;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions.PersonaNotFoundException;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions.TrabajoNotFoundException;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions.UsuarioNotFoundException;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.Nacionalidades;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.Persona;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.Trabajo;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.dto.PersonaDto;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.dto.TrabajoDto;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.security.JwtConfig;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.security.PasswordConfig;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.services.PersonaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = PersonaController.class)
@Import(value = {PersonaController.class, PasswordConfig.class, JwtConfig.class})
class PersonaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PersonaService personaSvc;

    private static final String API_PERSONA_BASE_URL = "/api/v1/persona";

    @DisplayName("Should return 200 and persona when the id is valid and user is authorized")
    @WithMockUser()
    @Test
    void getPersonaById_WhenAuthorized_ShouldReturn200Persona() {
        //given
        final var id = 1L;
        final String nombres = "Jeremias";
        final String apellidos = "Calvet";
        final var personaJeremias = Persona.builder()
                .id(id)
                .nombres(nombres)
                .apellidos(apellidos)
                .build();
        given(personaSvc.getPersona(id))
                .willReturn(personaJeremias);

        //when
        //then
        try {
            mockMvc.perform(
                            get(API_PERSONA_BASE_URL + "/find/{id}", id)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("nombres").value(nombres))
                    .andExpect(jsonPath("apellidos").value(apellidos))
                    .andExpect(jsonPath("id").value(id));
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return 404 when user is authorized and the user id is invalid")
    @WithMockUser
    @Test
    void getPersonaById_WhenNonExistentIdAndUserAuthorized_ShouldNotGetPersonaReturn404() {
        //given
        final Long nonExistentId = 3L;
        final String ERROR_MSG = String.format("Persona id %d no encontrada.", nonExistentId);
        given(personaSvc.getPersona(nonExistentId))
                .willThrow(new PersonaNotFoundException(nonExistentId));

        //when
        //then
        try {
            mockMvc.perform(
                            get(API_PERSONA_BASE_URL + "/find/{id}", nonExistentId)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(PersonaNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG));

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return 200 and persona when the id is valid and user is unauthenticated")
    @Test
    void getPersonaById_WhenUnauthenticated_ShouldReturn200Persona() {
        //given
        final var id = 1L;
        final String nombres = "Jeremias";
        final String apellidos = "Calvet";
        final var personaJeremias = Persona.builder()
                .id(id)
                .nombres(nombres)
                .apellidos(apellidos)
                .build();
        given(personaSvc.getPersona(id))
                .willReturn(personaJeremias);

        //when
        //then
        try {
            mockMvc.perform(
                            get(API_PERSONA_BASE_URL + "/find/{id}", id)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("nombres").value(nombres))
                    .andExpect(jsonPath("apellidos").value(apellidos))
                    .andExpect(jsonPath("id").value(id));
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return 404 when user is unauthenticated and the user id is invalid")
    @Test
    void getPersonaById_WhenNonExistentIdAndUserUnauthenticated_ShouldNotGetPersonaReturn404() {
        //given
        final Long nonExistentId = 3L;
        final String ERROR_MSG = String.format("Persona id %d no encontrada.", nonExistentId);
        given(personaSvc.getPersona(nonExistentId))
                .willThrow(new PersonaNotFoundException(nonExistentId));

        //when
        //then
        try {
            mockMvc.perform(
                            get(API_PERSONA_BASE_URL + "/find/{id}", nonExistentId)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(PersonaNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG));

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return a 201 status code when current authorized user added the persona")
    @WithMockUser(username = "username@test.com")
    @Test
    void addPersona_WhenPersonaIsAdded_ShouldReturn201() {
        //given
        String nombres = "Jere";
        String apellidos = "Calvet";
        LocalDate fechaNacimiento = LocalDate.now();
        Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        String email = "test@test.com";
        String descripcion = "descripción";
        String imagen = "test.jpg";
        String ocupacion = "Ocupación";
        long personID = 1L;
        var personToAdd = new PersonaDto(
                nombres,
                apellidos,
                fechaNacimiento,
                nacionalidad,
                email,
                descripcion,
                imagen,
                ocupacion,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        var personAdded = Persona.builder()
                .id(personID)
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .nacionalidad(nacionalidad)
                .email(email)
                .descripcion(descripcion)
                .imagen(imagen)
                .ocupacion(ocupacion)
                .build();

        ArgumentCaptor<PersonaDto> personDtoArgumentCaptor = ArgumentCaptor.forClass(PersonaDto.class);
        Mockito.when(personaSvc.addPersona(personDtoArgumentCaptor.capture()))
                .thenReturn(personAdded);

        //when
        //then
        try {
            mockMvc.perform(
                            post(API_PERSONA_BASE_URL + "/add")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(personToAdd)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("id").value(personID))
                    .andExpect(jsonPath("nombres").value(nombres))
                    .andExpect(jsonPath("apellidos").value(apellidos))
                    .andExpect(jsonPath("fechaNacimiento").value(fechaNacimiento.format(DateTimeFormatter.ISO_DATE)))
                    .andExpect(jsonPath("nacionalidad").value(nacionalidad.toString()))
                    .andExpect(jsonPath("descripcion").value(descripcion))
                    .andExpect(jsonPath("imagen").value(imagen))
                    .andExpect(jsonPath("ocupacion").value(ocupacion))
                    .andExpect(jsonPath("email").value(email));

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        final PersonaDto capturedRequestValue = personDtoArgumentCaptor.getValue();
        Assertions.assertThat(capturedRequestValue.getNombres()).isEqualTo(nombres);
        Assertions.assertThat(capturedRequestValue.getApellidos()).isEqualTo(apellidos);
        Assertions.assertThat(capturedRequestValue.getFechaNacimiento()).isEqualTo(fechaNacimiento);
        Assertions.assertThat(capturedRequestValue.getNacionalidad()).isEqualTo(nacionalidad);
        Assertions.assertThat(capturedRequestValue.getEmail()).isEqualTo(email);
        Assertions.assertThat(capturedRequestValue.getDescripcion()).isEqualTo(descripcion);
        Assertions.assertThat(capturedRequestValue.getImagen()).isEqualTo(imagen);
        Assertions.assertThat(capturedRequestValue.getOcupacion()).isEqualTo(ocupacion);
    }

    @DisplayName("Should return a 403 status code when user is unauthorized and shouldn't add persona")
    @Test
    void addPersona_WhenUnauthorized_ShouldNotAddPersonaReturn403() {
        //given
        var personToAdd = new PersonaDto(
                "Jere",
                "Calvet",
                LocalDate.now(),
                Nacionalidades.ARGENTINA,
                "test@test.com",
                "descripción",
                "test.jpg",
                "Ocupación",
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        //when
        //then
        try {
            mockMvc.perform(
                            post(API_PERSONA_BASE_URL + "/add")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(personToAdd)))
                    .andExpect(status().isForbidden());

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        Mockito.verify(personaSvc, Mockito.never()).addPersona(Mockito.any());
    }

    @Test
    @WithMockUser(username = "username@test.com")
    @DisplayName("Should return a 409 status code when current authorized user can't add persona because user already has persona")
    void addPersona_WhenCurrentUserAlreadyHasPersona_ShouldNotAddReturn409() {
        //given
        var personToAdd = new PersonaDto(
                "Jere",
                "Calvet",
                LocalDate.now(),
                Nacionalidades.ARGENTINA,
                "test@test.com",
                "descripción",
                "test.jpg",
                "Ocupación",
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String username = authentication.getName();

        final String ERROR_MSG = String.format("El usuario %s ya tiene una persona creada.", username);

        given(personaSvc.addPersona(Mockito.any(PersonaDto.class)))
                .willThrow(new PersonaAlreadyExistsException(username));
        //when
        //then
        try {
            mockMvc.perform(
                            post(API_PERSONA_BASE_URL + "/add")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(personToAdd)))
                    .andExpect(status().isConflict())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(PersonaAlreadyExistsException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG));
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @Test
    @WithMockUser(username = "username@test.com")
    @DisplayName("Should return a 202 status code when current authorized user can't add persona because user cant be found")
    void addPersona_WhenCurrentUserIsNotFound_ShouldNotAddReturn202() {
        //given
        var personToAdd = new PersonaDto(
                "Jere",
                "Calvet",
                LocalDate.now(),
                Nacionalidades.ARGENTINA,
                "test@test.com",
                "descripción",
                "test.jpg",
                "Ocupación",
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        final String ERROR_MSG = "Usuario no encontrado.";
        given(personaSvc.addPersona(Mockito.any(PersonaDto.class)))
                .willThrow(new UsuarioNotFoundException());
        //when
        //then
        try {
            mockMvc.perform(
                            post(API_PERSONA_BASE_URL + "/add")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(personToAdd)))
                    .andExpect(status().isAccepted())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(UsuarioNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG));
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @Test
    @WithMockUser(username = "username@test.com")
    @DisplayName("Should return a 200 status code when current authorized user updated persona")
    void updatePersona_WhenPersonaIsUpdated_ShouldReturn200UpdatedPersona() {
        //given
        final long personId = 1L;
        final String nombres = "Jere";
        final String apellidos = "Calvet";
        final LocalDate fechaNacimiento = LocalDate.now();
        final Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        final String email = "test@test.com";
        final String descripcion = "descripción";
        final String imagen = "test.jpg";
        final String ocupacion = "Ocupación";

        var personaDataToUpdate = new PersonaDto(
                nombres,
                apellidos,
                fechaNacimiento,
                nacionalidad,
                email,
                descripcion,
                imagen,
                ocupacion,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        var updatedPersona = Persona.builder()
                .id(personId)
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .nacionalidad(nacionalidad)
                .email(email)
                .descripcion(descripcion)
                .imagen(imagen)
                .ocupacion(ocupacion)
                .habilidades(List.of())
                .experienciasLaborales(List.of())
                .estudios(List.of())
                .proyectos(List.of())
                .build();

        ArgumentCaptor<Long> idArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<PersonaDto> personDtoArgumentCaptor = ArgumentCaptor.forClass(PersonaDto.class);
        given(personaSvc.updatePersona(idArgumentCaptor.capture(), personDtoArgumentCaptor.capture()))
                .willReturn(updatedPersona);

        //when
        //then
        try {
            mockMvc.perform(
                            put(API_PERSONA_BASE_URL + "/update/{personId}", personId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(personaDataToUpdate)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("id").value(personId))
                    .andExpect(jsonPath("nombres").value(nombres))
                    .andExpect(jsonPath("apellidos").value(apellidos))
                    .andExpect(jsonPath("fechaNacimiento").value(fechaNacimiento.format(DateTimeFormatter.ISO_DATE)))
                    .andExpect(jsonPath("nacionalidad").value(nacionalidad.toString()))
                    .andExpect(jsonPath("descripcion").value(descripcion))
                    .andExpect(jsonPath("imagen").value(imagen))
                    .andExpect(jsonPath("ocupacion").value(ocupacion))
                    .andExpect(jsonPath("email").value(email));

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        final Long idCapturedRequestValue = idArgumentCaptor.getValue();
        Assertions.assertThat(idCapturedRequestValue).isEqualTo(personId);

        final PersonaDto dtoCapturedRequestValue = personDtoArgumentCaptor.getValue();
        Assertions.assertThat(dtoCapturedRequestValue.getNombres()).isEqualTo(nombres);
        Assertions.assertThat(dtoCapturedRequestValue.getApellidos()).isEqualTo(apellidos);
        Assertions.assertThat(dtoCapturedRequestValue.getFechaNacimiento()).isEqualTo(fechaNacimiento);
        Assertions.assertThat(dtoCapturedRequestValue.getNacionalidad()).isEqualTo(nacionalidad);
        Assertions.assertThat(dtoCapturedRequestValue.getEmail()).isEqualTo(email);
        Assertions.assertThat(dtoCapturedRequestValue.getDescripcion()).isEqualTo(descripcion);
        Assertions.assertThat(dtoCapturedRequestValue.getImagen()).isEqualTo(imagen);
        Assertions.assertThat(dtoCapturedRequestValue.getOcupacion()).isEqualTo(ocupacion);
    }

    @DisplayName("Should return a 403 status code when user is unauthorized and shouldn't update persona")
    @Test
    void updatePersona_WhenUnauthorized_ShouldNotUpdatePersonaReturn403() {
        //given
        final long personId = 1L;
        var personToUpdate = new PersonaDto(
                "Jere",
                "Calvet",
                LocalDate.now(),
                Nacionalidades.ARGENTINA,
                "test@test.com",
                "descripción",
                "test.jpg",
                "Ocupación",
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        //when
        //then
        try {
            mockMvc.perform(
                            put(API_PERSONA_BASE_URL + "/update/{personId}", personId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(personToUpdate)))
                    .andExpect(status().isForbidden());

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        Mockito.verify(personaSvc, Mockito.never()).updatePersona(Mockito.any(), Mockito.any());
    }

    @Test
    @WithMockUser(username = "username@test.com")
    @DisplayName("Should return 404 when persona id is invalid and shouldn't update persona")
    void updatePersona_WhenPersonaIdNotFound_ShouldNotUpdatePersonaReturn404() {
        //given
        final Long nonExistenId = 3L;
        final String nombres = "Jere";
        final String apellidos = "Calvet";
        final LocalDate fechaNacimiento = LocalDate.now();
        final Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        final String email = "test@test.com";
        final String descripcion = "descripción";
        final String imagen = "test.jpg";
        final String ocupacion = "Ocupación";

        var personToUpdate = new PersonaDto(
                nombres,
                apellidos,
                fechaNacimiento,
                nacionalidad,
                email,
                descripcion,
                imagen,
                ocupacion,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
        final String ERROR_MSG = String.format("Persona id %d no encontrada.", nonExistenId);

        ArgumentCaptor<Long> idArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<PersonaDto> personDtoArgumentCaptor = ArgumentCaptor.forClass(PersonaDto.class);
        given(personaSvc.updatePersona(idArgumentCaptor.capture(), personDtoArgumentCaptor.capture()))
                .willThrow(new PersonaNotFoundException(nonExistenId));

        //when
        //then
        try {
            mockMvc.perform(
                            put(API_PERSONA_BASE_URL + "/update/{id}", nonExistenId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(personToUpdate)))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(PersonaNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG));

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        final Long idCapturedRequestValue = idArgumentCaptor.getValue();
        Assertions.assertThat(idCapturedRequestValue).isEqualTo(nonExistenId);

        final PersonaDto dtoCapturedRequestValue = personDtoArgumentCaptor.getValue();
        Assertions.assertThat(dtoCapturedRequestValue.getNombres()).isEqualTo(nombres);
        Assertions.assertThat(dtoCapturedRequestValue.getApellidos()).isEqualTo(apellidos);
        Assertions.assertThat(dtoCapturedRequestValue.getFechaNacimiento()).isEqualTo(fechaNacimiento);
        Assertions.assertThat(dtoCapturedRequestValue.getNacionalidad()).isEqualTo(nacionalidad);
        Assertions.assertThat(dtoCapturedRequestValue.getEmail()).isEqualTo(email);
        Assertions.assertThat(dtoCapturedRequestValue.getDescripcion()).isEqualTo(descripcion);
        Assertions.assertThat(dtoCapturedRequestValue.getImagen()).isEqualTo(imagen);
        Assertions.assertThat(dtoCapturedRequestValue.getOcupacion()).isEqualTo(ocupacion);
    }

    @DisplayName("Should return a 204 status code when current authorized user deleted a persona")
    @WithMockUser()
    @Test
    void deletePersona_WhenPersonaIsDeleted_ShouldReturn204() {
        //given
        final Long personIdToDelete = 1L;
        ArgumentCaptor<Long> idArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.doNothing().when(personaSvc)
                .deletePersona((idArgumentCaptor.capture()));

        //when
        //then
        try {
            mockMvc.perform(
                            delete(API_PERSONA_BASE_URL + "/delete/{id}", personIdToDelete))
                    .andExpect(status().isNoContent());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        Mockito.verify(personaSvc, Mockito.times(1)).deletePersona(personIdToDelete);
        Assertions.assertThat(idArgumentCaptor.getValue()).isEqualTo(personIdToDelete);
    }

    @DisplayName("Should return a 403 status code when user is unauthorized and shouldn't delete persona")
    @Test
    void deletePersona_WhenUnauthorized_ShouldNotDeleteReturn403() {
        //given
        final Long personIdToDelete = 1L;

        //when
        //then
        try {
            mockMvc.perform(
                            delete(API_PERSONA_BASE_URL + "/delete/{id}", personIdToDelete))
                    .andExpect(status().isForbidden());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        Mockito.verify(personaSvc, Mockito.never()).deletePersona(Mockito.anyLong());
    }

    @DisplayName("Should return 404 when persona id is invalid and shouldn't delete persona")
    @WithMockUser()
    @Test
    void deletePersona_WhenNonExistentId_ShouldNotDeleteReturn404() {
        //given
        final Long nonExistentId = 3L;
        final String ERROR_MSG = String.format("Persona id %d no encontrada.", nonExistentId);

        BDDMockito.willThrow(new PersonaNotFoundException(nonExistentId))
                .given(personaSvc).deletePersona(nonExistentId);

        //when
        //then
        try {
            mockMvc.perform(
                            delete(API_PERSONA_BASE_URL + "/delete/{id}", nonExistentId)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(PersonaNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG));

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return 200 and all personas when user is authorized")
    @WithMockUser
    @Test
    void getAllPersonas_WhenUserIsAuthorized_ShouldReturn200AllPersonas() {
        //given
        final var persona1 = Persona.builder()
                .id(1L)
                .nombres("Jere")
                .apellidos("Calvet")
                .build();

        final var persona2 = Persona.builder()
                .id(2L)
                .nombres("Nahuel")
                .apellidos("Calvet")
                .build();
        final var personas = List.of(persona1, persona2);

        given(personaSvc.getAllPersonas())
                .willReturn(personas);

        //when
        //then
        try {
            mockMvc.perform(
                            get(API_PERSONA_BASE_URL + "/all"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(persona1.getId()))
                    .andExpect(jsonPath("$[0].nombres").value(persona1.getNombres()))
                    .andExpect(jsonPath("$[0].apellidos").value(persona1.getApellidos()))
                    .andExpect(jsonPath("$[1].id").value(persona2.getId()))
                    .andExpect(jsonPath("$[1].nombres").value(persona2.getNombres()))
                    .andExpect(jsonPath("$[1].apellidos").value(persona2.getApellidos()));

        } catch (Exception e) {
            Assertions.fail("Should not throw any exception");
        }

        Mockito.verify(personaSvc, Mockito.times(1)).getAllPersonas();
    }

    @DisplayName("Should return 200 and all personas when user is unauthorized")
    @Test
    void getAllPersonas_WhenUserIsUnauthorized_ShouldReturn200AllPersonas() {
        //given
        final var persona1 = Persona.builder()
                .id(1L)
                .nombres("Jere")
                .apellidos("Calvet")
                .build();

        final var persona2 = Persona.builder()
                .id(2L)
                .nombres("Nahuel")
                .apellidos("Calvet")
                .build();
        final var personas = List.of(persona1, persona2);

        given(personaSvc.getAllPersonas())
                .willReturn(personas);

        //when
        //then
        try {
            mockMvc.perform(
                            get(API_PERSONA_BASE_URL + "/all"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(persona1.getId()))
                    .andExpect(jsonPath("$[0].nombres").value(persona1.getNombres()))
                    .andExpect(jsonPath("$[0].apellidos").value(persona1.getApellidos()))
                    .andExpect(jsonPath("$[1].id").value(persona2.getId()))
                    .andExpect(jsonPath("$[1].nombres").value(persona2.getNombres()))
                    .andExpect(jsonPath("$[1].apellidos").value(persona2.getApellidos()));

        } catch (Exception e) {
            Assertions.fail("Should not throw any exception");
        }

        Mockito.verify(personaSvc, Mockito.times(1)).getAllPersonas();
    }

    @DisplayName("Should return 200 and the current when user is authorized and has a person")
    @WithMockUser
    @Test
    void currentPersona_WhenUserIsAuthorizedAndHasPersona_ShouldReturn200CurrentPersona() {
        //given
        long personId = 1L;
        String nombres = "Jere";
        String apellidos = "Calvet";
        LocalDate fechaNacimiento = LocalDate.of(1990, 6, 7);
        Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        final var currentPerson = Persona.builder()
                .id(personId)
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .nacionalidad(nacionalidad)
                .build();

        given(personaSvc.getCurrentPersona())
                .willReturn(currentPerson);

        //when
        //then
        try {
            mockMvc.perform(
                            get(API_PERSONA_BASE_URL + "/current"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("id").value(personId))
                    .andExpect(jsonPath("nombres").value(nombres))
                    .andExpect(jsonPath("apellidos").value(apellidos))
                    .andExpect(jsonPath("fechaNacimiento").value(fechaNacimiento.format(DateTimeFormatter.ISO_DATE)))
                    .andExpect(jsonPath("nacionalidad").value(nacionalidad.toString()));

        } catch (Exception e) {
            Assertions.fail("Should not throw any exception");
        }
    }

    @DisplayName("Should return 202 when current user can't be found because is unauthorized or is not in DB")
    @Test
    void currentPersona_WhenUserIsUnauthorizedOrCanNotBeFound_ShouldReturn202() {
        //given
        final String ERROR_MSG = "Usuario no encontrado.";

        BDDMockito.willThrow(new UsuarioNotFoundException())
                .given(personaSvc).getCurrentPersona();
        //when
        //then
        try {
            mockMvc.perform(
                            get(API_PERSONA_BASE_URL + "/current"))
                    .andExpect(status().isAccepted())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(UsuarioNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG));

        } catch (Exception e) {
            Assertions.fail("Should not throw any exception");
        }
    }

    @DisplayName("Should return 404 when current user hasn't created his person yet")
    @WithMockUser(username = "jere@test.com")
    @Test
    void currentPersona_WhenUserDoesNotHavePersona_ShouldReturn404() {
        //given
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String username = authentication.getName();
        final String ERROR_MSG = String.format("El usuario %s no tiene una persona creada.", username);

        BDDMockito.willThrow(new PersonaNotFoundException(username))
                .given(personaSvc).getCurrentPersona();
        //when
        //then
        try {
            mockMvc.perform(
                            get(API_PERSONA_BASE_URL + "/current"))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(PersonaNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG));

        } catch (Exception e) {
            Assertions.fail("Should not throw any exception");
        }
    }

    @DisplayName("Should return 201 and add the trabajo when the persona exists")
    @WithMockUser()
    @Test
    void addTrabajo_WhenPersonaExists_ShouldReturn201UpdatedPersona() {
        //given
        final var empresa = "Carrefour";
        final var cargo = "Tester";
        final var lugar = "Rio Grande";
        final var desde = LocalDate.of(2010, 1, 1);
        final var hasta = LocalDate.of(2012, 1, 1);
        final var trabajoDto = new TrabajoDto(
                empresa,
                cargo,
                lugar,
                desde,
                hasta
        );
        final var idTrabajo = 2L;
        final var trabajoAdded = Trabajo.builder()
                .id(idTrabajo)
                .empresa(empresa)
                .cargo(cargo)
                .lugar(lugar)
                .desde(desde)
                .hasta(hasta)
                .build();
        List<Trabajo> experienciasLaborales = Stream.of(trabajoAdded)
                .collect(Collectors.toList());

        final var personId = 1L;
        final var nombres = "Jere";
        final var apellidos = "Calvet";
        final var fechaNacimiento = LocalDate.now();
        final var nacionalidad = Nacionalidades.ARGENTINA;
        final var email = "test@test.com";
        final var descripcion = "descripción";
        final var imagen = "test.jpg";
        final var ocupacion = "Ocupación";
        var personaWithTrabajoAdded = Persona.builder()
                .id(personId)
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .nacionalidad(nacionalidad)
                .email(email)
                .descripcion(descripcion)
                .imagen(imagen)
                .ocupacion(ocupacion)
                .experienciasLaborales(experienciasLaborales)
                .habilidades(List.of())
                .estudios(List.of())
                .proyectos(List.of())
                .build();

        ArgumentCaptor<TrabajoDto> trabajoRequestDtoArgumentCaptor = ArgumentCaptor.forClass(TrabajoDto.class);
        ArgumentCaptor<Long> idPersonaRequestArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        given(personaSvc.addTrabajo(
                idPersonaRequestArgumentCaptor.capture(),
                trabajoRequestDtoArgumentCaptor.capture()))
                .willReturn(personaWithTrabajoAdded);

        //when
        //then
        try {
            mockMvc.perform(
                            post(API_PERSONA_BASE_URL + "/add/{id}/trabajos/", personId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(trabajoDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("id").value(personId))
                    .andExpect(jsonPath("nombres").value(nombres))
                    .andExpect(jsonPath("apellidos").value(apellidos))
                    .andExpect(jsonPath("fechaNacimiento").value(fechaNacimiento.toString()))
                    .andExpect(jsonPath("nacionalidad").value(nacionalidad.toString()))
                    .andExpect(jsonPath("email").value(email))
                    .andExpect(jsonPath("descripcion").value(descripcion))
                    .andExpect(jsonPath("ocupacion").value(ocupacion))
                    .andExpect(jsonPath("imagen").value(imagen))
                    .andExpect(jsonPath("ocupacion").value(ocupacion))
                    .andExpect(jsonPath("experienciasLaborales", hasSize(1)))
                    .andExpect(jsonPath("$.experienciasLaborales[:1].id").value((int) idTrabajo))
                    .andExpect(jsonPath("$.experienciasLaborales[:1].empresa").value(empresa))
                    .andExpect(jsonPath("$.experienciasLaborales[:1].cargo").value(cargo))
                    .andExpect(jsonPath("$.experienciasLaborales[:1].lugar").value(lugar))
                    .andExpect(jsonPath("$.experienciasLaborales[:1].desde").value(desde.toString()))
                    .andExpect(jsonPath("$.experienciasLaborales[:1].hasta").value(hasta.toString()));
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        final Long idPersonaCapturedRequestValue = idPersonaRequestArgumentCaptor.getValue();
        Assertions.assertThat(idPersonaCapturedRequestValue).isEqualTo(personId);

        final TrabajoDto trabajoDtoCapturedRequestValue = trabajoRequestDtoArgumentCaptor.getValue();
        Assertions.assertThat(trabajoDtoCapturedRequestValue.getCargo()).isEqualTo(cargo);
        Assertions.assertThat(trabajoDtoCapturedRequestValue.getEmpresa()).isEqualTo(empresa);
        Assertions.assertThat(trabajoDtoCapturedRequestValue.getDesde()).isEqualTo(desde);
        Assertions.assertThat(trabajoDtoCapturedRequestValue.getHasta()).isEqualTo(hasta);
        Assertions.assertThat(trabajoDtoCapturedRequestValue.getLugar()).isEqualTo(lugar);

        Mockito.verify(personaSvc, times(1)).addTrabajo(Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(personaSvc);
    }

    @DisplayName("Should return a 403 status code when user is unauthorized and shouldn't add trabajo")
    @Test
    void addTrabajo_WhenUnauthorized_ShouldNotAddTrabajoReturn403() {
        //given
        final Long personIdToAddTrabajo = 1L;
        final var trabajoToAddDto = new TrabajoDto(
                "Carrefour",
                "Tester",
                "Rio Grande",
                LocalDate.of(2010, 1, 1),
                LocalDate.of(2012, 1, 1)
        );

        //when
        //then
        try {
            mockMvc.perform(
                            post(API_PERSONA_BASE_URL + "/add/{id}/trabajos/", personIdToAddTrabajo)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(trabajoToAddDto)))
                    .andExpect(status().isForbidden());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        Mockito.verify(personaSvc, Mockito.never()).addTrabajo(Mockito.any(), Mockito.any());
    }

    @DisplayName("Should return 404 when persona id is invalid and shouldn't add trabajo")
    @WithMockUser()
    @Test
    void addTrabajo_WhenPersonaDoesNotExist_ShouldNotAddTrabajoReturn404() {
        //given
        final Long personIdToAddTrabajo = 1L;
        final String ERROR_MSG = String.format("Persona id %d no encontrada.", personIdToAddTrabajo);

        final var trabajoToAddDto = new TrabajoDto(
                "Carrefour",
                "Tester",
                "Rio Grande",
                LocalDate.of(2010, 1, 1),
                LocalDate.of(2012, 1, 1)
        );

        given(personaSvc.addTrabajo(Mockito.anyLong(), Mockito.any(TrabajoDto.class)))
                .willThrow(new PersonaNotFoundException(personIdToAddTrabajo));

        //when
        //then
        try {
            mockMvc.perform(
                            post(API_PERSONA_BASE_URL + "/add/{id}/trabajos/", personIdToAddTrabajo)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(trabajoToAddDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(PersonaNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG));

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return 200 and update the trabajo when the persona exists and trabajo exist")
    @WithMockUser
    @Test
    void updateTrabajo_WhenTrabajoIsUpdated_ShouldReturn200UpdatedPersona() {
        //given
        final long personId = 1L;
        final long trabajoId = 2L;
        final var empresa = "Carrefour";
        final var cargo = "Tester";
        final var lugar = "Rio Grande";
        final var desde = LocalDate.of(2010, 1, 1);
        final var hasta = LocalDate.of(2012, 1, 1);
        final var trabajoDto = new TrabajoDto(
                empresa,
                cargo,
                lugar,
                desde,
                hasta
        );
        Trabajo trabajoUpdated = Trabajo.builder()
                .id(trabajoId)
                .empresa(empresa)
                .cargo(cargo)
                .desde(desde)
                .hasta(hasta)
                .lugar(lugar)
                .build();

        final String nombres = "Jere";
        final String apellidos = "Calvet";
        final LocalDate fechaNacimiento = LocalDate.now();
        final Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        final String email = "test@test.com";
        final String descripcion = "descripción";
        final String imagen = "test.jpg";
        final String ocupacion = "Ocupación";
        var personaWithUpdatedTrabajo = Persona.builder()
                .id(personId)
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .nacionalidad(nacionalidad)
                .email(email)
                .descripcion(descripcion)
                .imagen(imagen)
                .ocupacion(ocupacion)
                .habilidades(List.of())
                .experienciasLaborales(
                        Stream.of(trabajoUpdated)
                        .collect(Collectors.toList())
                )
                .estudios(List.of())
                .proyectos(List.of())
                .build();

        ArgumentCaptor<Long> idPersonaArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> idTrabajoArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<TrabajoDto> trabajoDtoArgumentCaptor = ArgumentCaptor.forClass(TrabajoDto.class);

        BDDMockito.given(
                personaSvc.updateTrabajo(
                        idPersonaArgumentCaptor.capture(),
                        idTrabajoArgumentCaptor.capture(),
                        trabajoDtoArgumentCaptor.capture())
        ).willReturn(
                personaWithUpdatedTrabajo
        );

        //when
        //then
        try {
            mockMvc.perform(
                            put(API_PERSONA_BASE_URL + "/update/{id}/trabajos/{idTrabajo}", personId, trabajoId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(trabajoDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("id").value(personId))
                    .andExpect(jsonPath("nombres").value(nombres))
                    .andExpect(jsonPath("apellidos").value(apellidos))
                    .andExpect(jsonPath("fechaNacimiento").value(fechaNacimiento.format(DateTimeFormatter.ISO_DATE)))
                    .andExpect(jsonPath("nacionalidad").value(nacionalidad.toString()))
                    .andExpect(jsonPath("descripcion").value(descripcion))
                    .andExpect(jsonPath("imagen").value(imagen))
                    .andExpect(jsonPath("ocupacion").value(ocupacion))
                    .andExpect(jsonPath("email").value(email))
                    .andExpect(jsonPath("experienciasLaborales", hasSize(1)))
                    .andExpect(jsonPath("$.experienciasLaborales[:1].id").value((int) trabajoId))
                    .andExpect(jsonPath("$.experienciasLaborales[:1].empresa").value(empresa))
                    .andExpect(jsonPath("$.experienciasLaborales[:1].cargo").value(cargo))
                    .andExpect(jsonPath("$.experienciasLaborales[:1].lugar").value(lugar))
                    .andExpect(jsonPath("$.experienciasLaborales[:1].desde").value(desde.toString()))
                    .andExpect(jsonPath("$.experienciasLaborales[:1].hasta").value(hasta.toString()));
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        final Long idPersonaCapturedRequestValue = idPersonaArgumentCaptor.getValue();
        Assertions.assertThat(idPersonaCapturedRequestValue).isEqualTo(personId);

        final Long idTrabajoCapturedRequestValue = idTrabajoArgumentCaptor.getValue();
        Assertions.assertThat(idTrabajoCapturedRequestValue).isEqualTo(trabajoId);

        final TrabajoDto dtoCapturedRequestValue = trabajoDtoArgumentCaptor.getValue();
        Assertions.assertThat(dtoCapturedRequestValue.getEmpresa()).isEqualTo(empresa);
        Assertions.assertThat(dtoCapturedRequestValue.getLugar()).isEqualTo(lugar);
        Assertions.assertThat(dtoCapturedRequestValue.getCargo()).isEqualTo(cargo);
        Assertions.assertThat(dtoCapturedRequestValue.getDesde()).isEqualTo(desde);
        Assertions.assertThat(dtoCapturedRequestValue.getHasta()).isEqualTo(hasta);

        Mockito.verify(personaSvc, times(1)).updateTrabajo(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(personaSvc);
    }

    @DisplayName("Should return a 403 status code when user is unauthorized and shouldn't update trabajo")
    @Test
    void updateTrabajo_WhenUnauthorized_ShouldNotUpdateReturn403() {
        //given
        final Long personIdToUpdate = 1L;
        final Long trabajoIdToUpdate = 2L;
        final var trabajoDto = new TrabajoDto(
                "Carrefour",
                "Tester",
                "Rio Grande",
                LocalDate.of(2010, 1, 1),
                LocalDate.of(2012, 1, 1)
        );

        //when
        //then
        try {
            mockMvc.perform(
                            put(API_PERSONA_BASE_URL + "/update/{id}/trabajos/{idTrabajo}", personIdToUpdate, trabajoIdToUpdate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(trabajoDto)))
                    .andExpect(status().isForbidden());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        Mockito.verify(personaSvc, Mockito.never()).updateTrabajo(Mockito.anyLong(), Mockito.any(), Mockito.any());
    }

    @DisplayName("Should return 404 when persona id is invalid and shouldn't update trabajo")
    @WithMockUser()
    @Test
    void updateTrabajo_WhenNonExistentPersonaId_ShouldNotUpdateReturn404() {
        //given
        final Long nonExistentPersonaId = 3L;
        final Long trabajoIdToUpdate = 2L;
        final var trabajoDto = new TrabajoDto(
                "Carrefour",
                "Tester",
                "Rio Grande",
                LocalDate.of(2010, 1, 1),
                LocalDate.of(2012, 1, 1)
        );
        final String ERROR_MSG = String.format("Persona id %d no encontrada.", nonExistentPersonaId);

        BDDMockito.willThrow(new PersonaNotFoundException(nonExistentPersonaId))
                .given(personaSvc).updateTrabajo(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(TrabajoDto.class));

        //when
        //then
        try {
            mockMvc.perform(
                            put(API_PERSONA_BASE_URL + "/update/{id}/trabajos/{idTrabajo}", nonExistentPersonaId, trabajoIdToUpdate)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(trabajoDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(PersonaNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG));

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return 404 when trabajo id is invalid and shouldn't update trabajo")
    @WithMockUser()
    @Test
    void updateTrabajo_WhenNonExistentTrabajoId_ShouldNotUpdateReturn404() {
        //given
        final Long personaId = 3L;
        final Long nonExistentTrabajoId = 2L;
        final var trabajoDto = new TrabajoDto(
                "Carrefour",
                "Tester",
                "Rio Grande",
                LocalDate.of(2010, 1, 1),
                LocalDate.of(2012, 1, 1)
        );
        final String ERROR_MSG = String.format("Trabajo id %d no encontrado.", nonExistentTrabajoId);

        BDDMockito.willThrow(new TrabajoNotFoundException(nonExistentTrabajoId))
                .given(personaSvc).updateTrabajo(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(TrabajoDto.class));

        //when
        //then
        try {
            mockMvc.perform(
                            put(API_PERSONA_BASE_URL + "/update/{id}/trabajos/{idTrabajo}", personaId, nonExistentTrabajoId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(trabajoDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(TrabajoNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG));

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }
}