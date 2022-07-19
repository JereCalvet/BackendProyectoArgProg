package com.argentinaprog.yoprogramo.proyectocv.spa.jere.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private final UserDetailsService userDetailsService;
    @Autowired
    private final PasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private final JwtConfig jwtConfig;

    public SecurityConfig(UserDetailsService userDetailsService, PasswordEncoder bCryptPasswordEncoder, JwtConfig jwtConfig) {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtConfig = jwtConfig;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        var jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager(), jwtConfig);
        jwtAuthenticationFilter.setFilterProcessesUrl("/api/v1/auth/login");

        // @formatter:off
        http.csrf().disable()
                .cors()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(STATELESS)
                .and()
                .addFilter(jwtAuthenticationFilter)
                .addFilterAfter(new JwtAuthorizationFilter(jwtConfig), JwtAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/api/v1/auth/**").permitAll()
                .antMatchers("/api/v1/persona/find/{id}").permitAll()
                .antMatchers("/api/v1/persona/all").permitAll()
                .antMatchers("/api/v1/persona/current").permitAll()
                .anyRequest()
                .authenticated();
        // @formatter:on
    }

    @Bean @Value("${frontend.url}")
    public CorsConfigurationSource corsConfigurationSource(String frontendUrl) {
        var corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        log.info("frontendUrl: {}", frontendUrl);
        System.out.println(frontendUrl);
        log.info("------------");
        System.out.println("------------");
        corsConfiguration.setAllowedOrigins(List.of(frontendUrl, "https://portfolio-e8aa4.web.app/","https://portfolio-e8aa4.firebaseapp.com/", "https://portfolio-e8aa4.web.app","https://portfolio-e8aa4.firebaseapp.com"));
        log.info("frontendUrl: {}", corsConfiguration.getAllowedOrigins());
        System.out.println(corsConfiguration.getAllowedOrigins());
        corsConfiguration.setAllowedHeaders(Arrays.asList("Origin", "Access-Control-Allow-Origin", "Content-Type",
                "Accept", "Authorization", "Origin, Accept", "X-Requested-With", "Access-Control-Request-Method",
                "Access-Control-Request-Headers"));
        corsConfiguration.setExposedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization",
                "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        var urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return urlBasedCorsConfigurationSource;
    }
}
