package com.argentinaprog.yoprogramo.proyectocv.spa.jere.services;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private final UsuarioRepository usuarioRepo;
    
    public UsuarioService(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }
}
