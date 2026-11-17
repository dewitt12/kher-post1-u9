package com.ufps.seguridadweb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ufps.seguridadweb.model.Comentario;
import com.ufps.seguridadweb.model.Rol;
import com.ufps.seguridadweb.model.Usuario;
import com.ufps.seguridadweb.repository.ComentarioRepository;
import com.ufps.seguridadweb.repository.UsuarioRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner cargarDatos(UsuarioRepository usuarios,
                                  ComentarioRepository comentarios,
                                  PasswordEncoder encoder,
                                  @Value("${app.seed.admin-password}") String adminPassword,
                                  @Value("${app.seed.user-password}") String userPassword) {
        return args -> {
            // Las contrasenas se guardan SIEMPRE como hash BCrypt.
            usuarios.save(new Usuario("admin", encoder.encode(adminPassword), Rol.ADMIN));
            usuarios.save(new Usuario("juan", encoder.encode(userPassword), Rol.USER));
            comentarios.save(new Comentario("admin", "Bienvenidos al laboratorio de seguridad web."));
        };
    }
}
