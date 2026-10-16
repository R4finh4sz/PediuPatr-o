package com.umc.pediupatrao.config;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository repo,
            @org.springframework.beans.factory.annotation.Value("${app.admin-password:}") String senha) {
        return args -> {

            if (repo.count() == 0) {
                if (senha.isBlank()) {
                    throw new IllegalStateException("Defina ADMIN_PASSWORD para criar o administrador inicial.");
                }

                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

                Usuario user = new Usuario();
                user.setUsername("admin");
                user.setRole("ADMIN");
                user.setPassword(encoder.encode(senha));

                repo.save(user);

                System.out.println("Usuário admin criado!");
            }
        };
    }
}
