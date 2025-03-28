package com.argentinaprog.yoprogramo.proyectocv.spa.jere;

import org.testcontainers.containers.MySQLContainer;

public abstract class AbstractContainerBaseTest {

    static final MySQLContainer MY_SQL_CONTAINER;

    static {
        MY_SQL_CONTAINER = new MySQLContainer<>("mysql:8.0.30")
                .withDatabaseName("testDB")
                .withUsername("testUser")
                .withPassword("testPassword");
        MY_SQL_CONTAINER.start();

        System.setProperty("spring.datasource.url", MY_SQL_CONTAINER.getJdbcUrl());
        System.setProperty("spring.datasource.password", MY_SQL_CONTAINER.getPassword());
        System.setProperty("spring.datasource.username", MY_SQL_CONTAINER.getUsername());

    }
}

