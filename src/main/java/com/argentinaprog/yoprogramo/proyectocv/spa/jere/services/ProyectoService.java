package com.argentinaprog.yoprogramo.proyectocv.spa.jere.services;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.repositories.ProyectoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProyectoService {

    @Autowired
    private final ProyectoRepository proyectosRepo;

    public ProyectoService(ProyectoRepository proyectosRepo) {
        this.proyectosRepo = proyectosRepo;
    }
}
