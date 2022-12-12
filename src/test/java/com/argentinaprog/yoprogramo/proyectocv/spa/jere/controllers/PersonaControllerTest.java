package com.argentinaprog.yoprogramo.proyectocv.spa.jere.controllers;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions.*;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.*;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.dto.*;
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
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
                    .andExpect(jsonPath("id").value(id))
                    .andDo(print());
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
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

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
                    .andExpect(jsonPath("id").value(id))
                    .andDo(print());
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
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

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
                    .andExpect(jsonPath("email").value(email))
                    .andDo(print());

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
                    .andExpect(status().isForbidden())
                    .andDo(print());

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
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());
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
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());
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
                    .andExpect(jsonPath("email").value(email))
                    .andDo(print());

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
                    .andExpect(status().isForbidden())
                    .andDo(print());

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
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

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
                    .andExpect(status().isNoContent())
                    .andDo(print());
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
                    .andExpect(status().isForbidden())
                    .andDo(print());
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

        willThrow(new PersonaNotFoundException(nonExistentId))
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
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

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
                    .andExpect(jsonPath("$[1].apellidos").value(persona2.getApellidos()))
                    .andDo(print());

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
                    .andExpect(jsonPath("$[1].apellidos").value(persona2.getApellidos()))
                    .andDo(print());

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
                    .andExpect(jsonPath("nacionalidad").value(nacionalidad.toString()))
                    .andDo(print());

        } catch (Exception e) {
            Assertions.fail("Should not throw any exception");
        }
    }

    @DisplayName("Should return 202 when current user can't be found because is unauthorized or is not in DB")
    @Test
    void currentPersona_WhenUserIsUnauthorizedOrCanNotBeFound_ShouldReturn202() {
        //given
        final String ERROR_MSG = "Usuario no encontrado.";

        willThrow(new UsuarioNotFoundException())
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
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

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

        willThrow(new PersonaNotFoundException(username))
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
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

        } catch (Exception e) {
            Assertions.fail("Should not throw any exception");
        }
    }

    // ------------------- Trabajos -----------------------------

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
                    .andExpect(jsonPath("$.experienciasLaborales[:1].hasta").value(hasta.toString()))
                    .andDo(print());
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
                    .andExpect(status().isForbidden())
                    .andDo(print());
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
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

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
                    .andExpect(jsonPath("$.experienciasLaborales[:1].hasta").value(hasta.toString()))
                    .andDo(print());
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
                    .andExpect(status().isForbidden())
                    .andDo(print());
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

        willThrow(new PersonaNotFoundException(nonExistentPersonaId))
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
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

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

        willThrow(new TrabajoNotFoundException(nonExistentTrabajoId))
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
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return a 204 status code when current authorized user deleted a trabajo")
    @WithMockUser(username = "username@test.com")
    @Test
    void deleteTrabajo_WhenTrabajoIsDeleted_ShouldReturn204() {
        //given
        final Long personaId = 8L;
        final Long trabajoId = 2L;

        ArgumentCaptor<Long> idPersonaArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> idTrabajoArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        Mockito.doNothing()
                .when(personaSvc)
                .removeTrabajo(idPersonaArgumentCaptor.capture(), idTrabajoArgumentCaptor.capture());

        //when
        //then
        try {
            mockMvc.perform(
                            delete(API_PERSONA_BASE_URL + "/remove/{id}/trabajos/{idTrabajo}", personaId, trabajoId))
                    .andExpect(status().isNoContent())
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        final Long idPersonaCapturedValue = idPersonaArgumentCaptor.getValue();
        Assertions.assertThat(idPersonaCapturedValue).isEqualTo(personaId);
        final Long idTrabajoCapturedValue = idTrabajoArgumentCaptor.getValue();
        Assertions.assertThat(idTrabajoCapturedValue).isEqualTo(trabajoId);

        Mockito.verify(personaSvc, Mockito.times(1)).removeTrabajo(personaId, trabajoId);
    }

    @DisplayName("Should return a 403 status code when user is unauthorized and shouldn't delete trabajo")
    @Test
    void deleteTrabajo_WhenUnauthorized_ShouldNotDeleteTrabajoReturn403() {
        //given
        final Long personaId = 8L;
        final Long trabajoId = 2L;

        //when
        //then
        try {
            mockMvc.perform(
                            delete(API_PERSONA_BASE_URL + "/remove/{id}/trabajos/{idTrabajo}", personaId, trabajoId))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        Mockito.verify(personaSvc, Mockito.never()).removeTrabajo(Mockito.anyLong(), Mockito.anyLong());
    }

    @DisplayName("Should return 404 when persona id is invalid and shouldn't delete trabajo")
    @WithMockUser()
    @Test
    void deleteTrabajo_WhenNonExistentPersonaId_ShouldNotDeleteReturn404() {
        //given
        final Long nonExistentPersonaId = 3L;
        final Long trabajoIdToDelete = 2L;
        final String ERROR_MSG = String.format("Persona id %d no encontrada.", nonExistentPersonaId);

        willThrow(new PersonaNotFoundException(nonExistentPersonaId))
                .given(personaSvc).removeTrabajo(Mockito.anyLong(), Mockito.anyLong());

        //when
        //then
        try {
            mockMvc.perform(
                            delete(API_PERSONA_BASE_URL + "/remove/{id}/trabajos/{idTrabajo}", nonExistentPersonaId, trabajoIdToDelete))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(PersonaNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return 404 when trabajo id is invalid and shouldn't delete trabajo")
    @WithMockUser()
    @Test
    void deleteTrabajo_WhenNonExistentTrabajoId_ShouldNotDeleteReturn404() {
        //given
        final Long personaId = 3L;
        final Long nonExistentTrabajoId = 2L;
        final String ERROR_MSG = String.format("Trabajo id %d no encontrado.", nonExistentTrabajoId);

        willThrow(new TrabajoNotFoundException(nonExistentTrabajoId))
                .given(personaSvc).removeTrabajo(Mockito.anyLong(), Mockito.anyLong());

        //when
        //then
        try {
            mockMvc.perform(
                            delete(API_PERSONA_BASE_URL + "/remove/{id}/trabajos/{idTrabajo}", personaId, nonExistentTrabajoId))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(TrabajoNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    // ------------------- Educación -----------------------------

    @DisplayName("Should return 201 and add the estudio when the persona exists")
    @WithMockUser()
    @Test
    void addEstudio_WhenPersonaExists_ShouldReturn201UpdatedPersona() {
        //given
        final var idEducacion = 2L;
        final var lugar = "Rio Grande";
        final var progreso = ProgresoEducacion.CURSANDO;
        final var titulo = "Secundario Tecnico";
        final var institucion = "Escuela N° 15";
        final var educacionDto = new EducacionDto(
                institucion,
                titulo,
                lugar,
                progreso
        );
        final var educacionAdded = Educacion.builder()
                .id(idEducacion)
                .lugar(lugar)
                .estado(progreso)
                .titulo(titulo)
                .institucion(institucion)
                .build();
        List<Educacion> estudios = Stream.of(educacionAdded)
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
                .experienciasLaborales(List.of())
                .habilidades(List.of())
                .estudios(estudios)
                .proyectos(List.of())
                .build();

        ArgumentCaptor<EducacionDto> educacionRequestDtoArgumentCaptor = ArgumentCaptor.forClass(EducacionDto.class);
        ArgumentCaptor<Long> idPersonaRequestArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        given(personaSvc.addEstudio(
                idPersonaRequestArgumentCaptor.capture(),
                educacionRequestDtoArgumentCaptor.capture()))
                .willReturn(personaWithTrabajoAdded);

        //when
        //then
        try {
            mockMvc.perform(
                            post(API_PERSONA_BASE_URL + "/add/{id}/estudios/", personId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(educacionDto)))
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
                    .andExpect(jsonPath("estudios", hasSize(1)))
                    .andExpect(jsonPath("$.estudios[:1].id").value((int) idEducacion))
                    .andExpect(jsonPath("$.estudios[:1].institucion").value(institucion))
                    .andExpect(jsonPath("$.estudios[:1].titulo").value(titulo))
                    .andExpect(jsonPath("$.estudios[:1].lugar").value(lugar))
                    .andExpect(jsonPath("$.estudios[:1].estado").value(progreso.toString()))
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        final Long idPersonaCapturedRequestValue = idPersonaRequestArgumentCaptor.getValue();
        Assertions.assertThat(idPersonaCapturedRequestValue).isEqualTo(personId);

        final EducacionDto educacionDtoCapturedRequestValue = educacionRequestDtoArgumentCaptor.getValue();
        Assertions.assertThat(educacionDtoCapturedRequestValue.getEstado()).isEqualTo(progreso);
        Assertions.assertThat(educacionDtoCapturedRequestValue.getLugar()).isEqualTo(lugar);
        Assertions.assertThat(educacionDtoCapturedRequestValue.getInstitucion()).isEqualTo(institucion);
        Assertions.assertThat(educacionDtoCapturedRequestValue.getTitulo()).isEqualTo(titulo);

        Mockito.verify(personaSvc, times(1)).addEstudio(Mockito.anyLong(), Mockito.any(EducacionDto.class));
        Mockito.verifyNoMoreInteractions(personaSvc);
    }

    @DisplayName("Should return a 403 status code when user is unauthorized and shouldn't add estudio")
    @Test
    void addEstudio_WhenUnauthorized_ShouldNotAddEstudioReturn403() {
        //given
        final Long personIdToAddEducacion = 1L;
        final var educacionToAddDto = new EducacionDto(
                "Escuela N° 15",
                "Secundario Tecnico",
                "Rio Grande",
                ProgresoEducacion.CURSANDO
        );

        //when
        //then
        try {
            mockMvc.perform(
                            post(API_PERSONA_BASE_URL + "/add/{id}/estudios/", personIdToAddEducacion)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(educacionToAddDto)))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        Mockito.verify(personaSvc, Mockito.never()).addEstudio(Mockito.any(), Mockito.any());
    }

    @DisplayName("Should return 404 when persona id is invalid and shouldn't add estudio")
    @WithMockUser()
    @Test
    void addEstudio_WhenPersonaDoesNotExist_ShouldNotAddEstudioReturn404() {
        //given
        final Long personIdToAddEducacion = 1L;
        final String ERROR_MSG = String.format("Persona id %d no encontrada.", personIdToAddEducacion);

        final var educacionToAddDto = new EducacionDto(
                "Escuela N° 15",
                "Secundario Tecnico",
                "Rio Grande",
                ProgresoEducacion.CURSANDO
        );

        given(personaSvc.addEstudio(Mockito.anyLong(), Mockito.any(EducacionDto.class)))
                .willThrow(new PersonaNotFoundException(personIdToAddEducacion));

        //when
        //then
        try {
            mockMvc.perform(
                            post(API_PERSONA_BASE_URL + "/add/{id}/estudios/", personIdToAddEducacion)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(educacionToAddDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(PersonaNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return 200 and update the estudio when the persona exists and estudio exist")
    @WithMockUser
    @Test
    void updateEstudio_WhenEstudioIsUpdated_ShouldReturn200UpdatedPersona() {
        //given
        final var educacionId = 2L;
        final var lugar = "Rio Grande";
        final var progreso = ProgresoEducacion.CURSANDO;
        final var titulo = "Secundario Tecnico";
        final var institucion = "Escuela N° 15";
        final var educacionDto = new EducacionDto(
                institucion,
                titulo,
                lugar,
                progreso
        );
        final var educacionUpdated = Educacion.builder()
                .id(educacionId)
                .lugar(lugar)
                .estado(progreso)
                .titulo(titulo)
                .institucion(institucion)
                .build();

        final long personId = 1L;
        final String nombres = "Jere";
        final String apellidos = "Calvet";
        final LocalDate fechaNacimiento = LocalDate.now();
        final Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        final String email = "test@test.com";
        final String descripcion = "descripción";
        final String imagen = "test.jpg";
        final String ocupacion = "Ocupación";
        var personaWithUpdatedEducacion = Persona.builder()
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
                .estudios(
                        Stream.of(educacionUpdated)
                                .collect(Collectors.toList()))
                .proyectos(List.of())
                .build();

        ArgumentCaptor<Long> idPersonaArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> idTrabajoArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<EducacionDto> educacionDtoArgumentCaptor = ArgumentCaptor.forClass(EducacionDto.class);

        BDDMockito.given(
                personaSvc.updateEstudio(
                        idPersonaArgumentCaptor.capture(),
                        idTrabajoArgumentCaptor.capture(),
                        educacionDtoArgumentCaptor.capture())
        ).willReturn(
                personaWithUpdatedEducacion
        );

        //when
        //then
        try {
            mockMvc.perform(
                            put(API_PERSONA_BASE_URL + "/update/{id}/estudios/{idEstudio}", personId, educacionId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(educacionDto)))
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
                    .andExpect(jsonPath("estudios", hasSize(1)))
                    .andExpect(jsonPath("$.estudios[:1].id").value((int) educacionId))
                    .andExpect(jsonPath("$.estudios[:1].institucion").value(institucion))
                    .andExpect(jsonPath("$.estudios[:1].titulo").value(titulo))
                    .andExpect(jsonPath("$.estudios[:1].lugar").value(lugar))
                    .andExpect(jsonPath("$.estudios[:1].estado").value(progreso.toString()))
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        final Long idPersonaCapturedRequestValue = idPersonaArgumentCaptor.getValue();
        Assertions.assertThat(idPersonaCapturedRequestValue).isEqualTo(personId);

        final Long idEducacionCapturedRequestValue = idTrabajoArgumentCaptor.getValue();
        Assertions.assertThat(idEducacionCapturedRequestValue).isEqualTo(educacionId);

        final EducacionDto dtoCapturedRequestValue = educacionDtoArgumentCaptor.getValue();
        Assertions.assertThat(dtoCapturedRequestValue.getInstitucion()).isEqualTo(institucion);
        Assertions.assertThat(dtoCapturedRequestValue.getLugar()).isEqualTo(lugar);
        Assertions.assertThat(dtoCapturedRequestValue.getTitulo()).isEqualTo(titulo);
        Assertions.assertThat(dtoCapturedRequestValue.getEstado()).isEqualTo(progreso);

        Mockito.verify(personaSvc, times(1)).updateEstudio(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(EducacionDto.class));
        Mockito.verifyNoMoreInteractions(personaSvc);
    }

    @DisplayName("Should return a 403 status code when user is unauthorized and shouldn't update estudio")
    @Test
    void updateEstudio_WhenUnauthorized_ShouldNotUpdateReturn403() {
        //given
        final Long personIdToUpdate = 1L;
        final Long estudioIdToUpdate = 2L;
        final var educacionDto = new EducacionDto(
                "Escuela N° 15",
                "Secundario Tecnico",
                "Rio Grande",
                ProgresoEducacion.CURSANDO
        );

        //when
        //then
        try {
            mockMvc.perform(
                            put(API_PERSONA_BASE_URL + "/update/{id}/estudios/{idEstudio}", personIdToUpdate, estudioIdToUpdate)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(educacionDto)))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        Mockito.verify(personaSvc, Mockito.never()).updateEstudio(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @DisplayName("Should return 404 when persona id is invalid and shouldn't update estudio")
    @WithMockUser()
    @Test
    void updateEstudio_WhenNonExistentPersonaId_ShouldNotUpdateReturn404() {
        //given
        final Long nonExistentPersonaId = 3L;
        final Long educacionIdToUpdate = 2L;
        final var educacionDto = new EducacionDto(
                "Escuela N° 15",
                "Secundario Tecnico",
                "Rio Grande",
                ProgresoEducacion.CURSANDO
        );
        final String ERROR_MSG = String.format("Persona id %d no encontrada.", nonExistentPersonaId);

        willThrow(new PersonaNotFoundException(nonExistentPersonaId))
                .given(personaSvc).updateEstudio(
                        Mockito.anyLong(),
                        Mockito.anyLong(),
                        Mockito.any(EducacionDto.class)
                );

        //when
        //then
        try {
            mockMvc.perform(
                            put(API_PERSONA_BASE_URL + "/update/{id}/estudios/{idEstudios}", nonExistentPersonaId, educacionIdToUpdate)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(educacionDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(PersonaNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return 404 when estudio id is invalid and shouldn't update estudio")
    @WithMockUser()
    @Test
    void updateEstudio_WhenNonExistentEstudioId_ShouldNotUpdateReturn404() {
        //given
        final Long personaId = 3L;
        final Long nonExistentEducacionId = 2L;
        final var educacionDto = new EducacionDto(
                "Escuela N° 15",
                "Secundario Tecnico",
                "Rio Grande",
                ProgresoEducacion.CURSANDO
        );
        final String ERROR_MSG = String.format("Estudio id %d no encontrado.", nonExistentEducacionId);

        willThrow(new EducacionNotFoundException(nonExistentEducacionId))
                .given(personaSvc).updateEstudio(
                        Mockito.anyLong(),
                        Mockito.anyLong(),
                        Mockito.any(EducacionDto.class)
                );

        //when
        //then
        try {
            mockMvc.perform(
                            put(API_PERSONA_BASE_URL + "/update/{id}/estudios/{idEstudio}", personaId, nonExistentEducacionId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(educacionDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(EducacionNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return a 204 status code when current authorized user deleted a estudio")
    @WithMockUser(username = "username@test.com")
    @Test
    void deleteEstudio_WhenEstudioIsDeleted_ShouldReturn204() {
        //given
        final Long personaId = 8L;
        final Long educacionId = 2L;

        ArgumentCaptor<Long> idPersonaArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> idEducacionArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        Mockito.doNothing()
                .when(personaSvc)
                .removeEstudio(
                        idPersonaArgumentCaptor.capture(),
                        idEducacionArgumentCaptor.capture()
                );

        //when
        //then
        try {
            mockMvc.perform(
                            delete(API_PERSONA_BASE_URL + "/remove/{id}/estudios/{idEducacion}", personaId, educacionId))
                    .andExpect(status().isNoContent())
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        final Long idPersonaCapturedValue = idPersonaArgumentCaptor.getValue();
        Assertions.assertThat(idPersonaCapturedValue).isEqualTo(personaId);
        final Long idEducacionCapturedValue = idEducacionArgumentCaptor.getValue();
        Assertions.assertThat(idEducacionCapturedValue).isEqualTo(educacionId);

        Mockito.verify(personaSvc, Mockito.times(1)).removeEstudio(personaId, educacionId);
    }

    @DisplayName("Should return a 403 status code when user is unauthorized and shouldn't delete estudio")
    @Test
    void deleteEstudio_WhenUnauthorized_ShouldNotDeleteEstudioReturn403() {
        //given
        final Long personaId = 8L;
        final Long educacionId = 2L;

        //when
        //then
        try {
            mockMvc.perform(
                            delete(API_PERSONA_BASE_URL + "/remove/{id}/estudios/{idEducacion}", personaId, educacionId))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        Mockito.verify(personaSvc, Mockito.never()).removeEstudio(Mockito.anyLong(), Mockito.anyLong());
    }

    @DisplayName("Should return 404 when persona id is invalid and shouldn't delete estudio")
    @WithMockUser()
    @Test
    void deleteEstudio_WhenNonExistentPersonaId_ShouldNotDeleteReturn404() {
        //given
        final Long nonExistentPersonaId = 3L;
        final Long educacionIdToDelete = 2L;
        final String ERROR_MSG = String.format("Persona id %d no encontrada.", nonExistentPersonaId);

        willThrow(new PersonaNotFoundException(nonExistentPersonaId))
                .given(personaSvc).removeEstudio(Mockito.anyLong(), Mockito.anyLong());

        //when
        //then
        try {
            mockMvc.perform(
                            delete(API_PERSONA_BASE_URL + "/remove/{id}/estudios/{idEstudio}", nonExistentPersonaId, educacionIdToDelete))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(PersonaNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return 404 when estudio id is invalid and shouldn't delete estudio")
    @WithMockUser()
    @Test
    void deleteEstudio_WhenNonExistentEstudioId_ShouldNotDeleteReturn404() {
        //given
        final Long personaId = 3L;
        final Long nonExistentEducacionId = 2L;
        final String ERROR_MSG = String.format("Estudio id %d no encontrado.", nonExistentEducacionId);

        willThrow(new EducacionNotFoundException(nonExistentEducacionId))
                .given(personaSvc).removeEstudio(Mockito.anyLong(), Mockito.anyLong());

        //when
        //then
        try {
            mockMvc.perform(
                            delete(API_PERSONA_BASE_URL + "/remove/{id}/estudios/{idEstudio}", personaId, nonExistentEducacionId))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(EducacionNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    // ------------------- Proyecto -----------------------------

    @DisplayName("Should return 201 and add the proyecto when the persona exists")
    @WithMockUser()
    @Test
    void addProyecto_WhenPersonaExists_ShouldReturn201UpdatedPersona() {
        //given
        final var proyectoId = 2L;
        final var nombre = "Argentina Programa";
        final var descripcionProyecto = "Un curso de programación";
        final var proyectoDto = new ProyectoDto(
                nombre,
                descripcionProyecto
        );
        final var proyectoAdded = Proyecto.builder()
                .id(proyectoId)
                .nombre(nombre)
                .descripcion(descripcionProyecto)
                .build();
        List<Proyecto> proyectos = Stream.of(proyectoAdded)
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
        var personaWithProyectoAdded = Persona.builder()
                .id(personId)
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .nacionalidad(nacionalidad)
                .email(email)
                .descripcion(descripcion)
                .imagen(imagen)
                .ocupacion(ocupacion)
                .experienciasLaborales(List.of())
                .habilidades(List.of())
                .estudios(List.of())
                .proyectos(proyectos)
                .build();

        ArgumentCaptor<ProyectoDto> proyectoRequestDtoArgumentCaptor = ArgumentCaptor.forClass(ProyectoDto.class);
        ArgumentCaptor<Long> idPersonaRequestArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        given(personaSvc.addProyecto(
                idPersonaRequestArgumentCaptor.capture(),
                proyectoRequestDtoArgumentCaptor.capture()))
                .willReturn(personaWithProyectoAdded);

        //when
        //then
        try {
            mockMvc.perform(
                            post(API_PERSONA_BASE_URL + "/add/{id}/proyectos/", personId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(proyectoDto)))
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
                    .andExpect(jsonPath("proyectos", hasSize(1)))
                    .andExpect(jsonPath("$.proyectos[:1].id").value((int) proyectoId))
                    .andExpect(jsonPath("$.proyectos[:1].nombre").value(nombre))
                    .andExpect(jsonPath("$.proyectos[:1].descripcion").value(descripcionProyecto))
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        final Long idPersonaCapturedRequestValue = idPersonaRequestArgumentCaptor.getValue();
        Assertions.assertThat(idPersonaCapturedRequestValue).isEqualTo(personId);

        final ProyectoDto proyectoDtoCapturedRequestValue = proyectoRequestDtoArgumentCaptor.getValue();
        Assertions.assertThat(proyectoDtoCapturedRequestValue.getNombre()).isEqualTo(nombre);
        Assertions.assertThat(proyectoDtoCapturedRequestValue.getDescripcion()).isEqualTo(descripcionProyecto);

        Mockito.verify(personaSvc, times(1)).addProyecto(Mockito.anyLong(), Mockito.any(ProyectoDto.class));
        Mockito.verifyNoMoreInteractions(personaSvc);
    }

    @DisplayName("Should return a 403 status code when user is unauthorized and shouldn't add proyecto")
    @Test
    void addProyecto_WhenUnauthorized_ShouldNotAddEstudioReturn403() {
        //given
        final Long personIdToAddProyecto = 1L;
        final var proyectoToAddDto = new ProyectoDto(
                "Argentina Programa",
                "Un curso de programación"
        );

        //when
        //then
        try {
            mockMvc.perform(
                            post(API_PERSONA_BASE_URL + "/add/{id}/proyectos/", personIdToAddProyecto)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(proyectoToAddDto)))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        Mockito.verify(personaSvc, Mockito.never()).addProyecto(Mockito.any(), Mockito.any());
    }

    @DisplayName("Should return 404 when persona id is invalid and shouldn't add proyecto")
    @WithMockUser()
    @Test
    void addProyecto_WhenPersonaDoesNotExist_ShouldNotAddProyectoReturn404() {
        //given
        final Long personIdToAddProyecto = 1L;
        final String ERROR_MSG = String.format("Persona id %d no encontrada.", personIdToAddProyecto);

        final var proyectoToAddDto = new ProyectoDto(
                "Argentina Programa",
                "Un curso de programación"
        );

        given(personaSvc.addProyecto(Mockito.anyLong(), Mockito.any(ProyectoDto.class)))
                .willThrow(new PersonaNotFoundException(personIdToAddProyecto));

        //when
        //then
        try {
            mockMvc.perform(
                            post(API_PERSONA_BASE_URL + "/add/{id}/proyectos/", personIdToAddProyecto)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(proyectoToAddDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(PersonaNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return 200 and update the proyecto when the persona exists and proyecto exist")
    @WithMockUser
    @Test
    void updateProyecto_WhenProyectoIsUpdated_ShouldReturn200UpdatedPersona() {
        //given
        final var proyectoId = 2L;
        final var nombre = "Argentina Programa";
        final var descripcionProyecto = "Un curso de programación";
        final var proyectoDto = new ProyectoDto(
                nombre,
                descripcionProyecto
        );
        final var proyectoUpdated = Proyecto.builder()
                .id(proyectoId)
                .nombre(nombre)
                .descripcion(descripcionProyecto)
                .build();

        final long personId = 1L;
        final String nombres = "Jere";
        final String apellidos = "Calvet";
        final LocalDate fechaNacimiento = LocalDate.now();
        final Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        final String email = "test@test.com";
        final String descripcion = "descripción";
        final String imagen = "test.jpg";
        final String ocupacion = "Ocupación";
        var personaWithUpdatedProyecto = Persona.builder()
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
                .proyectos(
                        Stream.of(proyectoUpdated)
                                .collect(Collectors.toList()))
                .build();

        ArgumentCaptor<Long> idPersonaArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> idProyectoArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<ProyectoDto> proyectoDtoArgumentCaptor = ArgumentCaptor.forClass(ProyectoDto.class);

        BDDMockito.given(
                personaSvc.updateProyecto(
                        idPersonaArgumentCaptor.capture(),
                        idProyectoArgumentCaptor.capture(),
                        proyectoDtoArgumentCaptor.capture())
        ).willReturn(
                personaWithUpdatedProyecto
        );

        //when
        //then
        try {
            mockMvc.perform(
                            put(API_PERSONA_BASE_URL + "/update/{id}/proyectos/{idProyecto}", personId, proyectoId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(proyectoDto)))
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
                    .andExpect(jsonPath("proyectos", hasSize(1)))
                    .andExpect(jsonPath("$.proyectos[:1].id").value((int) proyectoId))
                    .andExpect(jsonPath("$.proyectos[:1].nombre").value(nombre))
                    .andExpect(jsonPath("$.proyectos[:1].descripcion").value(descripcionProyecto))
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        final Long idPersonaCapturedRequestValue = idPersonaArgumentCaptor.getValue();
        Assertions.assertThat(idPersonaCapturedRequestValue).isEqualTo(personId);

        final Long idProyectoCapturedRequestValue = idProyectoArgumentCaptor.getValue();
        Assertions.assertThat(idProyectoCapturedRequestValue).isEqualTo(proyectoId);

        final ProyectoDto dtoCapturedRequestValue = proyectoDtoArgumentCaptor.getValue();
        Assertions.assertThat(dtoCapturedRequestValue.getNombre()).isEqualTo(nombre);
        Assertions.assertThat(dtoCapturedRequestValue.getDescripcion()).isEqualTo(descripcionProyecto);

        Mockito.verify(personaSvc, times(1)).updateProyecto(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(ProyectoDto.class));
        Mockito.verifyNoMoreInteractions(personaSvc);
    }

    @DisplayName("Should return a 403 status code when user is unauthorized and shouldn't update proyecto")
    @Test
    void updateProyecto_WhenUnauthorized_ShouldNotUpdateReturn403() {
        //given
        final Long personIdToUpdate = 1L;
        final Long proyectoIdToUpdate = 2L;
        final var proyectoDto = new ProyectoDto(
                "Argentina Programa",
                "Un curso de programación"
        );

        //when
        //then
        try {
            mockMvc.perform(
                            put(API_PERSONA_BASE_URL + "/update/{id}/proyectos/{idProyecto}", personIdToUpdate, proyectoIdToUpdate)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(proyectoDto)))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        Mockito.verify(personaSvc, Mockito.never()).updateProyecto(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @DisplayName("Should return 404 when persona id is invalid and shouldn't update proyecto")
    @WithMockUser()
    @Test
    void updateProyecto_WhenNonExistentPersonaId_ShouldNotUpdateReturn404() {
        //given
        final Long nonExistentPersonaId = 3L;
        final Long proyectoIdToUpdate = 2L;
        final var proyectoDto = new ProyectoDto(
                "Argentina Programa",
                "Un curso de programación"
        );
        final String ERROR_MSG = String.format("Persona id %d no encontrada.", nonExistentPersonaId);

        willThrow(new PersonaNotFoundException(nonExistentPersonaId))
                .given(personaSvc).updateProyecto(
                        Mockito.anyLong(),
                        Mockito.anyLong(),
                        Mockito.any(ProyectoDto.class)
                );

        //when
        //then
        try {
            mockMvc.perform(
                            put(API_PERSONA_BASE_URL + "/update/{id}/proyectos/{idProyecto}", nonExistentPersonaId, proyectoIdToUpdate)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(proyectoDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(PersonaNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return 404 when proyecto id is invalid and shouldn't update proyecto")
    @WithMockUser()
    @Test
    void updateProyecto_WhenNonExistentProyectoId_ShouldNotUpdateReturn404() {
        //given
        final Long personaId = 3L;
        final Long nonExistentProyectoId = 2L;
        final var proyectoDto = new ProyectoDto(
                "Argentina Programa",
                "Un curso de programación"
        );
        final String ERROR_MSG = String.format("Proyecto id %d no encontrado.", nonExistentProyectoId);

        willThrow(new ProyectoNotFoundException(nonExistentProyectoId))
                .given(personaSvc).updateProyecto(
                        Mockito.anyLong(),
                        Mockito.anyLong(),
                        Mockito.any(ProyectoDto.class)
                );

        //when
        //then
        try {
            mockMvc.perform(
                            put(API_PERSONA_BASE_URL + "/update/{id}/proyectos/{idProyecto}", personaId, nonExistentProyectoId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(proyectoDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(ProyectoNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return a 204 status code when current authorized user deleted a proyecto")
    @WithMockUser(username = "username@test.com")
    @Test
    void deleteProyecto_WhenProyectoIsDeleted_ShouldReturn204() {
        //given
        final Long personaId = 8L;
        final Long proyectoId = 2L;

        ArgumentCaptor<Long> idPersonaArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> idProyectoArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        Mockito.doNothing()
                .when(personaSvc)
                .removeProyecto(
                        idPersonaArgumentCaptor.capture(),
                        idProyectoArgumentCaptor.capture()
                );

        //when
        //then
        try {
            mockMvc.perform(
                            delete(API_PERSONA_BASE_URL + "/remove/{id}/proyectos/{idProyecto}", personaId, proyectoId))
                    .andExpect(status().isNoContent())
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        final Long idPersonaCapturedValue = idPersonaArgumentCaptor.getValue();
        Assertions.assertThat(idPersonaCapturedValue).isEqualTo(personaId);
        final Long idProyectoCapturedValue = idProyectoArgumentCaptor.getValue();
        Assertions.assertThat(idProyectoCapturedValue).isEqualTo(proyectoId);

        Mockito.verify(personaSvc, Mockito.times(1)).removeProyecto(personaId, proyectoId);
    }

    @DisplayName("Should return a 403 status code when user is unauthorized and shouldn't delete proyecto")
    @Test
    void deleteProyecto_WhenUnauthorized_ShouldNotDeleteProyectoReturn403() {
        //given
        final Long personaId = 8L;
        final Long proyectoId = 2L;

        //when
        //then
        try {
            mockMvc.perform(
                            delete(API_PERSONA_BASE_URL + "/remove/{id}/proyectos/{idProyecto}", personaId, proyectoId))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        Mockito.verify(personaSvc, Mockito.never()).removeProyecto(Mockito.anyLong(), Mockito.anyLong());
    }

    @DisplayName("Should return 404 when persona id is invalid and shouldn't delete proyecto")
    @WithMockUser()
    @Test
    void deleteProyecto_WhenNonExistentPersonaId_ShouldNotDeleteReturn404() {
        //given
        final Long nonExistentPersonaId = 3L;
        final Long proyectoIdToDelete = 2L;
        final String ERROR_MSG = String.format("Persona id %d no encontrada.", nonExistentPersonaId);

        willThrow(new PersonaNotFoundException(nonExistentPersonaId))
                .given(personaSvc).removeProyecto(Mockito.anyLong(), Mockito.anyLong());

        //when
        //then
        try {
            mockMvc.perform(
                            delete(API_PERSONA_BASE_URL + "/remove/{id}/proyectos/{idProyecto}", nonExistentPersonaId, proyectoIdToDelete))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(PersonaNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return 404 when proyecto id is invalid and shouldn't delete proyecto")
    @WithMockUser()
    @Test
    void deleteProyecto_WhenNonExistentProyectoId_ShouldNotDeleteReturn404() {
        //given
        final Long personaId = 3L;
        final Long nonExistentProyectoId = 2L;
        final String ERROR_MSG = String.format("Proyecto id %d no encontrado.", nonExistentProyectoId);

        willThrow(new ProyectoNotFoundException(nonExistentProyectoId))
                .given(personaSvc).removeProyecto(Mockito.anyLong(), Mockito.anyLong());

        //when
        //then
        try {
            mockMvc.perform(
                            delete(API_PERSONA_BASE_URL + "/remove/{id}/proyectos/{idProyecto}", personaId, nonExistentProyectoId))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(ProyectoNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    // ------------------- Habilidad -----------------------------

    @DisplayName("Should return 201 and add the habilidad when the persona exists")
    @WithMockUser()
    @Test
    void addHabilidad_WhenPersonaExists_ShouldReturn201UpdatedPersona() {
        //given
        final var habilidadId = 2L;
        final var nombre = "Conducir";
        final var descripcionHabilidad = "Carnet de conducir BH1 de autos, camionetas y motos";
        final var nivel = 15;
        final var habilidadDto = new HabilidadDto(
                nombre,
                nivel,
                descripcionHabilidad
        );
        final var habilidadAdded = Habilidad.builder()
                .id(habilidadId)
                .nombre(nombre)
                .nivel(nivel)
                .descripcion(descripcionHabilidad)
                .build();
        List<Habilidad> habilidades = Stream.of(habilidadAdded)
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
        var personaWithHabilidadAdded = Persona.builder()
                .id(personId)
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .nacionalidad(nacionalidad)
                .email(email)
                .descripcion(descripcion)
                .imagen(imagen)
                .ocupacion(ocupacion)
                .experienciasLaborales(List.of())
                .habilidades(habilidades)
                .estudios(List.of())
                .proyectos(List.of())
                .build();

        ArgumentCaptor<HabilidadDto> habilidadRequestDtoArgumentCaptor = ArgumentCaptor.forClass(HabilidadDto.class);
        ArgumentCaptor<Long> idPersonaRequestArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        given(personaSvc.addHabilidad(
                idPersonaRequestArgumentCaptor.capture(),
                habilidadRequestDtoArgumentCaptor.capture()))
                .willReturn(personaWithHabilidadAdded);

        //when
        //then
        try {
            mockMvc.perform(
                            post(API_PERSONA_BASE_URL + "/add/{id}/habilidades/", personId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(habilidadDto)))
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
                    .andExpect(jsonPath("habilidades", hasSize(1)))
                    .andExpect(jsonPath("$.habilidades[:1].id").value((int) habilidadId))
                    .andExpect(jsonPath("$.habilidades[:1].nombre").value(nombre))
                    .andExpect(jsonPath("$.habilidades[:1].nivel").value(nivel))
                    .andExpect(jsonPath("$.habilidades[:1].descripcion").value(descripcionHabilidad))
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        final Long idPersonaCapturedRequestValue = idPersonaRequestArgumentCaptor.getValue();
        Assertions.assertThat(idPersonaCapturedRequestValue).isEqualTo(personId);

        final HabilidadDto habilidadDtoCapturedRequestValue = habilidadRequestDtoArgumentCaptor.getValue();
        Assertions.assertThat(habilidadDtoCapturedRequestValue.getNombre()).isEqualTo(nombre);
        Assertions.assertThat(habilidadDtoCapturedRequestValue.getNivel()).isEqualTo(nivel);
        Assertions.assertThat(habilidadDtoCapturedRequestValue.getDescripcion()).isEqualTo(descripcionHabilidad);

        Mockito.verify(personaSvc, times(1)).addHabilidad(Mockito.anyLong(), Mockito.any(HabilidadDto.class));
        Mockito.verifyNoMoreInteractions(personaSvc);
    }

    @DisplayName("Should return a 403 status code when user is unauthorized and shouldn't add habilidad")
    @Test
    void addHabilidad_WhenUnauthorized_ShouldNotAddHabilidadReturn403() {
        //given
        final Long personIdToAddHabilidad = 1L;
        final var habilidadToAddDto = new HabilidadDto(
                "Conducir",
                15,
                "Carnet de conducir BH1 de autos, camionetas y motos"
        );

        //when
        //then
        try {
            mockMvc.perform(
                            post(API_PERSONA_BASE_URL + "/add/{id}/habilidades/", personIdToAddHabilidad)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(habilidadToAddDto)))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        Mockito.verify(personaSvc, Mockito.never()).addHabilidad(Mockito.any(), Mockito.any());
    }

    @DisplayName("Should return 404 when persona id is invalid and shouldn't add habilidad")
    @WithMockUser()
    @Test
    void addHabilidad_WhenPersonaDoesNotExist_ShouldNotAddHabilidadReturn404() {
        //given
        final Long personIdToAddHabilidad = 1L;
        final String ERROR_MSG = String.format("Persona id %d no encontrada.", personIdToAddHabilidad);

        final var habilidadToAddDto = new HabilidadDto(
                "Conducir",
                15,
                "Carnet de conducir BH1 de autos, camionetas y motos"
        );

        given(personaSvc.addHabilidad(Mockito.anyLong(), Mockito.any(HabilidadDto.class)))
                .willThrow(new PersonaNotFoundException(personIdToAddHabilidad));

        //when
        //then
        try {
            mockMvc.perform(
                            post(API_PERSONA_BASE_URL + "/add/{id}/habilidades/", personIdToAddHabilidad)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(habilidadToAddDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(PersonaNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return 200 and update the habilidad when persona and habilidad exist")
    @WithMockUser
    @Test
    void updateHabilidad_WhenHabilidadIsUpdated_ShouldReturn200UpdatedPersona() {
        //given
        final var habilidadId = 2L;
        final var nombre = "Conducir";
        final var descripcionHabilidad = "Carnet de conducir BH1 de autos, camionetas y motos";
        final var nivel = 15;
        final var habilidadDto = new HabilidadDto(
                nombre,
                nivel,
                descripcionHabilidad
        );
        final var habilidadUpdated = Habilidad.builder()
                .id(habilidadId)
                .nombre(nombre)
                .nivel(nivel)
                .descripcion(descripcionHabilidad)
                .build();

        final long personId = 1L;
        final String nombres = "Jere";
        final String apellidos = "Calvet";
        final LocalDate fechaNacimiento = LocalDate.now();
        final Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        final String email = "test@test.com";
        final String descripcion = "descripción";
        final String imagen = "test.jpg";
        final String ocupacion = "Ocupación";
        var personaWithUpdatedHabilidad = Persona.builder()
                .id(personId)
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .nacionalidad(nacionalidad)
                .email(email)
                .descripcion(descripcion)
                .imagen(imagen)
                .ocupacion(ocupacion)
                .habilidades(
                        Stream.of(habilidadUpdated)
                                .collect(Collectors.toList()))
                .experienciasLaborales(List.of())
                .estudios(List.of())
                .proyectos(List.of())
                .build();

        ArgumentCaptor<Long> idPersonaArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> idHabilidadArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<HabilidadDto> habilidadDtoArgumentCaptor = ArgumentCaptor.forClass(HabilidadDto.class);

        BDDMockito.given(
                personaSvc.updateHabilidad(
                        idPersonaArgumentCaptor.capture(),
                        idHabilidadArgumentCaptor.capture(),
                        habilidadDtoArgumentCaptor.capture())
        ).willReturn(
                personaWithUpdatedHabilidad
        );

        //when
        //then
        try {
            mockMvc.perform(
                            put(API_PERSONA_BASE_URL + "/update/{id}/habilidades/{idHabilidad}", personId, habilidadId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(habilidadDto)))
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
                    .andExpect(jsonPath("habilidades", hasSize(1)))
                    .andExpect(jsonPath("$.habilidades[:1].id").value((int) habilidadId))
                    .andExpect(jsonPath("$.habilidades[:1].nombre").value(nombre))
                    .andExpect(jsonPath("$.habilidades[:1].nivel").value(nivel))
                    .andExpect(jsonPath("$.habilidades[:1].descripcion").value(descripcionHabilidad))
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        final Long idPersonaCapturedRequestValue = idPersonaArgumentCaptor.getValue();
        Assertions.assertThat(idPersonaCapturedRequestValue).isEqualTo(personId);

        final Long idHabilidadCapturedRequestValue = idHabilidadArgumentCaptor.getValue();
        Assertions.assertThat(idHabilidadCapturedRequestValue).isEqualTo(habilidadId);

        final HabilidadDto dtoCapturedRequestValue = habilidadDtoArgumentCaptor.getValue();
        Assertions.assertThat(dtoCapturedRequestValue.getNombre()).isEqualTo(nombre);
        Assertions.assertThat(dtoCapturedRequestValue.getNivel()).isEqualTo(nivel);
        Assertions.assertThat(dtoCapturedRequestValue.getDescripcion()).isEqualTo(descripcionHabilidad);

        Mockito.verify(personaSvc, times(1)).updateHabilidad(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(HabilidadDto.class));
        Mockito.verifyNoMoreInteractions(personaSvc);
    }

    @DisplayName("Should return a 403 status code when user is unauthorized and shouldn't update habilidad")
    @Test
    void updateHabilidad_WhenUnauthorized_ShouldNotUpdateReturn403() {
        //given
        final Long personIdToUpdate = 1L;
        final Long habilidadIdToUpdate = 2L;
        final var habilidadDto = new HabilidadDto(
                "Conducir",
                15,
                "Carnet de conducir BH1 de autos, camionetas y motos"
        );

        //when
        //then
        try {
            mockMvc.perform(
                            put(API_PERSONA_BASE_URL + "/update/{id}/habilidades/{idHabilidad}", personIdToUpdate, habilidadIdToUpdate)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(habilidadDto)))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        Mockito.verify(personaSvc, Mockito.never()).updateHabilidad(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @DisplayName("Should return 404 when persona id is invalid and shouldn't update habilidad")
    @WithMockUser()
    @Test
    void updateHabilidad_WhenNonExistentPersonaId_ShouldNotUpdateReturn404() {
        //given
        final Long nonExistentPersonaId = 3L;
        final Long habilidadIdToUpdate = 2L;
        final var habilidadDto = new HabilidadDto(
                "Conducir",
                15,
                "Carnet de conducir BH1 de autos, camionetas y motos"
        );
        final String ERROR_MSG = String.format("Persona id %d no encontrada.", nonExistentPersonaId);

        willThrow(new PersonaNotFoundException(nonExistentPersonaId))
                .given(personaSvc).updateHabilidad(
                        Mockito.anyLong(),
                        Mockito.anyLong(),
                        Mockito.any(HabilidadDto.class)
                );

        //when
        //then
        try {
            mockMvc.perform(
                            put(API_PERSONA_BASE_URL + "/update/{id}/habilidades/{idHabilidad}", nonExistentPersonaId, habilidadIdToUpdate)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(habilidadDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(PersonaNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return 404 when proyecto id is invalid and shouldn't update habilidad")
    @WithMockUser()
    @Test
    void updateHabilidad_WhenNonExistentHabilidadId_ShouldNotUpdateReturn404() {
        //given
        final Long personaId = 3L;
        final Long nonExistentHabilidadId = 2L;
        final var habilidadDto = new HabilidadDto(
                "Conducir",
                15,
                "Carnet de conducir BH1 de autos, camionetas y motos"
        );
        final String ERROR_MSG = String.format("Habilidad id %d no encontrado.", nonExistentHabilidadId);

        willThrow(new HabilidadNotFoundException(nonExistentHabilidadId))
                .given(personaSvc).updateHabilidad(
                        Mockito.anyLong(),
                        Mockito.anyLong(),
                        Mockito.any(HabilidadDto.class)
                );

        //when
        //then
        try {
            mockMvc.perform(
                            put(API_PERSONA_BASE_URL + "/update/{id}/habilidades/{idHabilidad}", personaId, nonExistentHabilidadId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(habilidadDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(HabilidadNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return a 204 status code when current authorized user deleted an habilidad")
    @WithMockUser(username = "username@test.com")
    @Test
    void deleteHabilidad_WhenHabilidadIsDeleted_ShouldReturn204() {
        //given
        final Long personaId = 8L;
        final Long habilidadId = 2L;

        ArgumentCaptor<Long> idPersonaArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> idHabilidadArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        Mockito.doNothing()
                .when(personaSvc)
                .removeHabilidad(
                        idPersonaArgumentCaptor.capture(),
                        idHabilidadArgumentCaptor.capture()
                );

        //when
        //then
        try {
            mockMvc.perform(
                            delete(API_PERSONA_BASE_URL + "/remove/{id}/habilidades/{idHabilidad}", personaId, habilidadId))
                    .andExpect(status().isNoContent())
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        final Long idPersonaCapturedValue = idPersonaArgumentCaptor.getValue();
        Assertions.assertThat(idPersonaCapturedValue).isEqualTo(personaId);
        final Long idHabilidadCapturedValue = idHabilidadArgumentCaptor.getValue();
        Assertions.assertThat(idHabilidadCapturedValue).isEqualTo(habilidadId);

        Mockito.verify(personaSvc, Mockito.times(1)).removeHabilidad(personaId, habilidadId);
    }

    @DisplayName("Should return a 403 status code when user is unauthorized and shouldn't delete habilidad")
    @Test
    void deleteHabilidad_WhenUnauthorized_ShouldNotDeleteHabilidadReturn403() {
        //given
        final Long personaId = 8L;
        final Long habilidadId = 2L;

        //when
        //then
        try {
            mockMvc.perform(
                            delete(API_PERSONA_BASE_URL + "/remove/{id}/habilidades/{idHabilidad}", personaId, habilidadId))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        Mockito.verify(personaSvc, Mockito.never()).removeHabilidad(Mockito.anyLong(), Mockito.anyLong());
    }

    @DisplayName("Should return 404 when persona id is invalid and shouldn't delete habilidad")
    @WithMockUser()
    @Test
    void deleteHabilidad_WhenNonExistentPersonaId_ShouldNotDeleteReturn404() {
        //given
        final Long nonExistentPersonaId = 3L;
        final Long habilidadIdToDelete = 2L;
        final String ERROR_MSG = String.format("Persona id %d no encontrada.", nonExistentPersonaId);

        willThrow(new PersonaNotFoundException(nonExistentPersonaId))
                .given(personaSvc).removeHabilidad(Mockito.anyLong(), Mockito.anyLong());

        //when
        //then
        try {
            mockMvc.perform(
                            delete(API_PERSONA_BASE_URL + "/remove/{id}/habilidades/{idHabilidad}", nonExistentPersonaId, habilidadIdToDelete))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(PersonaNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @DisplayName("Should return 404 when habilidad id is invalid and shouldn't delete habilidad")
    @WithMockUser()
    @Test
    void deleteHabilidad_WhenNonExistentHabilidadId_ShouldNotDeleteReturn404() {
        //given
        final Long personaId = 3L;
        final Long nonExistentHabilidadId = 2L;
        final String ERROR_MSG = String.format("Habilidad id %d no encontrado.", nonExistentHabilidadId);

        willThrow(new HabilidadNotFoundException(nonExistentHabilidadId))
                .given(personaSvc).removeHabilidad(Mockito.anyLong(), Mockito.anyLong());

        //when
        //then
        try {
            mockMvc.perform(
                            delete(API_PERSONA_BASE_URL + "/remove/{id}/habilidades/{idHabilidad}", personaId, nonExistentHabilidadId))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(HabilidadNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());

        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }
}