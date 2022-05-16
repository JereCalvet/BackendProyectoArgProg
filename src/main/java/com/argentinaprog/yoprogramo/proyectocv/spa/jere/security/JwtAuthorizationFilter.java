package com.argentinaprog.yoprogramo.proyectocv.spa.jere.security;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions.JwtTokenInvalidoException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;

    public JwtAuthorizationFilter(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader(AUTHORIZATION);
        if (isBearerHeaderInvalid(authHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.replace("Bearer ", "");

        try {
            final var verified = JWT
                    .require(jwtConfig.algorithmWithSecret())
                    .build()
                    .verify(token);

            final List<SimpleGrantedAuthority> authorities =
                    Arrays.stream(verified.getClaim("roles").asArray(String.class))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

            final var authToken = new UsernamePasswordAuthenticationToken(
                    verified.getSubject(),
                    null,
                    authorities);

            SecurityContextHolder.getContext().setAuthentication(authToken);
        } catch (JWTVerificationException exception) {
            throw new JwtTokenInvalidoException(token, exception.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isBearerHeaderInvalid(String authHeader) {
        return Strings.isBlank(authHeader) || Strings.isEmpty(authHeader) || !(authHeader.startsWith("Bearer "));
    }
}
