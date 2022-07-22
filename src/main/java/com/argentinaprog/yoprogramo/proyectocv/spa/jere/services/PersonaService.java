package com.argentinaprog.yoprogramo.proyectocv.spa.jere.services;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions.*;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.*;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.dto.*;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.repositories.PersonaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PersonaService {

    @Autowired
    private final PersonaRepository personaRepo;

    @Autowired
    private final UsuarioService usuarioSvc;

    @Autowired
    private final ModelMapper mapper;

    public PersonaService(PersonaRepository personaRepo, UsuarioService usuarioSvc, ModelMapper mapper) {
        this.personaRepo = personaRepo;
        this.usuarioSvc = usuarioSvc;
        this.mapper = mapper;
    }

    // ------------------- Persona -----------------------------

    public Persona getPersona(Long id) {
        return personaRepo.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException(id));
    }

    @Transactional
    public Persona addPersona(PersonaDto personaDto) {
        var persona = mapper.map(personaDto, Persona.class);
        final Usuario currentUser = this.usuarioSvc.getCurrentUser();

        if (Objects.nonNull(currentUser.getPersona())) {
            throw new PersonaAlreadyExistsException(currentUser.getUsername());
        }
        currentUser.setPersona(persona);
        return personaRepo.save(persona);
    }

    @Transactional
    public void deletePersona(Long id) {
        var persona = personaRepo.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException(id));
        personaRepo.delete(persona);
    }

    @Transactional
    public Persona updatePersona(Long id, PersonaDto personaDto) {
        var persona = personaRepo.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException(id));
        mapper.map(personaDto, persona);

        return personaRepo.save(persona);
    }

    public Persona getCurrentPersona() {
        final Usuario currentUser = this.usuarioSvc.getCurrentUser();
        return Optional.ofNullable(currentUser.getPersona())
                .orElseThrow(() -> new PersonaNotFoundException(currentUser.getUsername()));
    }

    public List<Persona> getAllPersonas() {
        return personaRepo.findAll();
    }

    // ------------------- Trabajo -----------------------------

    @Transactional
    public Persona addTrabajo(Long id, TrabajoDto trabajoDto) {
        var persona = personaRepo.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException(id));

        final Trabajo trabajoParaAgregar = mapper.map(trabajoDto, Trabajo.class);
        trabajoParaAgregar.setPersona(persona);
        persona.getExperienciasLaborales().add(trabajoParaAgregar);

        return personaRepo.save(persona);
    }

    @Transactional
    public Persona updateTrabajo(Long id, Long idTrabajo, TrabajoDto trabajoDto) {
        var persona = personaRepo.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException(id));

        final Trabajo nuevosDatosTrabajo = mapper.map(trabajoDto, Trabajo.class);
        final Trabajo trabajoParaActualizar = persona.getExperienciasLaborales()
                .stream()
                .filter(trabajo -> Objects.equals(trabajo.getId(), idTrabajo))
                .findFirst()
                .orElseThrow(() -> new TrabajoNotFoundException(idTrabajo));
        mapper.map(nuevosDatosTrabajo, trabajoParaActualizar);

        return personaRepo.save(persona);
    }

    @Transactional
    public void removeTrabajo(Long id, Long idTrabajo) {
        var persona = personaRepo.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException(id));

        final List<Trabajo> experienciasLaborales = persona.getExperienciasLaborales();
        if (experienciasLaborales.stream().noneMatch(trabajo -> Objects.equals(trabajo.getId(), idTrabajo))) {
            throw new TrabajoNotFoundException(idTrabajo);
        }
        experienciasLaborales.removeIf(trabajo -> trabajo.getId().equals(idTrabajo));

        personaRepo.save(persona);
    }

    // ------------------- Educacion -----------------------------

    @Transactional
    public Persona addEstudio(Long id, EducacionDto educacionDto) {
        var persona = personaRepo.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException(id));

        final Educacion estudioParaAgregar = mapper.map(educacionDto, Educacion.class);
        estudioParaAgregar.setPersona(persona);
        persona.getEstudios().add(estudioParaAgregar);

        return personaRepo.save(persona);
    }

    @Transactional
    public Persona updateEstudio(Long id, Long idEstudio, EducacionDto educacionDto) {
        var persona = personaRepo.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException(id));

        final Educacion nuevosDatosEstudio = mapper.map(educacionDto, Educacion.class);
        final Educacion estudioParaActualizar = persona.getEstudios()
                .stream()
                .filter(estudio -> Objects.equals(estudio.getId(), idEstudio))
                .findFirst()
                .orElseThrow(() -> new EducacionNotFoundException(idEstudio));
        mapper.map(nuevosDatosEstudio, estudioParaActualizar);

        return personaRepo.save(persona);
    }

    @Transactional
    public void removeEstudio(Long id, Long idEstudio) {
        var persona = personaRepo.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException(id));

        final List<Educacion> estudios = persona.getEstudios();
        if (estudios.stream().noneMatch(trabajo -> Objects.equals(trabajo.getId(), idEstudio))) {
            throw new EducacionNotFoundException(idEstudio);
        }
        estudios.removeIf(trabajo -> trabajo.getId().equals(idEstudio));

        personaRepo.save(persona);
    }

    // ------------------- Proyecto -----------------------------

    @Transactional
    public Persona addProyecto(Long id, ProyectoDto proyectoDto) {
        var persona = personaRepo.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException(id));

        final Proyecto proyectoParaAgregar = mapper.map(proyectoDto, Proyecto.class);
        proyectoParaAgregar.setPersona(persona);
        persona.getProyectos().add(proyectoParaAgregar);

        return personaRepo.save(persona);
    }

    @Transactional
    public Persona updateProyecto(Long id, Long idProyecto, ProyectoDto proyectoDto) {
        var persona = personaRepo.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException(id));

        final Proyecto nuevosDatosProyecto = mapper.map(proyectoDto, Proyecto.class);
        final Proyecto proyectoParaActualizar = persona.getProyectos()
                .stream()
                .filter(proyecto -> Objects.equals(proyecto.getId(), idProyecto))
                .findFirst()
                .orElseThrow(() -> new ProyectoNotFoundException(idProyecto));
        mapper.map(nuevosDatosProyecto, proyectoParaActualizar);

        return personaRepo.save(persona);
    }

    @Transactional
    public void removeProyecto(Long id, Long idProyecto) {
        var persona = personaRepo.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException(id));

        final List<Proyecto> proyectos = persona.getProyectos();
        if (proyectos.stream().noneMatch(proyecto -> Objects.equals(proyecto.getId(), idProyecto))) {
            throw new EducacionNotFoundException(idProyecto);
        }
        proyectos.removeIf(trabajo -> trabajo.getId().equals(idProyecto));

        personaRepo.save(persona);
    }

    // ------------------- Habilidad -----------------------------

    @Transactional
    public Persona addHabilidad(Long id, HabilidadDto habilidadDto) {
        var persona = personaRepo.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException(id));

        final Habilidad habilidadParaAgregar = mapper.map(habilidadDto, Habilidad.class);
        habilidadParaAgregar.setPersona(persona);
        persona.getHabilidades().add(habilidadParaAgregar);

        return personaRepo.save(persona);
    }

    @Transactional
    public Persona updateHabilidad(Long id, Long idHabilidad, HabilidadDto habilidadDto) {
        var persona = personaRepo.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException(id));

        final Habilidad nuevosDatosHabilidad = mapper.map(habilidadDto, Habilidad.class);
        final Habilidad habilidadParaActualizar = persona.getHabilidades()
                .stream()
                .filter(habilidad -> Objects.equals(habilidad.getId(), idHabilidad))
                .findFirst()
                .orElseThrow(() -> new HabilidadNotFoundException(idHabilidad));
        mapper.map(nuevosDatosHabilidad, habilidadParaActualizar);

        return personaRepo.save(persona);
    }

    @Transactional
    public void removeHabilidad(Long id, Long idHabilidad) {
        var persona = personaRepo.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException(id));

        final List<Habilidad> habilidades = persona.getHabilidades();
        if (habilidades.stream().noneMatch(habilidad -> Objects.equals(habilidad.getId(), idHabilidad))) {
            throw new HabilidadNotFoundException(idHabilidad);
        }
        habilidades.removeIf(habilidad -> habilidad.getId().equals(idHabilidad));

        personaRepo.save(persona);
    }
}
