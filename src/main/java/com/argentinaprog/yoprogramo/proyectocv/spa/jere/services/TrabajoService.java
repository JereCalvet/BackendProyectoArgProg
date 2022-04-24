package com.argentinaprog.yoprogramo.proyectocv.spa.jere.services;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.repositories.TrabajoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrabajoService {

    @Autowired
    private final TrabajoRepository trabajoRepo;

    public TrabajoService(TrabajoRepository trabajoRepo) {
        this.trabajoRepo = trabajoRepo;
    }
}
