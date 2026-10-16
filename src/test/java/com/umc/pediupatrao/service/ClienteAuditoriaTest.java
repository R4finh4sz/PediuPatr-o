package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.*;
import com.umc.pediupatrao.repository.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClienteAuditoriaTest {
    private final ClienteRepository clientes = mock(ClienteRepository.class);
    private final AuditoriaRepository eventos = mock(AuditoriaRepository.class);
    private final ClienteService service = new ClienteService(clientes, new AuditoriaService(eventos));

    @BeforeEach
    void autenticar() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "atendente01", "", List.of(new SimpleGrantedAuthority("ROLE_ATENDENTE"))));
        when(clientes.save(any())).thenAnswer(invocacao -> {
            Cliente cliente = invocacao.getArgument(0);
            cliente.setId("c1");
            return cliente;
        });
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void alteracaoRegistraTelefoneEEnderecoComIdentificacao() {
        Cliente anterior = new Cliente();
        anterior.setId("c1");
        anterior.setNome("Maria");
        anterior.setTelefone("1111");
        anterior.setLogradouro("Rua antiga");
        when(clientes.findById("c1")).thenReturn(Optional.of(anterior));

        Cliente novo = new Cliente();
        novo.setId("c1");
        novo.setNome("Maria");
        novo.setTelefone("2222");
        novo.setLogradouro("Rua nova");
        service.salvar(novo);

        ArgumentCaptor<Auditoria> captor = ArgumentCaptor.forClass(Auditoria.class);
        verify(eventos).insert(captor.capture());
        Auditoria evento = captor.getValue();
        assertEquals("atendente01", evento.getUsuario());
        assertEquals("ATENDENTE", evento.getPerfil());
        assertEquals("ALTERACAO_CLIENTE", evento.getOperacao());
        assertEquals("Cliente", evento.getEntidade());
        assertEquals("c1", evento.getEntidadeId());
        assertNotNull(evento.getDataHora());
        assertEquals(new Auditoria.Alteracao("1111", "2222"), evento.getCampos().get("telefone"));
        assertEquals(new Auditoria.Alteracao("Rua antiga", "Rua nova"), evento.getCampos().get("logradouro"));
        assertFalse(evento.getCampos().containsKey("nome"));
        assertEquals(Set.of("telefone", "logradouro"), evento.getCampos().keySet());
    }

    @Test
    void inclusaoTambemPersisteAuditoria() {
        Cliente cliente = new Cliente();
        cliente.setNome("Maria");
        cliente.setTelefone("1111");
        service.salvar(cliente);
        verify(eventos).insert(argThat(evento ->
                evento.getOperacao().equals("INCLUSAO_CLIENTE")
                && evento.getCampos().get("telefone").anterior() == null
                && evento.getCampos().get("telefone").novo().equals("1111")));
    }
}
