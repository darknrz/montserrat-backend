package com.monserrat.security;

import com.monserrat.entity.Admin;
import com.monserrat.entity.UsuarioAcademico;
import com.monserrat.repository.AdminRepository;
import com.monserrat.repository.UsuarioAcademicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final UsuarioAcademicoRepository usuarioAcademicoRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return adminRepository.findByUsername(username)
                .map(admin -> buildAdminUser(username, admin))
                .or(() -> usuarioAcademicoRepository.findByDni(username)
                        .map(this::buildAcademicUser))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    private UserDetails buildAdminUser(String username, Admin admin) {
        if (!admin.getActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + username);
        }

        return User.builder()
                .username(admin.getUsername())
                .password(admin.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + admin.getRol())))
                .build();
    }

    private UserDetails buildAcademicUser(UsuarioAcademico usuario) {
        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + usuario.getDni());
        }

        return User.builder()
                .username(usuario.getDni())
                .password(usuario.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol())))
                .build();
    }
}
