package com.argentinaprog.yoprogramo.proyectocv.spa.jere.controllers;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions.EmailAlreadyTakenException;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions.UsuarioNotFoundException;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.Usuario;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.dto.LoginRequestDto;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.security.JwtConfig;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.security.PasswordConfig;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.security.SecurityConfig;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.services.UsuarioService;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthController.class)
@Import(value = {UsuarioService.class, SecurityConfig.class, PasswordConfig.class, JwtConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    private final static String API_AUTH_BASE_URL = "/api/v1/auth";

    @Test
    @DisplayName("Debe crear usuario y devolver 201")
    void postRegister_WhenUserIsCreated_ThenReturn201() {
        //given
        final String nombreUsuario = "jere@test.com";
        final String password = "12345678";

        final var usuarioRegistrado = Usuario.builder()
                .username(nombreUsuario)
                .password(password)
                .build();

        var loginRequestDto = new LoginRequestDto(nombreUsuario, password);
        ArgumentCaptor<LoginRequestDto> loginRequestDtoArgumentCaptor = ArgumentCaptor.forClass(LoginRequestDto.class);
        BDDMockito.given(usuarioService.save(loginRequestDtoArgumentCaptor.capture()))
                .willReturn(usuarioRegistrado);

        //when
        //then
        try {
            mockMvc.perform(
                            MockMvcRequestBuilders.post(API_AUTH_BASE_URL + "/register")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(loginRequestDto)))
                    .andExpect(status().isCreated())
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }

        final LoginRequestDto capturedRequestValue = loginRequestDtoArgumentCaptor.getValue();
        Assertions.assertThat(capturedRequestValue.getUsername()).isEqualTo(nombreUsuario);
        Assertions.assertThat(capturedRequestValue.getPassword()).isEqualTo(password);
    }

    @Test
    @DisplayName("Should return 409 when the email is already taken")
    void register_WhenEmailIsAlreadyTaken_ShouldNotRegisterReturn409() {
        //given
        final String usernameAlreadyTaken = "jere@test.com";
        final String password = "12345678";
        var loginRequestDto = new LoginRequestDto(
                usernameAlreadyTaken,
                password
        );

        final String ERROR_MSG = String.format("Ya existe un usuario con este email %s.", usernameAlreadyTaken);

        willThrow(new EmailAlreadyTakenException(usernameAlreadyTaken))
                .given(usuarioService).save(Mockito.any(loginRequestDto.getClass()));

        //when
        //then
        try {
            mockMvc.perform(
                            MockMvcRequestBuilders.post(API_AUTH_BASE_URL + "/register")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(loginRequestDto)))
                    .andExpect(status().isConflict())
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException())
                            .isInstanceOf(EmailAlreadyTakenException.class))
                    .andExpect(result -> Assertions.assertThat(result.getResolvedException().getMessage())
                            .isEqualTo(ERROR_MSG))
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @Test
    @DisplayName("When user authenticated should return 200 the current user")
    @WithMockUser(username = "jere@test.com")
    void currentUser_ShouldReturn200CurrentUser() {
        //given
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String username = authentication.getName();
        final var currentLoggedUser = Usuario.builder()
                .username(username)
                .build();
        given(usuarioService.getCurrentUser())
                .willReturn(currentLoggedUser);
        //when
        //then
        try {
            mockMvc.perform(
                            get(API_AUTH_BASE_URL + "/current"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("username").value(username))
                    .andDo(print());
        } catch (Exception e) {
            Assertions.fail("Should not throw an exception");
        }
    }

    @Test
    @DisplayName("When anonymous user should return 202 an empty answer")
    void currentUser_WhenUserNotFound_ShouldReturn202() {
        //given
        final String ERROR_MSG = "Usuario no encontrado.";
        given(usuarioService.getCurrentUser())
                .willThrow(new UsuarioNotFoundException());

        //when
        //then
        try {
            mockMvc.perform(
                            get(API_AUTH_BASE_URL + "/current"))
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
}