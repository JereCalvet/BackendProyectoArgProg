package com.argentinaprog.yoprogramo.proyectocv.spa.jere.services;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions.PersonaAlreadyExistsException;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions.PersonaNotFoundException;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.Nacionalidades;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.Persona;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.Usuario;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.dto.PersonaDto;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.repositories.PersonaRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonaServiceTest {

    @Mock
    private UsuarioService usuarioSvc;

    @Mock
    private ModelMapper mapper;

    @Mock
    private PersonaRepository personaRepo;

    private PersonaService underTest;

    @BeforeEach
    void setUp() {
        underTest = new PersonaService(personaRepo, usuarioSvc, mapper);
    }

    @DisplayName("Obtener persona correctamente")
    @Test
    void getPersona_ShouldReturnPersona() {
        //given
        final Long id = 1L;
        final String nombres = "jere";
        Persona personaJere = Persona.builder()
                .id(id)
                .nombres(nombres)
                .build();
        BDDMockito.given(personaRepo.findById(Mockito.anyLong()))
                .willReturn(Optional.of(personaJere));

        //when
        final Persona personaEncontrada = underTest.getPersona(id);

        //then
        ArgumentCaptor<Long> idArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(personaRepo).findById(idArgumentCaptor.capture());
        Assertions.assertThat(personaEncontrada).isEqualTo(personaJere);
        Assertions.assertThat(personaEncontrada.getNombres()).isEqualTo(nombres);

        final Long idCapturado = idArgumentCaptor.getValue();
        Assertions.assertThat(idCapturado).isEqualTo(id);
    }

    @DisplayName("Obtener persona debe tirar error cuando la persona no existe")
    @Test
    void getPersona_whenPersonaNotFound_shouldThrowPersonaNotFoundException() {
        //given
        final Long id = 1L;
        final String errorMsg = String.format("Persona id %d no encontrada.", id);

        BDDMockito.given(personaRepo.findById(Mockito.anyLong()))
                .willReturn(Optional.empty());
        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.getPersona(id))
                .isInstanceOf(PersonaNotFoundException.class)
                .hasMessageContaining(errorMsg);
    }

    @DisplayName("Debe crear persona")
    @Test
    void addPersona_ShouldAddPersona() {
        //given
        final String emailDeRegistro = "registro@test.com";
        final String securePassword = "admin123";

        Usuario usuarioJere = Usuario.builder()
                .username(emailDeRegistro)
                .password(securePassword)
                .build();

        final String nombres = "jere";
        final String apellidos = "test";
        final LocalDate fechaNacimiento = LocalDate.of(1990, 1, 1);
        final Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        final String email = "jere@test.com";
        final String descripcion = "acerca de test";
        final String imagen = "assets/imagen.jpg";
        final String ocupacion = "tester";

        PersonaDto personaDto = new PersonaDto(
                nombres,
                apellidos,
                fechaNacimiento,
                nacionalidad,
                email,
                descripcion,
                imagen,
                ocupacion,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        Persona personaJere = Persona.builder()
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .nacionalidad(nacionalidad)
                .email(email)
                .descripcion(descripcion)
                .imagen(imagen)
                .ocupacion(ocupacion)
                .build();

        BDDMockito.given(usuarioSvc.getCurrentUser())
                .willReturn(usuarioJere);

        BDDMockito.given(mapper.map(Mockito.any(), Mockito.eq(Persona.class)))
                .willReturn(personaJere);

        //when
        underTest.addPersona(personaDto);

        //then
        verify(mapper).map(personaDto, Persona.class);

        ArgumentCaptor<Persona> personaArgumentCaptor = ArgumentCaptor.forClass(Persona.class);
        verify(personaRepo).save(personaArgumentCaptor.capture());

        final Persona personaCaptured = personaArgumentCaptor.getValue();
        Assertions.assertThat(personaCaptured.getNombres()).isEqualTo(personaDto.getNombres());
        Assertions.assertThat(personaCaptured.getApellidos()).isEqualTo(personaDto.getApellidos());
        Assertions.assertThat(personaCaptured.getFechaNacimiento()).isEqualTo(fechaNacimiento);
        Assertions.assertThat(personaCaptured.getNacionalidad()).isEqualTo(nacionalidad);
        Assertions.assertThat(personaCaptured.getEmail()).isEqualTo(email);
        Assertions.assertThat(personaCaptured.getDescripcion()).isEqualTo(descripcion);
        Assertions.assertThat(personaCaptured.getImagen()).isEqualTo(imagen);
        Assertions.assertThat(personaCaptured.getOcupacion()).isEqualTo(ocupacion);
        Assertions.assertThat(usuarioJere.getPersona()).isEqualTo(personaJere);
    }

    @DisplayName("Debe tirar error cuando el usuario ya tiene una persona creada e intenta crear otra.")
    @Test
    void addPersona_whenLoggedUserAlreadyHasPersona_shouldThrowPersonaAlreadyExistsException() {
        //given
        final String nombres = "jere";
        final String apellidos = "test";
        final LocalDate fechaNacimiento = LocalDate.of(1990, 1, 1);
        final Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        final String email = "jere@test.com";
        final String descripcion = "acerca de test";
        final String imagen = "assets/imagen.jpg";
        final String ocupacion = "tester";

        PersonaDto personaDto = new PersonaDto(
                nombres,
                apellidos,
                fechaNacimiento,
                nacionalidad,
                email,
                descripcion,
                imagen,
                ocupacion,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        Persona personaJere = Persona.builder()
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .nacionalidad(nacionalidad)
                .email(email)
                .descripcion(descripcion)
                .imagen(imagen)
                .ocupacion(ocupacion)
                .build();

        final String emailDeRegistro = "registro@test.com";

        Usuario supuestoUsuarioLogeado = Mockito.mock(Usuario.class);
        BDDMockito.given(usuarioSvc.getCurrentUser())
                .willReturn(supuestoUsuarioLogeado);
        BDDMockito.given(supuestoUsuarioLogeado.getUsername())
                .willReturn(emailDeRegistro);
        BDDMockito.when(supuestoUsuarioLogeado.getPersona())
                .thenReturn(personaJere);

        final String errorMsg = String.format("El usuario %s ya tiene una persona creada.", emailDeRegistro);

        //when
        //underTest.addPersona(personaDto);

        //then
        Assertions.assertThatThrownBy(() -> underTest.addPersona(personaDto))
                .isInstanceOf(PersonaAlreadyExistsException.class)
                .hasMessageContaining(errorMsg);

        Mockito.verify(personaRepo, Mockito.never()).save(Mockito.any());
    }

    @DisplayName("Debe tirar borrar la persona correctamente, el id es valido")
    @Test
    void deletePersona() {
        //given
        final Long id = 1L;
        final String nombres = "jere";
        final String apellidos = "test";
        final LocalDate fechaNacimiento = LocalDate.of(1990, 1, 1);
        final Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        final String email = "jere@test.com";
        final String descripcion = "acerca de test";
        final String imagen = "assets/imagen.jpg";
        final String ocupacion = "tester";

        Persona personaJere = Persona.builder()
                .id(id)
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .nacionalidad(nacionalidad)
                .email(email)
                .descripcion(descripcion)
                .imagen(imagen)
                .ocupacion(ocupacion)
                .estudios(List.of())
                .experienciasLaborales(List.of())
                .habilidades(List.of())
                .proyectos(List.of())
                .build();

        BDDMockito.given(personaRepo.findById(Mockito.anyLong()))
                .willReturn(Optional.of(personaJere));

        //when
        underTest.deletePersona(id);

        //then
        ArgumentCaptor<Persona> personaArgumentCaptor = ArgumentCaptor.forClass(Persona.class);
        Mockito.verify(personaRepo).delete(personaArgumentCaptor.capture());

        final Persona personaValue = personaArgumentCaptor.getValue();
        Assertions.assertThat(personaValue).isEqualTo(personaJere);
        Assertions.assertThat(personaValue.getId()).isEqualTo(id);
    }

    @DisplayName("Debe tirar error al intentar borrar, cuando no existe una persona con ese id")
    @Test
    void deletePersona_whenPersonaByIdNotFound_shouldThrowPersonaNotFoundException() {
        //given
        final Long id = 1L;
        final String errorMsg = String.format("Persona id %d no encontrada.", id);

        BDDMockito.given(personaRepo.findById(Mockito.anyLong()))
                .willReturn(Optional.empty());

        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.deletePersona(id))
                .isInstanceOf(PersonaNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Mockito.verify(personaRepo, Mockito.never()).delete(Mockito.any());
    }

    @DisplayName("Debe actualizar la persona cuando es id es valido")
    @Test
    void updatePersona() {
        //given
        Long id = 1L;
        String nombresViejo = "testNombre";
        String nombresUpdate = "Jeremías";
        String apellidosViejo = "testApellido";
        String apellidosUpdate = "Calvet";
        LocalDate fechaNacimientoVieja = LocalDate.of(1990, 1, 1);
        LocalDate fechaNacimientoUpdate = LocalDate.of(1991, 1, 1);
        Nacionalidades nacionalidadVieja = Nacionalidades.BRASIL;
        Nacionalidades nacionalidadUpdate = Nacionalidades.ARGENTINA;
        String emailViejo = "mail@test.com";
        String emailUpdate = "jere@test.com";
        String descripcion = "acerca de test";
        String imagen = "assets/imagen.jpg";
        String ocupacion = "tester";

        Persona personaJere = Persona.builder()
                .id(id)
                .nombres(nombresViejo)
                .apellidos(apellidosViejo)
                .fechaNacimiento(fechaNacimientoVieja)
                .nacionalidad(nacionalidadVieja)
                .email(emailViejo)
                .descripcion(descripcion)
                .imagen(imagen)
                .ocupacion(ocupacion)
                .build();

        PersonaDto personaDto = new PersonaDto(
                nombresUpdate,
                apellidosUpdate,
                fechaNacimientoUpdate,
                nacionalidadUpdate,
                emailUpdate,
                null,
                null,
                ocupacion,
                null,
                null,
                null,
                null,
                null
        );

        BDDMockito.given(personaRepo.findById(Mockito.anyLong()))
                .willReturn(Optional.of(personaJere));
        BDDMockito.given(personaRepo.save(Mockito.any(Persona.class)))
                .willReturn(personaJere);

        doAnswer((invocation) -> {
                    var dto = (PersonaDto) invocation.getArgument(0);
                    var persona = (Persona) invocation.getArgument(1);
                    persona.setNombres(dto.getNombres());
                    persona.setApellidos(dto.getApellidos());
                    persona.setFechaNacimiento(dto.getFechaNacimiento());
                    persona.setNacionalidad(dto.getNacionalidad());
                    persona.setEmail(dto.getEmail());

                    return null;
                }
        ).when(mapper).map(personaDto, personaJere);

        //when
        final Persona updatedPersona = underTest.updatePersona(id, personaDto);

        //then
        verify(mapper).map(personaDto, personaJere);

        ArgumentCaptor<Long> idArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(personaRepo).findById(idArgumentCaptor.capture());
        final Long capturedValueIdArgument = idArgumentCaptor.getValue();
        Assertions.assertThat(capturedValueIdArgument).isEqualTo(id);

        ArgumentCaptor<Persona> personaArgumentCaptor = ArgumentCaptor.forClass(Persona.class);
        Mockito.verify(personaRepo).save(personaArgumentCaptor.capture());
        final Persona capturedValuePersonaArgument = personaArgumentCaptor.getValue();
        Assertions.assertThat(capturedValuePersonaArgument).isEqualTo(personaJere);

        Assertions.assertThat(updatedPersona).isEqualTo(personaJere);
        Assertions.assertThat(updatedPersona.getId()).isEqualTo(id);
        Assertions.assertThat(updatedPersona.getNombres()).isEqualTo(nombresUpdate);
        Assertions.assertThat(updatedPersona.getApellidos()).isEqualTo(apellidosUpdate);
        Assertions.assertThat(updatedPersona.getFechaNacimiento()).isEqualTo(fechaNacimientoUpdate);
        Assertions.assertThat(updatedPersona.getNacionalidad()).isEqualTo(nacionalidadUpdate);
        Assertions.assertThat(updatedPersona.getEmail()).isEqualTo(emailUpdate);
        Assertions.assertThat(updatedPersona.getOcupacion()).isEqualTo(ocupacion);
        Assertions.assertThat(updatedPersona.getDescripcion()).isEqualTo(descripcion);
        Assertions.assertThat(updatedPersona.getImagen()).isEqualTo(imagen);
    }

    @DisplayName("Debe tirar error al actualizar la persona cuando el id es invalido")
    @Test
    void updatePersona_WhenIdIsInvalid_ShouldThrowException() {
        //given
        final Long id = 1L;
        final String errorMsg = String.format("Persona id %d no encontrada.", id);
        String nombres = "Jeremías";
        String apellidos = "Calvet";
        LocalDate fechaNacimiento = LocalDate.of(1990, 1, 1);
        Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        String email = "jere@test.com";
        String descripcion = "acerca de test";
        String imagen = "assets/imagen.jpg";
        String ocupacion = "tester";
        var personaDto = new PersonaDto(
                nombres,
                apellidos,
                fechaNacimiento,
                nacionalidad,
                email,
                descripcion,
                imagen,
                ocupacion,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.empty());

        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.updatePersona(id, personaDto))
                .isInstanceOf(PersonaNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Mockito.verify(personaRepo, Mockito.never()).save(Mockito.any());
        Mockito.verify(personaRepo, times(1)).findById(id);
    }

    @Test
    void getCurrentPersona() {
    }

    @Test
    void getAllPersonas() {
    }

    @Test
    void addTrabajo() {
    }

    @Test
    void updateTrabajo() {
    }

    @Test
    void removeTrabajo() {
    }

    @Test
    void addEstudio() {
    }

    @Test
    void updateEstudio() {
    }

    @Test
    void removeEstudio() {
    }

    @Test
    void addProyecto() {
    }

    @Test
    void updateProyecto() {
    }

    @Test
    void removeProyecto() {
    }

    @Test
    void addHabilidad() {
    }

    @Test
    void updateHabilidad() {
    }

    @Test
    void removeHabilidad() {
    }
}