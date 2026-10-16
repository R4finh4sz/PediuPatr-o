package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final UsuarioService service = new UsuarioService(usuarios, encoder);

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "GERENTE", "ATENDENTE"})
    void autenticacaoUsaPerfilPersistido(String perfil) {
        Usuario usuario = new Usuario();
        usuario.setUsername("teste");
        usuario.setPassword(encoder.encode("senha"));
        usuario.setRole(perfil);
        when(usuarios.findByUsername("teste")).thenReturn(Optional.of(usuario));
        var detalhes = new UsuarioDetailsService(usuarios).loadUserByUsername("teste");
        assertTrue(detalhes.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + perfil)));
    }

    @Test
    void atualizacaoSemSenhaMantemHashExistente() {
        Usuario atual = new Usuario();
        atual.setId("u1");
        atual.setPassword(encoder.encode("senha"));
        String hash = atual.getPassword();
        when(usuarios.findById("u1")).thenReturn(Optional.of(atual));
        Usuario entrada = new Usuario();
        entrada.setUsername("teste");
        entrada.setRole("GERENTE");
        entrada.setPassword("");
        service.atualizarUsuario("u1", entrada);
        assertEquals(hash, atual.getPassword());
        assertEquals("GERENTE", atual.getRole());
    }

    @Test
    void criacaoRejeitaPerfilInvalido() {
        Usuario entrada = new Usuario();
        entrada.setUsername("teste");
        entrada.setRole("USER");
        entrada.setPassword("senha");
        assertThrows(IllegalArgumentException.class, () -> service.salvarUsuario(entrada));
        verify(usuarios, never()).save(any());
    }
}
