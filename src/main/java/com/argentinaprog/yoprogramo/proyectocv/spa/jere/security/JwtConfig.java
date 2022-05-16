package com.argentinaprog.yoprogramo.proyectocv.spa.jere.security;

import com.auth0.jwt.algorithms.Algorithm;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.jwt")
@Getter
@Setter
public class JwtConfig {

    private String secretKey;

    public Algorithm algorithmWithSecret() {
        return Algorithm.HMAC256(secretKey.getBytes());
    }
}
