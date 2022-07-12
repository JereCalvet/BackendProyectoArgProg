package com.argentinaprog.yoprogramo.proyectocv.spa.jere.controllers;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.Usuario;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.dto.LoginRequestDto;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private final UsuarioService usuarioSvc;

    public AuthController(UsuarioService usuarioSvc) {
        this.usuarioSvc = usuarioSvc;
    }

    @PostMapping("/register")
    public ResponseEntity<Usuario> register(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        return new ResponseEntity<>(usuarioSvc.save(loginRequestDto), HttpStatus.CREATED);
    }

    @GetMapping("/current")
    @ResponseBody
    public ResponseEntity<Usuario> currentUser() {
        return new ResponseEntity<>(usuarioSvc.getCurrentUser(), HttpStatus.OK);
    }
}
