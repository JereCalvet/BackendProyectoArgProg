package com.argentinaprog.yoprogramo.proyectocv.spa.jere.controllers;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.Persona;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.dto.*;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.services.PersonaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/persona")
public class PersonaController {

    @Autowired
    private final PersonaService personaSvc;
    
    public PersonaController(PersonaService personaSvc) {
        this.personaSvc = personaSvc;
    }

    // ------------------- Persona -----------------------------

    @GetMapping("/find/{id}")
    public ResponseEntity<Persona> getPersona(@PathVariable("id") Long id) {
        return new ResponseEntity<>(personaSvc.getPersona(id), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Persona> addPersona(@RequestBody @Valid PersonaDto personaToAdd) {
        return new ResponseEntity<>(personaSvc.addPersona(personaToAdd), HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Persona> updatePersona(@PathVariable("id") Long id, @RequestBody @Valid PersonaDto personaToUpdate) {
        return new ResponseEntity<>(personaSvc.updatePersona(id, personaToUpdate), HttpStatus.OK);
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Void> deletePersona(@PathVariable("id") Long id) {
        personaSvc.deletePersona(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Persona>> getAllPersonas() {
        return new ResponseEntity<>(personaSvc.getAllPersonas(), HttpStatus.OK);
    }

    // ------------------- Trabajos -----------------------------

    @PostMapping("/add/{id}/trabajos/")
    public ResponseEntity<Persona> addTrabajo(@PathVariable("id") Long id, @RequestBody @Valid TrabajoDto trabajoToAdd) {
        return new ResponseEntity<>(personaSvc.addTrabajo(id, trabajoToAdd), HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}/trabajos/{idTrabajo}")
    public ResponseEntity<Persona> updateTrabajo(@PathVariable("id") Long id, @PathVariable("idTrabajo") Long idTrabajo, @RequestBody @Valid TrabajoDto trabajoToUpdate) {
        return new ResponseEntity<>(personaSvc.updateTrabajo(id, idTrabajo, trabajoToUpdate), HttpStatus.OK);
    }

    @DeleteMapping("/remove/{id}/trabajos/{idTrabajo}")
    public ResponseEntity<Void> removeTrabajo(@PathVariable("id") Long id, @PathVariable("idTrabajo") Long idTrabajo) {
        personaSvc.removeTrabajo(id, idTrabajo);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ------------------- Educación -----------------------------

    @PostMapping("/add/{id}/estudios/")
    public ResponseEntity<Persona> addEstudio(@PathVariable("id") Long id, @RequestBody @Valid EducacionDto estudioToAdd) {
        return new ResponseEntity<>(personaSvc.addEstudio(id, estudioToAdd), HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}/estudios/{idEstudio}")
    public ResponseEntity<Persona> updateEstudio(@PathVariable("id") Long id, @PathVariable("idEstudio") Long idEstudio, @RequestBody @Valid EducacionDto estudioToUpdate) {
        return new ResponseEntity<>(personaSvc.updateEstudio(id, idEstudio, estudioToUpdate), HttpStatus.OK);
    }

    @DeleteMapping("/remove/{id}/estudios/{idEstudio}")
    public ResponseEntity<Void> removeEstudio(@PathVariable("id") Long id, @PathVariable("idEstudio") Long idEstudio) {
        personaSvc.removeEstudio(id, idEstudio);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ------------------- Proyecto -----------------------------

    @PostMapping("/add/{id}/proyectos/")
    public ResponseEntity<Persona> addProyecto(@PathVariable("id") Long id, @RequestBody @Valid ProyectoDto proyectoToAdd) {
        return new ResponseEntity<>(personaSvc.addProyecto(id, proyectoToAdd), HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}/proyectos/{idProyecto}")
    public ResponseEntity<Persona> updateProyecto(@PathVariable("id") Long id, @PathVariable("idProyecto") Long idProyecto, @RequestBody @Valid ProyectoDto proyectoToUpdate) {
        return new ResponseEntity<>(personaSvc.updateProyecto(id, idProyecto, proyectoToUpdate), HttpStatus.OK);
    }

    @DeleteMapping("/remove/{id}/proyectos/{idProyecto}")
    public ResponseEntity<Void> removeProyecto(@PathVariable("id") Long id, @PathVariable("idProyecto") Long idProyecto) {
        personaSvc.removeProyecto(id, idProyecto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ------------------- Habilidad -----------------------------

    @PostMapping("/add/{id}/habilidades/")
    public ResponseEntity<Persona> addHabilidad(@PathVariable("id") Long id, @RequestBody @Valid HabilidadDto habilidadToAdd) {
        return new ResponseEntity<>(personaSvc.addHabilidad(id, habilidadToAdd), HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}/habilidades/{idHabilidad}")
    public ResponseEntity<Persona> updateHabilidad(@PathVariable("id") Long id, @PathVariable("idHabilidad") Long idHabilidad, @RequestBody @Valid HabilidadDto habilidadToUpdate) {
        return new ResponseEntity<>(personaSvc.updateHabilidad(id, idHabilidad, habilidadToUpdate), HttpStatus.OK);
    }

    @DeleteMapping("/remove/{id}/habilidades/{idHabilidad}")
    public ResponseEntity<Void> removeHabilidad(@PathVariable("id") Long id, @PathVariable("idHabilidad") Long idHabilidad) {
        personaSvc.removeHabilidad(id, idHabilidad);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
