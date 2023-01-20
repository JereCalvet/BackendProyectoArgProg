package com.argentinaprog.yoprogramo.proyectocv.spa.jere.services;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions.EmailAlreadyTakenException;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions.UsuarioNotFoundException;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.Usuario;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.dto.LoginRequestDto;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.repositories.UsuarioRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepo;

    @Mock
    private ModelMapper mapper;

    @Mock
    private PasswordEncoder bcryptEncoder;

    private UsuarioService underTest;

     @BeforeEach
     void setUp() {
         underTest = new UsuarioService(usuarioRepo, bcryptEncoder, mapper);
     }

    @Test
    void loadUserByUsername_shouldReturnUser() {
        //given
        String emailJere = "jere@test.com";
        Usuario usuarioJere = Usuario.builder()
                .username(emailJere)
                .build();

        BDDMockito.given(usuarioRepo.findByUsername(Mockito.anyString()))
                .willReturn(Optional.of(usuarioJere));

        //when
        final UserDetails usuarioByUsernameEncontrado = underTest.loadUserByUsername(emailJere);

        //then
        ArgumentCaptor<String> emailArgumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(usuarioRepo).findByUsername(emailArgumentCaptor.capture());
        Assertions.assertThat(usuarioByUsernameEncontrado).isEqualTo(usuarioJere);
        Assertions.assertThat(usuarioByUsernameEncontrado.getUsername()).isEqualTo(emailJere);

        final String emailValue = emailArgumentCaptor.getValue();
        Assertions.assertThat(emailValue).isEqualTo(emailJere);
    }

    @Test
    void loadUserByUsername_whenUserNotFound_shouldThrowUsernameNotFoundException() {
        //given
        String emailJere = "jere@test.com";
        final String ERROR_MSG = "Usuario no encontrado";

        BDDMockito.given(usuarioRepo.findByUsername(Mockito.anyString()))
                .willReturn(Optional.empty());

        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.loadUserByUsername(emailJere))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining(ERROR_MSG);
     }

    @Test
    void save_shouldSaveUser() {
        //given
        final String username = "jere@test.com";
        final String password = "testPassword";
        final String encrypedPassword = "veryEncryptedPassword";
        final LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);
        final Usuario usuarioJere = Usuario.builder()
                .username(username)
                .password(password)
                .build();

        BDDMockito.given(mapper.map(Mockito.any(), Mockito.any())).willReturn(usuarioJere);
        BDDMockito.given(bcryptEncoder.encode(Mockito.anyString())).willReturn(encrypedPassword);
        BDDMockito.given(usuarioRepo.findByUsername(Mockito.anyString())).willReturn(Optional.empty());

        //when
        underTest.save(loginRequest);

        //then
        ArgumentCaptor<Usuario> usuarioArgumentCaptor = ArgumentCaptor.forClass(Usuario.class);
        Mockito.verify(usuarioRepo).save(usuarioArgumentCaptor.capture());

        final Usuario usuarioCaptured = usuarioArgumentCaptor.getValue();
        Assertions.assertThat(usuarioCaptured.getUsername()).isEqualTo(loginRequest.getUsername());
        Assertions.assertThat(usuarioCaptured.getPassword()).isEqualTo(encrypedPassword);
        Assertions.assertThat(usuarioCaptured.getPassword()).isNotEqualTo(password);
    }

    @Test()
    void save_WhenEmailTaken_ShouldThrowEmailAlreadyTakenException() {
        //given
        final String username = "jere@test.com";
        Usuario usuarioYaRegistrado = Usuario.builder().username(username).build();

        final String ERROR_MSG = String.format("Ya existe un usuario con este email %s.", username);
        final LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setUsername(username);

        BDDMockito.given(usuarioRepo.findByUsername(Mockito.anyString()))
                .willReturn(Optional.of(usuarioYaRegistrado));

        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.save(loginRequest))
                .isInstanceOf(EmailAlreadyTakenException.class)
                .hasMessageContaining(ERROR_MSG);

        Mockito.verify(usuarioRepo, Mockito.never()).save(Mockito.any());
    }

    @Test
    void getCurrentUser_WhenUserLogged_shouldReturnUser() {
        //given
        String emailJere = "jere@test.com";
        Usuario usuarioJere = Usuario.builder()
                .username(emailJere)
                .build();

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Authentication authentication = Mockito.mock(Authentication.class);

        BDDMockito.given(securityContext.getAuthentication()).willReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        BDDMockito.given(authentication.getName()).willReturn(emailJere);
        BDDMockito.given(usuarioRepo.findByUsername(Mockito.anyString()))
                .willReturn(Optional.of(usuarioJere));

        //when
        final Usuario usuarioLogeado = underTest.getCurrentUser();

        //then
        Assertions.assertThat(usuarioLogeado).isEqualTo(usuarioJere);
        Assertions.assertThat(usuarioLogeado.getUsername()).isEqualTo(emailJere);

        ArgumentCaptor<String> emailArgumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(usuarioRepo).findByUsername(emailArgumentCaptor.capture());
        final String emailValue = emailArgumentCaptor.getValue();
        Assertions.assertThat(emailValue).isEqualTo(emailJere);
    }

    @Test
    void getCurrentUser_WhenUserUnauthenticated_shouldThrowUsuarioNotFoundException() {
        //given
        final String ERROR_MSG = "Usuario no encontrado.";
        final String ANONYMOUS_USER_USERNAME = "anonymousUser";

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        AnonymousAuthenticationToken anonymousAuthenticationToken = Mockito.mock(AnonymousAuthenticationToken.class);

        BDDMockito.given(securityContext.getAuthentication()).willReturn(anonymousAuthenticationToken);
        SecurityContextHolder.setContext(securityContext);
        BDDMockito.given(anonymousAuthenticationToken.getName()).willReturn(ANONYMOUS_USER_USERNAME);
        BDDMockito.given(usuarioRepo.findByUsername(Mockito.anyString()))
                .willReturn(Optional.empty());

        //when

        //then
        Assertions.assertThatThrownBy(() -> underTest.getCurrentUser())
                .isInstanceOf(UsuarioNotFoundException.class)
                .hasMessageContaining(ERROR_MSG);
    }
}