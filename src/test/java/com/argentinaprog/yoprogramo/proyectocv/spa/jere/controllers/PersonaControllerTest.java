package com.argentinaprog.yoprogramo.proyectocv.spa.jere.controllers;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions.PersonaAlreadyExistsException;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions.PersonaNotFoundException;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.Nacionalidades;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.Persona;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.dto.PersonaDto;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
            e.printStackTrace();
        }
    }

    @DisplayName("Should return 404 when user is authorized and the user id is invalid")
    @WithMockUser
    @Test
    void getPersonaById_WhenNonExistentIdAndUserAuthorized_ShouldNotGetPersonaReturn404() {
        //given
        final Long nonExistenId = 3L;
        final String ERROR_MSG = String.format("Persona id %d no encontrada.", nonExistenId);
        given(personaSvc.getPersona(nonExistenId))
                .willThrow(new PersonaNotFoundException(nonExistenId));

        //when
        //then
        try {
            mockMvc.perform(
                            get(API_PERSONA_BASE_URL + "/find/{id}", nonExistenId)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(PersonaNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG));

        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    @DisplayName("Should return 404 when user is unauthenticated and the user id is invalid")
    @Test
    void getPersonaById_WhenNonExistentIdAndUserUnauthenticated_ShouldNotGetPersonaReturn404() {
        //given
        final Long nonExistenId = 3L;
        final String ERROR_MSG = String.format("Persona id %d no encontrada.", nonExistenId);
        given(personaSvc.getPersona(nonExistenId))
                .willThrow(new PersonaNotFoundException(nonExistenId));

        //when
        //then
        try {
            mockMvc.perform(
                            get(API_PERSONA_BASE_URL + "/find/{id}", nonExistenId)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(PersonaNotFoundException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @DisplayName("Should return a 201 status code when current authenticated user added the persona")
    @WithMockUser(username = "username@test.com")
    @Test
    void addPersona_WhenPersona_IsAddedThenReturn201() {
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

    @DisplayName("Should return a 403 status code when user is unauthenticated and shouldn't add persona")
    @Test
    void addPersona_WhenUnauthenticated_ShouldNotAddPersonaReturn403() {
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
            //Assertions.fail("Should not throw an exception");
            e.printStackTrace();
        }

        Mockito.verify(personaSvc, Mockito.never()).addPersona(Mockito.any());
    }

    @Test
    @WithMockUser(username = "username@test.com")
    @DisplayName("Should return a 409 status code when current authenticated user can't add persona because user already has persona")
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

        BDDMockito.given(personaSvc.addPersona(Mockito.any(PersonaDto.class)))
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
}