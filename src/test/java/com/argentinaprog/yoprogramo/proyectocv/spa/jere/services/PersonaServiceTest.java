package com.argentinaprog.yoprogramo.proyectocv.spa.jere.services;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.exceptions.*;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.*;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.dto.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        Mockito.verify(mapper, Mockito.never()).map(Mockito.any(), Mockito.any());
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

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(personaRepo, Mockito.never()).delete(Mockito.any());
    }

    @DisplayName("Debe actualizar la persona")
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
        Mockito.verify(mapper).map(personaDto, personaJere);

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
    void updatePersona_WhenIdIsInvalid_ShouldThrowPersonaNotFoundException() {
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

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(mapper, Mockito.never()).map(Mockito.any(), Mockito.any());
        Mockito.verify(personaRepo, Mockito.never()).save(Mockito.any());
    }

    @DisplayName("Debe devolver la persona del usuario logeado")
    @Test
    void getCurrentPersona_ShouldReturnTheCurrentUserPersona() {
        //given
        final Usuario currentUser = new Usuario();
        final Persona persona = new Persona();
        currentUser.setPersona(persona);

        BDDMockito.given(usuarioSvc.getCurrentUser())
                .willReturn(currentUser);

        //when
        final Persona personaUsuarioLogeado = underTest.getCurrentPersona();

        //then
        Mockito.verify(usuarioSvc).getCurrentUser();
        Assertions.assertThat(personaUsuarioLogeado).isEqualTo(persona);
    }

    @DisplayName("Debe tirar error cuando el usuario logeado no tiene persona")
    @Test
    void getCurrentPersona_whenTheCurrentUserDoesNotHaveAPersona_shouldThrowException() {
        //given
        final Usuario currentUser = new Usuario();
        final String username = "jere";
        currentUser.setUsername(username);
        final String errorMsg = String.format("El usuario %s no tiene una persona creada.", username);

        BDDMockito.given(usuarioSvc.getCurrentUser())
                .willReturn(currentUser);

        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.getCurrentPersona())
                .isInstanceOf(PersonaNotFoundException.class)
                .hasMessageContaining(errorMsg);
    }

    @DisplayName("Debe obtener un listado de personas correctamente")
    @Test
    void getAllPersona_shouldReturnAllThePersonas() {
        //given
        final Persona jere = Persona.builder()
                .nombres("Jere")
                .apellidos("Calvet")
                .fechaNacimiento(LocalDate.of(1990, 6, 7))
                .nacionalidad(Nacionalidades.ARGENTINA)
                .build();

        final Persona camila = Persona.builder()
                .nombres("Camila")
                .apellidos("Test")
                .fechaNacimiento(LocalDate.of(2000, 6, 7))
                .nacionalidad(Nacionalidades.ARGENTINA)
                .build();

        final List<Persona> listadoPersonas = List.of(jere, camila);

        BDDMockito.given(personaRepo.findAll())
                .willReturn(listadoPersonas);

        //when
        final List<Persona> allPersonas = underTest.getAllPersonas();

        //then
        Mockito.verify(personaRepo).findAll();

        Assertions.assertThat(allPersonas)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(listadoPersonas)
                .hasSize(2);
    }

    @DisplayName("Debe agregar un trabajo a la persona")
    @Test
    void addTrabajo() {
        //given
        final Long id = 1L;
        final String nombres = "Jeremías";
        final String apellidos = "Calvet";
        final LocalDate fechaNacimiento = LocalDate.of(1990, 1, 1);
        final Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        List<Trabajo> experienciasLaborales = new ArrayList<>();

        final var personaJere = Persona.builder()
                .id(id)
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .nacionalidad(nacionalidad)
                .build();
        personaJere.setExperienciasLaborales(experienciasLaborales);

        final String empresa = "Carrefour";
        final String cargo = "Tester";
        final String lugar = "Rio Grande";
        final LocalDate inicio = LocalDate.of(2010, 1, 1);
        final LocalDate fin = LocalDate.of(2012, 1, 1);
        final var trabajoDto = new TrabajoDto(
                empresa,
                cargo,
                lugar,
                inicio,
                fin
        );

        final var trabajoParaAgregar = Trabajo.builder()
                .cargo(cargo)
                .empresa(empresa)
                .desde(inicio)
                .hasta(fin)
                .lugar(lugar)
                .persona(null)
                .build();

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.of(personaJere));
        BDDMockito.given(mapper.map(trabajoDto, Trabajo.class))
                .willReturn(trabajoParaAgregar);
        BDDMockito.given(personaRepo.save(Mockito.any(Persona.class)))
                .willReturn(personaJere);

        //when
        final Persona personaJereAfterNewJob = underTest.addTrabajo(id, trabajoDto);

        //then
        ArgumentCaptor<Long> idArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(personaRepo).findById(idArgumentCaptor.capture());
        final Long idCapturedArgumentValue = idArgumentCaptor.getValue();
        Assertions.assertThat(idCapturedArgumentValue).isEqualTo(id);

        ArgumentCaptor<TrabajoDto> trabajoDtoArgumentCaptor = ArgumentCaptor.forClass(TrabajoDto.class);
        Mockito.verify(mapper).map(trabajoDtoArgumentCaptor.capture(), Mockito.eq(Trabajo.class));
        final TrabajoDto trabajoDtoCapturedArgumentValue = trabajoDtoArgumentCaptor.getValue();
        Assertions.assertThat(trabajoDtoCapturedArgumentValue).isEqualTo(trabajoDto);

        ArgumentCaptor<Persona> personaArgumentCaptor = ArgumentCaptor.forClass(Persona.class);
        Mockito.verify(personaRepo).save(personaArgumentCaptor.capture());
        final Persona personaCapturedArgumentValue = personaArgumentCaptor.getValue();
        Assertions.assertThat(personaCapturedArgumentValue).isEqualTo(personaJere);

        Assertions.assertThat(personaJereAfterNewJob.getId()).isEqualTo(id);
        Assertions.assertThat(personaJereAfterNewJob.getNombres()).isEqualTo(nombres);
        Assertions.assertThat(personaJereAfterNewJob.getApellidos()).isEqualTo(apellidos);
        Assertions.assertThat(personaJereAfterNewJob.getNacionalidad()).isEqualTo(nacionalidad);
        Assertions.assertThat(personaJereAfterNewJob.getFechaNacimiento()).isEqualTo(fechaNacimiento);
        final List<Trabajo> experienciasLaboralesDespuesAgregarTrabajo = personaJereAfterNewJob.getExperienciasLaborales();
        Assertions.assertThat(experienciasLaboralesDespuesAgregarTrabajo)
                .isNotNull()
                .isEqualTo(experienciasLaborales)
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(trabajoParaAgregar);

        Assertions.assertThat(trabajoParaAgregar.getPersona())
                .isNotNull()
                .isEqualTo(personaJere);
    }

    @DisplayName("Agregar un trabajo debe tirar error, cuando el id de la persona es invalido")
    @Test
    void addTrabajo_WhenPersonaDoesNotExist_ShouldThrowPersonaNotFoundException() {
        //given
        final Long id = 1L;
        final var trabajoDto = new TrabajoDto(
                "Carrefour",
                "Tester",
                "Rio Grande",
                LocalDate.of(2010, 1, 1),
                LocalDate.of(2012, 1, 1)
        );
        final String errorMsg = String.format("Persona id %d no encontrada.", id);

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.empty());

        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.addTrabajo(id, trabajoDto))
                .isInstanceOf(PersonaNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(mapper, Mockito.never()).map(Mockito.any(), Mockito.any());
        Mockito.verify(personaRepo, Mockito.never()).save(Mockito.any());
    }

    @DisplayName("Debe actualizar los datos de un trabajo")
    @Test
    void updateTrabajo() {
        //given
        final Long id = 1L;
        final Long idTrabajo = 2L;
        final var personaJere = Persona.builder()
                .id(id)
                .nombres("Jeremías")
                .apellidos("Calvet")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .nacionalidad(Nacionalidades.ARGENTINA)
                .build();

        final var trabajoParaActualizar = Trabajo.builder()
                .cargo("Tester")
                .empresa("Carrefour")
                .desde(LocalDate.of(2010, 1, 1))
                .hasta(LocalDate.of(2012, 1, 1))
                .lugar("Rio Grande")
                .persona(personaJere)
                .id(idTrabajo)
                .build();
        List<Trabajo> experienciasLaborales = Stream.of(trabajoParaActualizar)
                .collect(Collectors.toList());
        personaJere.setExperienciasLaborales(experienciasLaborales);

        final String empresaUpdate = "La Anonima";
        final String cargoUpdate = "Gestion";
        final String lugarUpdate = "Tolhuin";
        final LocalDate inicioUpdate = LocalDate.of(2010, 6, 1);
        final LocalDate finUpdate = LocalDate.of(2013, 1, 1);
        final var nuevosDatosTrabajoDto = new TrabajoDto(
                empresaUpdate,
                cargoUpdate,
                lugarUpdate,
                inicioUpdate,
                finUpdate
        );
        final var trabajoConDatosNuevos = Trabajo.builder()
                .cargo(cargoUpdate)
                .empresa(empresaUpdate)
                .desde(inicioUpdate)
                .hasta(finUpdate)
                .lugar(lugarUpdate)
                .persona(null)
                .id(idTrabajo)
                .build();

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.of(personaJere));
        BDDMockito.given(mapper.map(nuevosDatosTrabajoDto, Trabajo.class))
                .willReturn(trabajoConDatosNuevos);
        doAnswer((invocation) -> {
                    var trabajoDatosNuevos = (Trabajo) invocation.getArgument(0);
                    var trabajoDatosViejos = (Trabajo) invocation.getArgument(1);
                    trabajoDatosViejos.setEmpresa(trabajoDatosNuevos.getEmpresa());
                    trabajoDatosViejos.setLugar(trabajoDatosNuevos.getLugar());
                    trabajoDatosViejos.setCargo(trabajoDatosNuevos.getCargo());
                    trabajoDatosViejos.setDesde(trabajoDatosNuevos.getDesde());
                    trabajoDatosViejos.setHasta(trabajoDatosNuevos.getHasta());

                    return null;
                }
        ).when(mapper).map(trabajoConDatosNuevos, trabajoParaActualizar);
        BDDMockito.given(personaRepo.save(Mockito.any(Persona.class)))
                .willReturn(personaJere);
        //when
        final Persona updatedPersona = underTest.updateTrabajo(id, idTrabajo, nuevosDatosTrabajoDto);

        //then
        ArgumentCaptor<Long> idPersonaArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(personaRepo).findById(idPersonaArgumentCaptor.capture());
        final Long idCapturedArgumentValue = idPersonaArgumentCaptor.getValue();
        Assertions.assertThat(idCapturedArgumentValue).isEqualTo(id);

        Mockito.verify(mapper).map(nuevosDatosTrabajoDto, Trabajo.class);
        ArgumentCaptor<TrabajoDto> trabajoDtoArgumentCaptor = ArgumentCaptor.forClass(TrabajoDto.class);
        Mockito.verify(mapper).map(trabajoDtoArgumentCaptor.capture(), Mockito.eq(Trabajo.class));
        final TrabajoDto trabajoDtoCapturedArgumentValue = trabajoDtoArgumentCaptor.getValue();
        Assertions.assertThat(trabajoDtoCapturedArgumentValue).isEqualTo(nuevosDatosTrabajoDto);

        Mockito.verify(mapper).map(trabajoConDatosNuevos, trabajoParaActualizar);
        Assertions.assertThat(trabajoParaActualizar.getId()).isEqualTo(idTrabajo);
        Assertions.assertThat(trabajoParaActualizar.getPersona()).isEqualTo(personaJere);
        Assertions.assertThat(trabajoParaActualizar.getCargo()).isEqualTo(cargoUpdate);
        Assertions.assertThat(trabajoParaActualizar.getDesde()).isEqualTo(inicioUpdate);
        Assertions.assertThat(trabajoParaActualizar.getHasta()).isEqualTo(finUpdate);
        Assertions.assertThat(trabajoParaActualizar.getEmpresa()).isEqualTo(empresaUpdate);
        Assertions.assertThat(trabajoParaActualizar.getLugar()).isEqualTo(lugarUpdate);

        ArgumentCaptor<Persona> personaArgumentCaptor = ArgumentCaptor.forClass(Persona.class);
        Mockito.verify(personaRepo).save(personaArgumentCaptor.capture());
        final Persona capturedValuePersonaArgument = personaArgumentCaptor.getValue();
        Assertions.assertThat(capturedValuePersonaArgument).isEqualTo(personaJere);

        Assertions.assertThat(updatedPersona).isEqualTo(personaJere);
        final List<Trabajo> experienciasLaboralesDespuesActualizarTrabajo = updatedPersona.getExperienciasLaborales();
        Assertions.assertThat(experienciasLaboralesDespuesActualizarTrabajo)
                .isNotNull()
                .isEqualTo(experienciasLaborales)
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(trabajoParaActualizar);
    }

    @DisplayName("Actualizar un trabajo debe tirar error, cuando el id del trabajo es invalido")
    @Test
    void updateTrabajo_WhenTrabajoDoesNotExist_ShouldThrowTrabajoNotFoundException() {
        //given
        final Long id = 1L;
        final Long idTrabajo = 2L;
        final var trabajoDto = new TrabajoDto(
                "Carrefour",
                "Tester",
                "Rio Grande",
                LocalDate.of(2010, 1, 1),
                LocalDate.of(2012, 1, 1)
        );
        final Persona personaSinExperienciaLaboral = new Persona();
        personaSinExperienciaLaboral.setExperienciasLaborales(new ArrayList<>());

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.of(personaSinExperienciaLaboral));

        final String errorMsg = String.format("Trabajo id %d no encontrado.", idTrabajo);

        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.updateTrabajo(id, idTrabajo, trabajoDto))
                .isInstanceOf(TrabajoNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(mapper, times(1)).map(Mockito.any(), Mockito.any());
        Mockito.verify(personaRepo, Mockito.never()).save(Mockito.any());
    }

    @DisplayName("Actualizar un trabajo debe tirar error, cuando el id de la persona es invalido")
    @Test
    void updateTrabajo_WhenPersonaDoesNotExist_ShouldThrowPersonaNotFoundException() {
        //given
        final Long id = 1L;
        final Long idTrabajo = 1L;
        final var trabajoDto = new TrabajoDto(
                "Carrefour",
                "Tester",
                "Rio Grande",
                LocalDate.of(2010, 1, 1),
                LocalDate.of(2012, 1, 1)
        );
        final String errorMsg = String.format("Persona id %d no encontrada.", id);

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.empty());
        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.updateTrabajo(id, idTrabajo, trabajoDto))
                .isInstanceOf(PersonaNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(mapper, Mockito.never()).map(Mockito.any(), Mockito.any());
        Mockito.verify(personaRepo, Mockito.never()).save(Mockito.any());
    }

    @DisplayName("Debe eliminar un trabajo de la persona")
    @Test
    void removeTrabajo() {
        //given
        final Long id = 1L;
        final Long idTrabajo = 2L;

        String nombres = "Jeremías";
        String apellidos = "Calvet";
        Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        LocalDate fechaNacimiento = LocalDate.of(1990, 1, 1);
        final var personaJere = Persona.builder()
                .id(id)
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .nacionalidad(nacionalidad)
                .build();

        final var trabajoOk = Trabajo.builder()
                .id(1L)
                .cargo("La Anonima")
                .empresa("Tester")
                .desde(LocalDate.of(2008, 1, 1))
                .hasta(LocalDate.of(2009, 1, 1))
                .lugar("Rio Grande")
                .persona(personaJere)
                .build();

        final var trabajoParaEliminar = Trabajo.builder()
                .id(idTrabajo)
                .cargo("Carrrefour")
                .empresa("Tester")
                .desde(LocalDate.of(2010, 1, 1))
                .hasta(LocalDate.of(2012, 1, 1))
                .lugar("Rio Grande")
                .persona(personaJere)
                .build();

        List<Trabajo> experienciasLaborales = Stream.of(trabajoOk, trabajoParaEliminar)
                .collect(Collectors.toList());

        personaJere.setExperienciasLaborales(experienciasLaborales);

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.of(personaJere));
        BDDMockito.given(personaRepo.save(Mockito.any(Persona.class)))
                .willReturn(personaJere);

        //when
        underTest.removeTrabajo(id, idTrabajo);

        //then
        ArgumentCaptor<Long> idPersonaArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(personaRepo).findById(idPersonaArgumentCaptor.capture());
        final Long idCapturedArgumentValue = idPersonaArgumentCaptor.getValue();
        Assertions.assertThat(idCapturedArgumentValue).isEqualTo(id);

        ArgumentCaptor<Persona> personaArgumentCaptor = ArgumentCaptor.forClass(Persona.class);
        Mockito.verify(personaRepo).save(personaArgumentCaptor.capture());
        final Persona personaCapturedArgumentValue = personaArgumentCaptor.getValue();
        Assertions.assertThat(personaCapturedArgumentValue).isEqualTo(personaJere);

        Assertions.assertThat(personaJere.getId()).isEqualTo(id);
        Assertions.assertThat(personaJere.getNombres()).isEqualTo(nombres);
        Assertions.assertThat(personaJere.getApellidos()).isEqualTo(apellidos);
        Assertions.assertThat(personaJere.getNacionalidad()).isEqualTo(nacionalidad);
        Assertions.assertThat(personaJere.getFechaNacimiento()).isEqualTo(fechaNacimiento);

        final List<Trabajo> experienciasLaboralesDespuesEliminarTrabajo = personaJere.getExperienciasLaborales();
        Assertions.assertThat(experienciasLaboralesDespuesEliminarTrabajo)
                .isNotNull()
                .isEqualTo(experienciasLaborales)
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(trabajoOk)
                .doesNotContain(trabajoParaEliminar);
    }

    @DisplayName("Eliminar un trabajo debe tirar error, cuando el id de la persona es invalido")
    @Test
    void removeTrabajo_WhenPersonaDoesNotExist_ShouldThrowPersonaNotFoundException() {
        //given
        final Long id = 1L;
        final Long idTrabajo = 2L;
        final Persona persona = Persona.builder()
                .id(id)
                .build();
        List<Trabajo> experienciasLaborales = Stream.of(new Trabajo(), new Trabajo())
                .collect(Collectors.toList());

        final String errorMsg = String.format("Persona id %d no encontrada.", id);

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.empty());
        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.removeTrabajo(id, idTrabajo))
                .isInstanceOf(PersonaNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Assertions.assertThat(experienciasLaborales)
                .isNotEmpty()
                .hasSize(2);

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(personaRepo, Mockito.never()).save(persona);
    }

    @DisplayName("Eliminar un trabajo debe tirar error, cuando el id del trabajo es invalido")
    @Test
    void removeTrabajo_WhenTrabajoDoesNotExist_ShouldThrowTrabajoNotFoundException() {
        //given
        final Long id = 1L;
        final Long idTrabajo = 2L;
        final Persona persona = Persona.builder()
                .id(id)
                .nombres("Jeremias")
                .apellidos("Calvet")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .nacionalidad(Nacionalidades.ARGENTINA)
                .email("jere@gmail.com")
                .descripcion("Descripcion")
                .imagen("imagen")
                .ocupacion("tester")
                .experienciasLaborales(new ArrayList<>())
                .build();

        final String errorMsg = String.format("Trabajo id %d no encontrado.", idTrabajo);

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.of(persona));
        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.removeTrabajo(id, idTrabajo))
                .isInstanceOf(TrabajoNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Assertions.assertThat(persona.getExperienciasLaborales())
                .isNotNull()
                .isEmpty();

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(personaRepo, Mockito.never()).save(persona);
    }

    @DisplayName("Debe agregar un estudio a la persona")
    @Test
    void addEstudio() {
        //given
        final Long id = 1L;
        final String nombres = "Jeremías";
        final String apellidos = "Calvet";
        final LocalDate fechaNacimiento = LocalDate.of(1990, 1, 1);
        final Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        List<Educacion> estudios = new ArrayList<>();

        final var personaJere = Persona.builder()
                .id(id)
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .nacionalidad(nacionalidad)
                .build();
        personaJere.setEstudios(estudios);

        final String institucion = "Secundaria Comercio N° 15";
        final String titulo = "A.S.D. Comercio";
        final String lugar = "Rio Grande";
        final ProgresoEducacion progreso = ProgresoEducacion.CURSANDO;
        final var estudioDto = new EducacionDto(
                institucion,
                titulo,
                lugar,
                progreso
        );
        final var estudioParaAgregar = Educacion.builder()
                .estado(progreso)
                .titulo(titulo)
                .lugar(lugar)
                .institucion(institucion)
                .persona(null)
                .build();

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.of(personaJere));
        BDDMockito.given(mapper.map(estudioDto, Educacion.class))
                .willReturn(estudioParaAgregar);
        BDDMockito.given(personaRepo.save(Mockito.any(Persona.class)))
                .willReturn(personaJere);

        //when
        final Persona personaJereAfterNewStudy = underTest.addEstudio(id, estudioDto);

        //then
        ArgumentCaptor<Long> idArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(personaRepo).findById(idArgumentCaptor.capture());
        final Long idCapturedArgumentValue = idArgumentCaptor.getValue();
        Assertions.assertThat(idCapturedArgumentValue).isEqualTo(id);

        ArgumentCaptor<EducacionDto> estudioDtoArgumentCaptor = ArgumentCaptor.forClass(EducacionDto.class);
        Mockito.verify(mapper).map(estudioDtoArgumentCaptor.capture(), Mockito.eq(Educacion.class));
        final EducacionDto estudioDtoCapturedArgumentValue = estudioDtoArgumentCaptor.getValue();
        Assertions.assertThat(estudioDtoCapturedArgumentValue).isEqualTo(estudioDto);

        ArgumentCaptor<Persona> personaArgumentCaptor = ArgumentCaptor.forClass(Persona.class);
        Mockito.verify(personaRepo).save(personaArgumentCaptor.capture());
        final Persona personaCapturedArgumentValue = personaArgumentCaptor.getValue();
        Assertions.assertThat(personaCapturedArgumentValue).isEqualTo(personaJere);

        Assertions.assertThat(personaJereAfterNewStudy.getId()).isEqualTo(id);
        Assertions.assertThat(personaJereAfterNewStudy.getNombres()).isEqualTo(nombres);
        Assertions.assertThat(personaJereAfterNewStudy.getApellidos()).isEqualTo(apellidos);
        Assertions.assertThat(personaJereAfterNewStudy.getNacionalidad()).isEqualTo(nacionalidad);
        Assertions.assertThat(personaJereAfterNewStudy.getFechaNacimiento()).isEqualTo(fechaNacimiento);
        final List<Educacion> estudiosDespuesAgregarEstudio = personaJereAfterNewStudy.getEstudios();
        Assertions.assertThat(estudiosDespuesAgregarEstudio)
                .isNotNull()
                .isEqualTo(estudios)
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(estudioParaAgregar);

        Assertions.assertThat(estudioParaAgregar.getPersona())
                .isNotNull()
                .isEqualTo(personaJere);
    }

    @DisplayName("Agregar un estudio debe tirar error, cuando el id de la persona es invalido")
    @Test
    void addEstudio_WhenPersonaDoesNotExist_ShouldThrowPersonaNotFoundException() {
        //given
        final Long id = 1L;
        final var estudioDto = new EducacionDto(
                "Secundaria Comercio N° 15",
                "A.S.D. Comercio",
                "Rio Grande",
                ProgresoEducacion.CURSANDO
        );
        final String errorMsg = String.format("Persona id %d no encontrada.", id);

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.empty());

        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.addEstudio(id, estudioDto))
                .isInstanceOf(PersonaNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(mapper, Mockito.never()).map(Mockito.any(), Mockito.any());
        Mockito.verify(personaRepo, Mockito.never()).save(Mockito.any());
    }

    @DisplayName("Debe actualizar los datos de un estudio")
    @Test
    void updateEstudio() {
        //given
        final Long id = 1L;
        final Long idEstudio = 2L;
        final var personaJere = Persona.builder()
                .id(id)
                .nombres("Jeremías")
                .apellidos("Calvet")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .nacionalidad(Nacionalidades.ARGENTINA)
                .build();

        final var estudioParaActualizar = Educacion.builder()
                .id(idEstudio)
                .persona(personaJere)
                .lugar("Rio Grande")
                .titulo("A.S.D. Comercio")
                .institucion("Secundaria Comercio N° 15")
                .estado(ProgresoEducacion.CURSANDO)
                .build();

        List<Educacion> estudios = Stream.of(estudioParaActualizar)
                .collect(Collectors.toList());
        personaJere.setEstudios(estudios);

        final var lugarUpdate = "Río Grande";
        final var tituloUpdate = "A.S.D.F. Comercio";
        final var institucionUpdate = "Secundaria Comercios N° 15";
        final var progresoUpdate = ProgresoEducacion.COMPLETO;

        final var nuevosDatosEducacionDto = new EducacionDto(
                institucionUpdate,
                tituloUpdate,
                lugarUpdate,
                progresoUpdate
        );
        final var estudioConDatosNuevos = Educacion.builder()
                .lugar(lugarUpdate)
                .titulo(tituloUpdate)
                .institucion(institucionUpdate)
                .estado(progresoUpdate)
                .persona(null)
                .id(idEstudio)
                .build();

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.of(personaJere));
        BDDMockito.given(mapper.map(nuevosDatosEducacionDto, Educacion.class))
                .willReturn(estudioConDatosNuevos);
        doAnswer((invocation) -> {
                    var educacionDatosNuevos = (Educacion) invocation.getArgument(0);
                    var educacionDatosViejos = (Educacion) invocation.getArgument(1);
                    educacionDatosViejos.setInstitucion(educacionDatosNuevos.getInstitucion());
                    educacionDatosViejos.setTitulo(educacionDatosNuevos.getTitulo());
                    educacionDatosViejos.setLugar(educacionDatosNuevos.getLugar());
                    educacionDatosViejos.setEstado(educacionDatosNuevos.getEstado());

                    return null;
                }
        ).when(mapper).map(estudioConDatosNuevos, estudioParaActualizar);
        BDDMockito.given(personaRepo.save(Mockito.any(Persona.class)))
                .willReturn(personaJere);
        //when
        final Persona updatedPersona = underTest.updateEstudio(id, idEstudio, nuevosDatosEducacionDto);

        //then
        ArgumentCaptor<Long> idPersonaArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(personaRepo).findById(idPersonaArgumentCaptor.capture());
        final Long idCapturedArgumentValue = idPersonaArgumentCaptor.getValue();
        Assertions.assertThat(idCapturedArgumentValue).isEqualTo(id);

        Mockito.verify(mapper).map(nuevosDatosEducacionDto, Educacion.class);
        ArgumentCaptor<EducacionDto> educacionDtoArgumentCaptor = ArgumentCaptor.forClass(EducacionDto.class);
        Mockito.verify(mapper).map(educacionDtoArgumentCaptor.capture(), Mockito.eq(Educacion.class));
        final EducacionDto educacionDtoCapturedArgumentValue = educacionDtoArgumentCaptor.getValue();
        Assertions.assertThat(educacionDtoCapturedArgumentValue).isEqualTo(nuevosDatosEducacionDto);

        Mockito.verify(mapper).map(estudioConDatosNuevos, estudioParaActualizar);
        Assertions.assertThat(estudioParaActualizar.getId()).isEqualTo(idEstudio);
        Assertions.assertThat(estudioParaActualizar.getPersona()).isEqualTo(personaJere);
        Assertions.assertThat(estudioParaActualizar.getTitulo()).isEqualTo(tituloUpdate);
        Assertions.assertThat(estudioParaActualizar.getInstitucion()).isEqualTo(institucionUpdate);
        Assertions.assertThat(estudioParaActualizar.getEstado()).isEqualTo(progresoUpdate);
        Assertions.assertThat(estudioParaActualizar.getLugar()).isEqualTo(lugarUpdate);

        ArgumentCaptor<Persona> personaArgumentCaptor = ArgumentCaptor.forClass(Persona.class);
        Mockito.verify(personaRepo).save(personaArgumentCaptor.capture());
        final Persona capturedValuePersonaArgument = personaArgumentCaptor.getValue();
        Assertions.assertThat(capturedValuePersonaArgument).isEqualTo(personaJere);

        Assertions.assertThat(updatedPersona).isEqualTo(personaJere);
        final var estudiosDespuesActualizarEstudio = updatedPersona.getEstudios();
        Assertions.assertThat(estudiosDespuesActualizarEstudio)
                .isNotNull()
                .isEqualTo(estudios)
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(estudioParaActualizar);
    }

    @DisplayName("Actualizar un estudio debe tirar error, cuando el id del estudio es invalido")
    @Test
    void updateEstudio_WhenEstudioDoesNotExist_ShouldThrowEducacionNotFoundException() {
        //given
        final Long id = 1L;
        final Long idEstudio = 2L;
        final var educacionDto = new EducacionDto(
                "A.S.D.F. Comercio",
                "Secundaria Comercios N° 15",
                "Río Grande",
                ProgresoEducacion.COMPLETO
        );
        final Persona personaSinEstudios = new Persona();
        personaSinEstudios.setEstudios(new ArrayList<>());

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.of(personaSinEstudios));

        final String errorMsg = String.format("Estudio id %d no encontrado.", idEstudio);

        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.updateEstudio(id, idEstudio, educacionDto))
                .isInstanceOf(EducacionNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(mapper, times(1)).map(Mockito.any(), Mockito.any());
        Mockito.verify(personaRepo, Mockito.never()).save(Mockito.any());
    }

    @DisplayName("Actualizar un estudio debe tirar error, cuando el id de la persona es invalido")
    @Test
    void updateEstudio_WhenPersonaDoesNotExist_ShouldThrowPersonaNotFoundException() {
        //given
        final Long id = 1L;
        final Long idEstudio = 1L;
        final var educacionDto = new EducacionDto(
                "A.S.D.F. Comercio",
                "Secundaria Comercios N° 15",
                "Río Grande",
                ProgresoEducacion.COMPLETO
        );
        final String errorMsg = String.format("Persona id %d no encontrada.", id);

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.empty());
        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.updateEstudio(id, idEstudio, educacionDto))
                .isInstanceOf(PersonaNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(mapper, Mockito.never()).map(Mockito.any(), Mockito.any());
        Mockito.verify(personaRepo, Mockito.never()).save(Mockito.any());
    }

    @DisplayName("Debe eliminar un estudio de la persona")
    @Test
    void removeEstudio() {
        //given
        final Long id = 1L;
        final Long idEstudio = 2L;

        String nombres = "Jeremías";
        String apellidos = "Calvet";
        Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        LocalDate fechaNacimiento = LocalDate.of(1990, 1, 1);
        final var personaJere = Persona.builder()
                .id(id)
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .nacionalidad(nacionalidad)
                .build();

        final var estudioOk = Educacion.builder()
                .id(1L)
                .persona(personaJere)
                .lugar("Rio Grande")
                .titulo("A.S.D.F. Comercio")
                .institucion("Secundaria Comercio N° 15")
                .estado(ProgresoEducacion.CURSANDO)
                .build();
        final var estudioParaEliminar = Educacion.builder()
                .id(idEstudio)
                .persona(personaJere)
                .lugar("Rio Grande")
                .titulo("A.S.D. Comercio")
                .institucion("Secundaria Comercio N° 15")
                .estado(ProgresoEducacion.COMPLETO)
                .build();
        List<Educacion> estudios = Stream.of(estudioOk, estudioParaEliminar)
                .collect(Collectors.toList());
        personaJere.setEstudios(estudios);

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.of(personaJere));
        BDDMockito.given(personaRepo.save(Mockito.any(Persona.class)))
                .willReturn(personaJere);

        //when
        underTest.removeEstudio(id, idEstudio);

        //then
        ArgumentCaptor<Long> idPersonaArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(personaRepo).findById(idPersonaArgumentCaptor.capture());
        final Long idCapturedArgumentValue = idPersonaArgumentCaptor.getValue();
        Assertions.assertThat(idCapturedArgumentValue).isEqualTo(id);

        ArgumentCaptor<Persona> personaArgumentCaptor = ArgumentCaptor.forClass(Persona.class);
        Mockito.verify(personaRepo).save(personaArgumentCaptor.capture());
        final Persona personaCapturedArgumentValue = personaArgumentCaptor.getValue();
        Assertions.assertThat(personaCapturedArgumentValue).isEqualTo(personaJere);

        Assertions.assertThat(personaJere.getId()).isEqualTo(id);
        Assertions.assertThat(personaJere.getNombres()).isEqualTo(nombres);
        Assertions.assertThat(personaJere.getApellidos()).isEqualTo(apellidos);
        Assertions.assertThat(personaJere.getNacionalidad()).isEqualTo(nacionalidad);
        Assertions.assertThat(personaJere.getFechaNacimiento()).isEqualTo(fechaNacimiento);

        final var estudiosDespuesEliminarEstudio = personaJere.getEstudios();
        Assertions.assertThat(estudiosDespuesEliminarEstudio)
                .isNotNull()
                .isEqualTo(estudios)
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(estudioOk)
                .doesNotContain(estudioParaEliminar);
    }

    @DisplayName("Eliminar un estudio debe tirar error, cuando el id de la persona es invalido")
    @Test
    void removeEstudio_WhenPersonaDoesNotExist_ShouldThrowPersonaNotFoundException() {
        //given
        final Long id = 1L;
        final Long idEstudio = 2L;
        final Persona persona = Persona.builder()
                .id(id)
                .build();
        var estudios = Stream.of(new Educacion(), new Educacion())
                .collect(Collectors.toList());

        final String errorMsg = String.format("Persona id %d no encontrada.", id);

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.empty());
        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.removeEstudio(id, idEstudio))
                .isInstanceOf(PersonaNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Assertions.assertThat(estudios)
                .isNotEmpty()
                .hasSize(2);

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(personaRepo, Mockito.never()).save(persona);
    }

    @DisplayName("Eliminar un estudio debe tirar error, cuando el id del estudio es invalido")
    @Test
    void removeEstudio_WhenEstudioDoesNotExist_ShouldThrowEducacionNotFoundException() {
        //given
        final Long id = 1L;
        final Long idEstudio = 2L;
        final Persona persona = Persona.builder()
                .id(id)
                .nombres("Jeremias")
                .apellidos("Calvet")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .nacionalidad(Nacionalidades.ARGENTINA)
                .email("jere@gmail.com")
                .descripcion("Descripcion")
                .imagen("imagen")
                .ocupacion("tester")
                .estudios(new ArrayList<>())
                .build();

        final String errorMsg = String.format("Estudio id %d no encontrado.", idEstudio);

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.of(persona));
        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.removeEstudio(id, idEstudio))
                .isInstanceOf(EducacionNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Assertions.assertThat(persona.getExperienciasLaborales())
                .isNotNull()
                .isEmpty();

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(personaRepo, Mockito.never()).save(persona);
    }

    @DisplayName("Debe agregar un proyecto a la persona")
    @Test
    void addProyecto() {
        //given
        final Long id = 1L;
        final String nombres = "Jeremías";
        final String apellidos = "Calvet";
        final LocalDate fechaNacimiento = LocalDate.of(1990, 1, 1);
        final Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        List<Proyecto> proyectos = new ArrayList<>();

        final var personaJere = Persona.builder()
                .id(id)
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .nacionalidad(nacionalidad)
                .build();
        personaJere.setProyectos(proyectos);

        final String nombreProyecto = "Calculadora cientifica";
        final String descripcionProyecto = "Una calculadora hecha en javascript";
        final var proyectoDto = new ProyectoDto(nombreProyecto, descripcionProyecto);

        final var proyectoParaAgregar = Proyecto.builder()
                .persona(null)
                .nombre(nombreProyecto)
                .descripcion(descripcionProyecto)
                .build();

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.of(personaJere));
        BDDMockito.given(mapper.map(proyectoDto, Proyecto.class))
                .willReturn(proyectoParaAgregar);
        BDDMockito.given(personaRepo.save(Mockito.any(Persona.class)))
                .willReturn(personaJere);

        //when
        final Persona personaJereAfterNewProject = underTest.addProyecto(id, proyectoDto);

        //then
        ArgumentCaptor<Long> idArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(personaRepo).findById(idArgumentCaptor.capture());
        final Long idCapturedArgumentValue = idArgumentCaptor.getValue();
        Assertions.assertThat(idCapturedArgumentValue).isEqualTo(id);

        ArgumentCaptor<ProyectoDto> proyectoDtoArgumentCaptor = ArgumentCaptor.forClass(ProyectoDto.class);
        Mockito.verify(mapper).map(proyectoDtoArgumentCaptor.capture(), Mockito.eq(Proyecto.class));
        final ProyectoDto proyectoDtoCapturedArgumentValue = proyectoDtoArgumentCaptor.getValue();
        Assertions.assertThat(proyectoDtoCapturedArgumentValue).isEqualTo(proyectoDto);

        ArgumentCaptor<Persona> personaArgumentCaptor = ArgumentCaptor.forClass(Persona.class);
        Mockito.verify(personaRepo).save(personaArgumentCaptor.capture());
        final Persona personaCapturedArgumentValue = personaArgumentCaptor.getValue();
        Assertions.assertThat(personaCapturedArgumentValue).isEqualTo(personaJere);

        Assertions.assertThat(personaJereAfterNewProject.getId()).isEqualTo(id);
        Assertions.assertThat(personaJereAfterNewProject.getNombres()).isEqualTo(nombres);
        Assertions.assertThat(personaJereAfterNewProject.getApellidos()).isEqualTo(apellidos);
        Assertions.assertThat(personaJereAfterNewProject.getNacionalidad()).isEqualTo(nacionalidad);
        Assertions.assertThat(personaJereAfterNewProject.getFechaNacimiento()).isEqualTo(fechaNacimiento);
        List<Proyecto> proyectosDespuesAgregarProyecto = personaJereAfterNewProject.getProyectos();
        Assertions.assertThat(proyectosDespuesAgregarProyecto)
                .isNotNull()
                .isEqualTo(proyectos)
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(proyectoParaAgregar);

        Assertions.assertThat(proyectoParaAgregar.getPersona())
                .isNotNull()
                .isEqualTo(personaJere);
    }

    @DisplayName("Agregar un proyecto debe tirar error, cuando el id de la persona es invalido")
    @Test
    void addProyecto_WhenPersonaDoesNotExist_ShouldThrowPersonaNotFoundException() {
        //given
        final Long id = 1L;
        final var proyectoDto = new ProyectoDto(
                "Calculadora cientifica",
                "Una calculadora hecha en javascript"
        );

        final String errorMsg = String.format("Persona id %d no encontrada.", id);

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.empty());

        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.addProyecto(id, proyectoDto))
                .isInstanceOf(PersonaNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(mapper, Mockito.never()).map(Mockito.any(), Mockito.any());
        Mockito.verify(personaRepo, Mockito.never()).save(Mockito.any());
    }

    @DisplayName("Debe actualizar los datos de un proyecto")
    @Test
    void updateProyecto() {
        //given
        final Long id = 1L;
        final Long idProyecto = 2L;
        final var personaJere = Persona.builder()
                .id(id)
                .nombres("Jeremías")
                .apellidos("Calvet")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .nacionalidad(Nacionalidades.ARGENTINA)
                .build();

        final var proyectoParaActualizar = Proyecto.builder()
                .nombre("Calculadora cientifica")
                .descripcion("Una calculadora hecha en javascript")
                .persona(personaJere)
                .id(idProyecto)
                .build();
        List<Proyecto> proyectos = Stream.of(proyectoParaActualizar)
                .collect(Collectors.toList());
        personaJere.setProyectos(proyectos);

        final String nombreProyectoUpdate = "Calculadora cientifica";
        final String descripcionProyectoUpdate = "Una calculadora hecha en javascript con test.";

        final var nuevosDatosProyectoDto = new ProyectoDto(
                nombreProyectoUpdate,
                descripcionProyectoUpdate
        );

        final var proyectoConDatosNuevos = Proyecto.builder()
                .persona(null)
                .id(idProyecto)
                .nombre(nombreProyectoUpdate)
                .descripcion(descripcionProyectoUpdate)
                .build();

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.of(personaJere));
        BDDMockito.given(mapper.map(nuevosDatosProyectoDto, Proyecto.class))
                .willReturn(proyectoConDatosNuevos);
        doAnswer((invocation) -> {
                    var proyectoDatosNuevos = (Proyecto) invocation.getArgument(0);
                    var proyectoDatosViejos = (Proyecto) invocation.getArgument(1);
                    proyectoDatosViejos.setNombre(proyectoDatosNuevos.getNombre());
                    proyectoDatosViejos.setDescripcion(proyectoDatosNuevos.getDescripcion());

                    return null;
                }
        ).when(mapper).map(proyectoConDatosNuevos, proyectoParaActualizar);
        BDDMockito.given(personaRepo.save(Mockito.any(Persona.class)))
                .willReturn(personaJere);

        //when
        final Persona updatedPersona = underTest.updateProyecto(id, idProyecto, nuevosDatosProyectoDto);

        //then
        ArgumentCaptor<Long> idPersonaArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(personaRepo).findById(idPersonaArgumentCaptor.capture());
        final Long idCapturedArgumentValue = idPersonaArgumentCaptor.getValue();
        Assertions.assertThat(idCapturedArgumentValue).isEqualTo(id);

        Mockito.verify(mapper).map(nuevosDatosProyectoDto, Proyecto.class);
        ArgumentCaptor<ProyectoDto> proyectoDtoArgumentCaptor = ArgumentCaptor.forClass(ProyectoDto.class);
        Mockito.verify(mapper).map(proyectoDtoArgumentCaptor.capture(), Mockito.eq(Proyecto.class));
        final ProyectoDto proyectoDtoCapturedArgumentValue = proyectoDtoArgumentCaptor.getValue();
        Assertions.assertThat(proyectoDtoCapturedArgumentValue).isEqualTo(nuevosDatosProyectoDto);

        Mockito.verify(mapper).map(proyectoConDatosNuevos, proyectoParaActualizar);
        Assertions.assertThat(proyectoParaActualizar.getId()).isEqualTo(idProyecto);
        Assertions.assertThat(proyectoParaActualizar.getPersona()).isEqualTo(personaJere);
        Assertions.assertThat(proyectoParaActualizar.getNombre()).isEqualTo(nombreProyectoUpdate);
        Assertions.assertThat(proyectoParaActualizar.getDescripcion()).isEqualTo(descripcionProyectoUpdate);

        ArgumentCaptor<Persona> personaArgumentCaptor = ArgumentCaptor.forClass(Persona.class);
        Mockito.verify(personaRepo).save(personaArgumentCaptor.capture());
        final Persona capturedValuePersonaArgument = personaArgumentCaptor.getValue();
        Assertions.assertThat(capturedValuePersonaArgument).isEqualTo(personaJere);

        Assertions.assertThat(updatedPersona).isEqualTo(personaJere);
        final List<Proyecto> proyectosDespuesActualizarProyecto = updatedPersona.getProyectos();
        Assertions.assertThat(proyectosDespuesActualizarProyecto)
                .isNotNull()
                .isEqualTo(proyectos)
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(proyectoParaActualizar);
    }

    @DisplayName("Actualizar un proyecto debe tirar error, cuando el id del proyecto es invalido")
    @Test
    void updateProyecto_WhenProyectoDoesNotExist_ShouldThrowProyectoNotFoundException() {
        //given
        final Long id = 1L;
        final Long idProyecto = 2L;
        final var proyectoDto = new ProyectoDto(
                "Calculadora cientifica",
                "Una calculadora hecha en javascript"
        );
        final Persona personaSinProyectos = new Persona();
        personaSinProyectos.setProyectos(new ArrayList<>());
        final String errorMsg = String.format("Proyecto id %d no encontrado.", idProyecto);

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.of(personaSinProyectos));

        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.updateProyecto(id, idProyecto, proyectoDto))
                .isInstanceOf(ProyectoNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(mapper, times(1)).map(Mockito.any(), Mockito.any());
        Mockito.verify(personaRepo, Mockito.never()).save(Mockito.any());
    }

    @DisplayName("Actualizar un proyecto debe tirar error, cuando el id de la persona es invalido")
    @Test
    void updateProyecto_WhenPersonaDoesNotExist_ShouldThrowPersonaNotFoundException() {
        //given
        final Long id = 1L;
        final Long idProyecto = 2L;
        final var proyectoDto = new ProyectoDto(
                "Calculadora cientifica",
                "Una calculadora hecha en javascript"
        );
        final String errorMsg = String.format("Persona id %d no encontrada.", id);

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.empty());

        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.updateProyecto(id, idProyecto, proyectoDto))
                .isInstanceOf(PersonaNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(mapper, Mockito.never()).map(Mockito.any(), Mockito.any());
        Mockito.verify(personaRepo, Mockito.never()).save(Mockito.any());
    }

    @DisplayName("Debe eliminar un proyecto de la persona")
    @Test
    void removeProyecto() {
        //given
        final Long id = 1L;
        final Long idProyecto = 2L;

        String nombres = "Jeremías";
        String apellidos = "Calvet";
        Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        LocalDate fechaNacimiento = LocalDate.of(1990, 1, 1);
        final var personaJere = Persona.builder()
                .id(id)
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .nacionalidad(nacionalidad)
                .build();

        final var proyectoOk = Proyecto.builder()
                .id(1L)
                .persona(personaJere)
                .nombre("Calculadora cientifica")
                .descripcion("Una calculadora hecha en javascript")
                .build();
        final var proyectoParaEliminar = Proyecto.builder()
                .id(idProyecto)
                .persona(personaJere)
                .nombre("Proyecto test 123")
                .descripcion("Esto es una descripcion de prueba")
                .build();
        List<Proyecto> proyectos = Stream.of(proyectoOk, proyectoParaEliminar)
                .collect(Collectors.toList());
        personaJere.setProyectos(proyectos);

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.of(personaJere));
        BDDMockito.given(personaRepo.save(Mockito.any(Persona.class)))
                .willReturn(personaJere);

        //when
        underTest.removeProyecto(id, idProyecto);

        //then
        ArgumentCaptor<Long> idPersonaArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(personaRepo).findById(idPersonaArgumentCaptor.capture());
        final Long idCapturedArgumentValue = idPersonaArgumentCaptor.getValue();
        Assertions.assertThat(idCapturedArgumentValue).isEqualTo(id);

        ArgumentCaptor<Persona> personaArgumentCaptor = ArgumentCaptor.forClass(Persona.class);
        Mockito.verify(personaRepo).save(personaArgumentCaptor.capture());
        final Persona personaCapturedArgumentValue = personaArgumentCaptor.getValue();
        Assertions.assertThat(personaCapturedArgumentValue).isEqualTo(personaJere);

        Assertions.assertThat(personaJere.getId()).isEqualTo(id);
        Assertions.assertThat(personaJere.getNombres()).isEqualTo(nombres);
        Assertions.assertThat(personaJere.getApellidos()).isEqualTo(apellidos);
        Assertions.assertThat(personaJere.getNacionalidad()).isEqualTo(nacionalidad);
        Assertions.assertThat(personaJere.getFechaNacimiento()).isEqualTo(fechaNacimiento);

        final var proyectosDespuesEliminarProyecto = personaJere.getProyectos();
        Assertions.assertThat(proyectosDespuesEliminarProyecto)
                .isNotNull()
                .isEqualTo(proyectos)
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(proyectoOk)
                .doesNotContain(proyectoParaEliminar);
    }

    @DisplayName("Eliminar un proyecto debe tirar error, cuando el id de la persona es invalido")
    @Test
    void removeProyecto_WhenPersonaDoesNotExist_ShouldThrowPersonaNotFoundException() {
        //given
        final Long id = 1L;
        final Long idProyecto = 2L;
        final Persona persona = Persona.builder()
                .id(id)
                .build();
        List<Proyecto> proyectos = Stream.of(new Proyecto(), new Proyecto())
                .collect(Collectors.toList());

        final String errorMsg = String.format("Persona id %d no encontrada.", id);

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.empty());
        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.removeProyecto(id, idProyecto))
                .isInstanceOf(PersonaNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Assertions.assertThat(proyectos)
                .isNotEmpty()
                .hasSize(2);

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(personaRepo, Mockito.never()).save(persona);
    }

    @DisplayName("Eliminar un proyecto debe tirar error, cuando el id del proyecto es invalido")
    @Test
    void removeProyecto_WhenProyectoDoesNotExist_ShouldThrowProyectoNotFoundException() {
        //given
        final Long id = 1L;
        final Long idProyecto = 2L;
        final Persona persona = Persona.builder()
                .id(id)
                .nombres("Jeremias")
                .apellidos("Calvet")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .nacionalidad(Nacionalidades.ARGENTINA)
                .email("jere@gmail.com")
                .descripcion("Descripcion")
                .imagen("imagen")
                .ocupacion("tester")
                .proyectos(new ArrayList<>())
                .build();

        final String errorMsg = String.format("Proyecto id %d no encontrado.", idProyecto);

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.of(persona));
        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.removeProyecto(id, idProyecto))
                .isInstanceOf(ProyectoNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Assertions.assertThat(persona.getProyectos())
                .isNotNull()
                .isEmpty();

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(personaRepo, Mockito.never()).save(persona);
    }

    @DisplayName("Debe agregar una habilidad a la persona")
    @Test
    void addHabilidad() {
        //given
        final Long id = 1L;
        final String nombres = "Jeremías";
        final String apellidos = "Calvet";
        final LocalDate fechaNacimiento = LocalDate.of(1990, 1, 1);
        final Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        List<Habilidad> habilidades = new ArrayList<>();

        final var personaJere = Persona.builder()
                .id(id)
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .nacionalidad(nacionalidad)
                .build();
        personaJere.setHabilidades(habilidades);

        final String nombre = "Pilotear aviones";
        final int nivel = 70;
        final String descripcion = "Estoy habilitado para pilotear avionetas, jets, aviones comerciales.";
        final var habilidadDto = new HabilidadDto(
                nombre,
                nivel,
                descripcion
        );
        final var habilidadParaAgregar = Habilidad.builder()
                .persona(null)
                .nombre(nombre)
                .descripcion(descripcion)
                .nivel(nivel)
                .build();

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.of(personaJere));
        BDDMockito.given(mapper.map(habilidadDto, Habilidad.class))
                .willReturn(habilidadParaAgregar);
        BDDMockito.given(personaRepo.save(Mockito.any(Persona.class)))
                .willReturn(personaJere);

        //when
        final Persona personaJereAfterNewSkill = underTest.addHabilidad(id, habilidadDto);

        //then
        ArgumentCaptor<Long> idArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(personaRepo).findById(idArgumentCaptor.capture());
        final Long idCapturedArgumentValue = idArgumentCaptor.getValue();
        Assertions.assertThat(idCapturedArgumentValue).isEqualTo(id);

        ArgumentCaptor<HabilidadDto> habilidadDtoArgumentCaptor = ArgumentCaptor.forClass(HabilidadDto.class);
        Mockito.verify(mapper).map(habilidadDtoArgumentCaptor.capture(), Mockito.eq(Habilidad.class));
        final HabilidadDto habilidadDtoCapturedArgumentValue = habilidadDtoArgumentCaptor.getValue();
        Assertions.assertThat(habilidadDtoCapturedArgumentValue).isEqualTo(habilidadDto);

        ArgumentCaptor<Persona> personaArgumentCaptor = ArgumentCaptor.forClass(Persona.class);
        Mockito.verify(personaRepo).save(personaArgumentCaptor.capture());
        final Persona personaCapturedArgumentValue = personaArgumentCaptor.getValue();
        Assertions.assertThat(personaCapturedArgumentValue).isEqualTo(personaJere);

        Assertions.assertThat(personaJereAfterNewSkill.getId()).isEqualTo(id);
        Assertions.assertThat(personaJereAfterNewSkill.getNombres()).isEqualTo(nombres);
        Assertions.assertThat(personaJereAfterNewSkill.getApellidos()).isEqualTo(apellidos);
        Assertions.assertThat(personaJereAfterNewSkill.getNacionalidad()).isEqualTo(nacionalidad);
        Assertions.assertThat(personaJereAfterNewSkill.getFechaNacimiento()).isEqualTo(fechaNacimiento);
        final var habilidadesDespuesAgregarHabilidad = personaJereAfterNewSkill.getHabilidades();
        Assertions.assertThat(habilidadesDespuesAgregarHabilidad)
                .isNotNull()
                .isEqualTo(habilidades)
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(habilidadParaAgregar);

        Assertions.assertThat(habilidadParaAgregar.getPersona())
                .isNotNull()
                .isEqualTo(personaJere);
    }

    @DisplayName("Agregar una habilidad debe tirar error, cuando el id de la persona es invalido")
    @Test
    void addHabilidad_WhenPersonaDoesNotExist_ShouldThrowPersonaNotFoundException() {
        //given
        final var habilidadDto = new HabilidadDto(
                "Pilotear aviones",
                70,
                "Estoy habilitado para pilotear avionetas, jets, aviones comerciales."
        );
        final String errorMsg = String.format("Persona id %d no encontrada.", 1L);

        BDDMockito.given(personaRepo.findById(1L))
                .willReturn(Optional.empty());

        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.addHabilidad(1L, habilidadDto))
                .isInstanceOf(PersonaNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Mockito.verify(personaRepo, times(1)).findById(1L);
        Mockito.verify(mapper, Mockito.never()).map(Mockito.any(), Mockito.any());
        Mockito.verify(personaRepo, Mockito.never()).save(Mockito.any());
    }

    @DisplayName("Debe actualizar los datos de una habilidad")
    @Test
    void updateHabilidad() {
        //given
        final Long id = 1L;
        final Long idHabilidad = 2L;
        final var personaJere = Persona.builder()
                .id(id)
                .nombres("Jeremías")
                .apellidos("Calvet")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .nacionalidad(Nacionalidades.ARGENTINA)
                .build();

        final var habilidadParaActualizar = Habilidad.builder()
                .id(idHabilidad)
                .nombre("Carnet para conducir bicicletas")
                .nivel(30)
                .descripcion("Bicicletas BMX y mountain bike")
                .persona(personaJere)
                .build();

        final var habilidades = Stream.of(habilidadParaActualizar)
                .collect(Collectors.toList());
        personaJere.setHabilidades(habilidades);

        final String nombreUpdate = "Pilotear aviones";
        final int nivelUpdate = 80;
        final String descripcionUpdate = "Estoy habilitado para pilotear avionetas, jets, aviones comerciales.";

        final var nuevosDatosHabilidadDto = new HabilidadDto(
                nombreUpdate,
                nivelUpdate,
                descripcionUpdate
        );
        final var habilidadConDatosNuevos = Habilidad.builder()
                .id(idHabilidad)
                .nombre(nombreUpdate)
                .descripcion(descripcionUpdate)
                .nivel(nivelUpdate)
                .persona(null)
                .build();

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.of(personaJere));
        BDDMockito.given(mapper.map(nuevosDatosHabilidadDto, Habilidad.class))
                .willReturn(habilidadConDatosNuevos);
        doAnswer((invocation) -> {
                    var habilidadDatosNuevos = (Habilidad) invocation.getArgument(0);
                    var habilidadDatosViejos = (Habilidad) invocation.getArgument(1);
                    habilidadDatosViejos.setNombre(habilidadDatosNuevos.getNombre());
                    habilidadDatosViejos.setDescripcion(habilidadDatosNuevos.getDescripcion());
                    habilidadDatosViejos.setNivel(habilidadDatosNuevos.getNivel());

                    return null;
                }
        ).when(mapper).map(habilidadConDatosNuevos, habilidadParaActualizar);
        BDDMockito.given(personaRepo.save(Mockito.any(Persona.class)))
                .willReturn(personaJere);
        //when
        final Persona updatedPersona = underTest.updateHabilidad(id, idHabilidad, nuevosDatosHabilidadDto);

        //then
        ArgumentCaptor<Long> idPersonaArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(personaRepo).findById(idPersonaArgumentCaptor.capture());
        final Long idCapturedArgumentValue = idPersonaArgumentCaptor.getValue();
        Assertions.assertThat(idCapturedArgumentValue).isEqualTo(id);

        Mockito.verify(mapper).map(nuevosDatosHabilidadDto, Habilidad.class);
        ArgumentCaptor<HabilidadDto> habilidadDtoArgumentCaptor = ArgumentCaptor.forClass(HabilidadDto.class);
        Mockito.verify(mapper).map(habilidadDtoArgumentCaptor.capture(), Mockito.eq(Habilidad.class));
        final HabilidadDto habilidadDtoCapturedArgumentValue = habilidadDtoArgumentCaptor.getValue();
        Assertions.assertThat(habilidadDtoCapturedArgumentValue).isEqualTo(nuevosDatosHabilidadDto);

        Mockito.verify(mapper).map(habilidadConDatosNuevos, habilidadParaActualizar);
        Assertions.assertThat(habilidadParaActualizar.getId()).isEqualTo(idHabilidad);
        Assertions.assertThat(habilidadParaActualizar.getPersona()).isEqualTo(personaJere);
        Assertions.assertThat(habilidadParaActualizar.getNombre()).isEqualTo(nombreUpdate);
        Assertions.assertThat(habilidadParaActualizar.getDescripcion()).isEqualTo(descripcionUpdate);
        Assertions.assertThat(habilidadParaActualizar.getNivel()).isEqualTo(nivelUpdate);

        ArgumentCaptor<Persona> personaArgumentCaptor = ArgumentCaptor.forClass(Persona.class);
        Mockito.verify(personaRepo).save(personaArgumentCaptor.capture());
        final Persona capturedValuePersonaArgument = personaArgumentCaptor.getValue();
        Assertions.assertThat(capturedValuePersonaArgument).isEqualTo(personaJere);

        Assertions.assertThat(updatedPersona).isEqualTo(personaJere);
        final var habilidadesDespuesActualizarHabilidad = updatedPersona.getHabilidades();
        Assertions.assertThat(habilidadesDespuesActualizarHabilidad)
                .isNotNull()
                .isEqualTo(habilidades)
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(habilidadParaActualizar);
    }

    @DisplayName("Actualizar una habilidad debe tirar error, cuando el id de la habilidad es invalido")
    @Test
    void updateHabilidad_WhenHabilidadDoesNotExist_ShouldThrowHabilidadNotFoundException() {
        //given
        final Long id = 1L;
        final Long idHabilidad = 2L;
        final var habilidadDto = new HabilidadDto(
                "Pilotear aviones",
                80,
                "Estoy habilitado para pilotear avionetas, jets, aviones comerciales."
        );

        final Persona personaSinHabilidades = new Persona();
        personaSinHabilidades.setHabilidades(new ArrayList<>());

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.of(personaSinHabilidades));

        final String errorMsg = String.format("Habilidad id %d no encontrado.", idHabilidad);

        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.updateHabilidad(id, idHabilidad, habilidadDto))
                .isInstanceOf(HabilidadNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(mapper, times(1)).map(Mockito.any(), Mockito.any());
        Mockito.verify(personaRepo, Mockito.never()).save(Mockito.any());
    }

    @DisplayName("Actualizar una habilidad debe tirar error, cuando el id de la persona es invalido")
    @Test
    void updateHabilidad_WhenPersonaDoesNotExist_ShouldThrowPersonaNotFoundException() {
        //given
        final Long id = 1L;
        final Long idHabilidad = 1L;
        final var habilidadDto = new HabilidadDto(
                "Pilotear aviones",
                80,
                "Estoy habilitado para pilotear avionetas, jets, aviones comerciales."
        );
        final String errorMsg = String.format("Persona id %d no encontrada.", id);

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.empty());
        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.updateHabilidad(id, idHabilidad, habilidadDto))
                .isInstanceOf(PersonaNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(mapper, Mockito.never()).map(Mockito.any(), Mockito.any());
        Mockito.verify(personaRepo, Mockito.never()).save(Mockito.any());
    }

    @DisplayName("Debe eliminar una habilidad de la persona")
    @Test
    void removeHabilidad() {
        //given
        final Long id = 1L;
        final Long idHabilidad = 2L;

        String nombres = "Jeremías";
        String apellidos = "Calvet";
        Nacionalidades nacionalidad = Nacionalidades.ARGENTINA;
        LocalDate fechaNacimiento = LocalDate.of(1990, 1, 1);
        final var personaJere = Persona.builder()
                .id(id)
                .nombres(nombres)
                .apellidos(apellidos)
                .fechaNacimiento(fechaNacimiento)
                .nacionalidad(nacionalidad)
                .build();

        final var habilidadOk = Habilidad.builder()
                .id(1L)
                .nombre("Pilotear aviones")
                .nivel(80)
                .descripcion("Estoy habilitado para pilotear avionetas, jets, aviones comerciales.")
                .persona(personaJere)
                .build();

        final var habilidadParaEliminar = Habilidad.builder()
                .id(idHabilidad)
                .nombre("Carnet para conducir bicicletas")
                .nivel(30)
                .descripcion("Bicicletas BMX y mountain bike")
                .persona(personaJere)
                .build();

        List<Habilidad> habilidades = Stream.of(habilidadOk, habilidadParaEliminar)
                .collect(Collectors.toList());
        personaJere.setHabilidades(habilidades);

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.of(personaJere));
        BDDMockito.given(personaRepo.save(Mockito.any(Persona.class)))
                .willReturn(personaJere);

        //when
        underTest.removeHabilidad(id, idHabilidad);

        //then
        ArgumentCaptor<Long> idPersonaArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(personaRepo).findById(idPersonaArgumentCaptor.capture());
        final Long idCapturedArgumentValue = idPersonaArgumentCaptor.getValue();
        Assertions.assertThat(idCapturedArgumentValue).isEqualTo(id);

        ArgumentCaptor<Persona> personaArgumentCaptor = ArgumentCaptor.forClass(Persona.class);
        Mockito.verify(personaRepo).save(personaArgumentCaptor.capture());
        final Persona personaCapturedArgumentValue = personaArgumentCaptor.getValue();
        Assertions.assertThat(personaCapturedArgumentValue).isEqualTo(personaJere);

        Assertions.assertThat(personaJere.getId()).isEqualTo(id);
        Assertions.assertThat(personaJere.getNombres()).isEqualTo(nombres);
        Assertions.assertThat(personaJere.getApellidos()).isEqualTo(apellidos);
        Assertions.assertThat(personaJere.getNacionalidad()).isEqualTo(nacionalidad);
        Assertions.assertThat(personaJere.getFechaNacimiento()).isEqualTo(fechaNacimiento);

        final var habilidadesDespuesEliminarHabilidad = personaJere.getHabilidades();
        Assertions.assertThat(habilidadesDespuesEliminarHabilidad)
                .isNotNull()
                .isEqualTo(habilidades)
                .isNotEmpty()
                .hasSize(1)
                .containsExactly(habilidadOk)
                .doesNotContain(habilidadParaEliminar);
    }

    @DisplayName("Eliminar una habilidad debe tirar error, cuando el id de la persona es invalido")
    @Test
    void removeHabilidad_WhenPersonaDoesNotExist_ShouldThrowPersonaNotFoundException() {
        //given
        final Long id = 1L;
        final Long idHabilidad = 2L;
        final Persona persona = Persona.builder()
                .id(id)
                .build();
        final var habilidades = Stream.of(new Habilidad(), new Habilidad())
                .collect(Collectors.toList());

        final String errorMsg = String.format("Persona id %d no encontrada.", id);

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.empty());
        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.removeHabilidad(id, idHabilidad))
                .isInstanceOf(PersonaNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Assertions.assertThat(habilidades)
                .isNotEmpty()
                .hasSize(2);

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(personaRepo, Mockito.never()).save(persona);
    }

    @DisplayName("Eliminar una habilidad debe tirar error, cuando el id de la habilidad es invalido")
    @Test
    void removeHabilidad_WhenHabilidadDoesNotExist_ShouldThrowHabilidadNotFoundException() {
        //given
        final Long id = 1L;
        final Long idHabilidad = 2L;
        final Persona persona = Persona.builder()
                .id(id)
                .nombres("Jeremias")
                .apellidos("Calvet")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .nacionalidad(Nacionalidades.ARGENTINA)
                .email("jere@gmail.com")
                .descripcion("Descripcion")
                .imagen("imagen")
                .ocupacion("tester")
                .habilidades(new ArrayList<>())
                .build();

        final String errorMsg = String.format("Habilidad id %d no encontrado.", idHabilidad);

        BDDMockito.given(personaRepo.findById(id))
                .willReturn(Optional.of(persona));
        //when
        //then
        Assertions.assertThatThrownBy(() -> underTest.removeHabilidad(id, idHabilidad))
                .isInstanceOf(HabilidadNotFoundException.class)
                .hasMessageContaining(errorMsg);

        Assertions.assertThat(persona.getHabilidades())
                .isNotNull()
                .isEmpty();

        Mockito.verify(personaRepo, times(1)).findById(id);
        Mockito.verify(personaRepo, Mockito.never()).save(persona);
    }
}