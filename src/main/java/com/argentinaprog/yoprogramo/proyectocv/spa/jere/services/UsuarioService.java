package com.argentinaprog.yoprogramo.proyectocv.spa.jere.services;

import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.Usuario;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.model.dto.LoginRequestDto;
import com.argentinaprog.yoprogramo.proyectocv.spa.jere.repositories.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private final UsuarioRepository usuarioRepo;

    @Autowired
    private final PasswordEncoder bcryptEncoder;

    @Autowired
    private final ModelMapper mapper;

    public UsuarioService(UsuarioRepository usuarioRepo, PasswordEncoder bcryptEncoder, ModelMapper mapper) {
        this.usuarioRepo = usuarioRepo;
        this.bcryptEncoder = bcryptEncoder;
        this.mapper = mapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    public Usuario save(LoginRequestDto loginRequestDto) {
        final Usuario usuario = mapper.map(loginRequestDto, Usuario.class);
        usuario.setPassword(bcryptEncoder.encode(usuario.getPassword()));
        return usuarioRepo.save(usuario);
    }
}
