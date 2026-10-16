package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.UsuarioRepository;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    public Optional<Usuario> buscarPorId(String id) {
        return usuarios.findById(id);
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarios.findByUsername(username);
    }

    public Iterable<Usuario> listarTodos() {
        return usuarios.findAll();
    }

    public void salvarUsuario(Usuario entrada) {
        validar(entrada, null);
        if (entrada.getPassword() == null || entrada.getPassword().isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória.");
        }
        Usuario novo = new Usuario();
        novo.setUsername(entrada.getUsername());
        novo.setRole(entrada.getRole());
        novo.setPassword(encoder.encode(entrada.getPassword()));
        Usuario salvo = usuarios.save(novo);
        entrada.setId(salvo.getId());
    }

    public void atualizarUsuario(String id, Usuario entrada) {
        Usuario atual = usuarios.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        validar(entrada, id);
        atual.setUsername(entrada.getUsername());
        atual.setRole(entrada.getRole());
        if (entrada.getPassword() != null && !entrada.getPassword().isBlank()) {
            atual.setPassword(encoder.encode(entrada.getPassword()));
        }
        usuarios.save(atual);
    }

    public void deletarUsuario(String id) {
        usuarios.deleteById(id);
    }

    private void validar(Usuario usuario, String id) {
        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
            throw new IllegalArgumentException("Nome de usuário é obrigatório.");
        }
        if (usuario.getRole() == null || !Set.of("ADMIN", "GERENTE", "ATENDENTE").contains(usuario.getRole())) {
            throw new IllegalArgumentException("Perfil inválido.");
        }
        usuarios.findByUsername(usuario.getUsername()).ifPresent(existente -> {
            if (!Objects.equals(existente.getId(), id)) {
                throw new IllegalArgumentException("Nome de usuário já cadastrado.");
            }
        });
    }
}
