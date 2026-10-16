package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.repository.AuditoriaRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {
    private final AuditoriaRepository repository;

    public AuditoriaService(AuditoriaRepository repository) {
        this.repository = repository;
    }

    public void registrar(String operacao, String entidade, String id,
            Map<String, Object> antes, Map<String, Object> depois, String justificativa) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var campos = new LinkedHashMap<String, Auditoria.Alteracao>();
        var nomes = new LinkedHashSet<>(antes.keySet());
        nomes.addAll(depois.keySet());
        for (String nome : nomes) {
            if (!Objects.equals(antes.get(nome), depois.get(nome))) {
                campos.put(nome, new Auditoria.Alteracao(antes.get(nome), depois.get(nome)));
            }
        }
        Auditoria evento = new Auditoria();
        evento.setUsuario(auth.getName());
        evento.setPerfil(auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));
        evento.setDataHora(Instant.now());
        evento.setOperacao(operacao);
        evento.setEntidade(entidade);
        evento.setEntidadeId(id);
        evento.setCampos(campos);
        evento.setJustificativa(justificativa);
        repository.insert(evento);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public List<Auditoria> listar() {
        return repository.findAllByOrderByDataHoraDesc();
    }
}
