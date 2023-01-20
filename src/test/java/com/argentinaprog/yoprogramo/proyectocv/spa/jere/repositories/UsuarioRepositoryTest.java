package com.argentinaprog.yoprogramo.proyectocv.spa.jere.repositories;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.AbstractContainerBaseTest;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.Usuario;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsuarioRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private UsuarioRepository underTest;

    @Autowired
    private TestEntityManager entityManager;

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void findByUsername() {
        // given
        String emailNombreUsuario = "jere@test.com";
        String password = "password";

        Usuario usuario = Usuario.builder()
                .username(emailNombreUsuario)
                .password(password)
                .build();

        Usuario usuarioGuardado = entityManager.persistFlushFind(usuario);

        // when
        Optional<Usuario> usuarioEncontrado = underTest.findByUsername(emailNombreUsuario);

        // then
        Assertions.assertThat(usuarioEncontrado)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(usuarioGuardado);
    }

    @Test
    void findByUsername_WhenUsernameNotPresent_ShouldReturnEmptyUser() {
        //given
        String emailNombreUsuario = "jere@test.com";

        //when
        Optional<Usuario> usuarioNoEncontrado = underTest.findByUsername(emailNombreUsuario);

        //then
        Assertions.assertThat(usuarioNoEncontrado)
                .isNotPresent();
    }
}