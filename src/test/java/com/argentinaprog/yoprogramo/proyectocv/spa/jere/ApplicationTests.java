package com.argentinaprog.yoprogramo.proyectocv.spa.jere;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.controllers.AuthController;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.controllers.PersonaController;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests extends AbstractContainerBaseTest {

    @Autowired
    private AuthController authController;

    @Autowired
    private PersonaController personaController;

    @Test
    void contextLoads() {
        Assertions.assertThat(authController).isNotNull();
        Assertions.assertThat(personaController).isNotNull();
    }

    @Test
    void testContainerLoads() {
        Assertions.assertThat(MY_SQL_CONTAINER.isRunning());
    }

}
