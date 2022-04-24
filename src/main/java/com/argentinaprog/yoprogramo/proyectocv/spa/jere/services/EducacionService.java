package com.argentinaprog.yoprogramo.proyectocv.spa.jere.services;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.repositories.EducacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EducacionService {

    @Autowired
    private final EducacionRepository educacionRepo;

    public EducacionService(EducacionRepository educacionRepo) {
        this.educacionRepo = educacionRepo;
    }
}
