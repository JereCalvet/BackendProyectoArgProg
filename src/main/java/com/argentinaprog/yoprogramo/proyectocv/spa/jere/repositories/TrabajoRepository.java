package com.argentinaprog.yoprogramo.proyectocv.spa.jere.repositories;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.Trabajo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrabajoRepository extends JpaRepository<Trabajo, Long> {
}
