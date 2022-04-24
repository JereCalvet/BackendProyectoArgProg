package com.argentinaprog.yoprogramo.proyectocv.spa.jere.services;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.repositories.HabilidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HabilidadService {

    @Autowired
    private final HabilidadRepository habilidadRepo;

    public HabilidadService(HabilidadRepository habilidadRepo) {
        this.habilidadRepo = habilidadRepo;
    }
}
