package com.umc.pediupatrao.repository;

import com.umc.pediupatrao.entity.Auditoria;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditoriaMongoTest {
    private MappingMongoConverter conversor() throws Exception {
        MongoCustomConversions conversoes = new MongoCustomConversions(List.of());
        MongoMappingContext contexto = new MongoMappingContext();
        contexto.setSimpleTypeHolder(conversoes.getSimpleTypeHolder());
        contexto.afterPropertiesSet();
        MappingMongoConverter conversor = new MappingMongoConverter(NoOpDbRefResolver.INSTANCE, contexto);
        conversor.setCustomConversions(conversoes);
        conversor.afterPropertiesSet();
        return conversor;
    }

    @Test
    void auditoriaPodeSerConvertidaParaMongoERecuperada() throws Exception {
        Auditoria evento = new Auditoria();
        evento.setUsuario("gerente");
        evento.setPerfil("GERENTE");
        evento.setDataHora(Instant.now());
        evento.setEntidade("Pedido");
        evento.setEntidadeId("p1");
        evento.setCampos(Map.of(
                "logradouro", new Auditoria.Alteracao("Rua antiga", "Rua nova"),
                "valorTotal", new Auditoria.Alteracao(new BigDecimal("50.00"), new BigDecimal("40.00"))));
        MappingMongoConverter conversor = conversor();
        Document documento = new Document();
        conversor.write(evento, documento);
        Auditoria recuperado = conversor.read(Auditoria.class, documento);
        assertEquals(evento.getUsuario(), recuperado.getUsuario());
        assertEquals(evento.getCampos().keySet(), recuperado.getCampos().keySet());
        assertEquals(evento.getCampos().get("logradouro"), recuperado.getCampos().get("logradouro"));
        // Valores decimais em campos genéricos são armazenados como texto pelo conversor Mongo.
        assertEquals("50.00", recuperado.getCampos().get("valorTotal").anterior().toString());
        assertEquals("40.00", recuperado.getCampos().get("valorTotal").novo().toString());
    }

    @Test
    void repositorioInsereEventoSemExporAtualizacaoOuExclusao() throws Exception {
        MongoOperations mongo = mock(MongoOperations.class);
        when(mongo.getConverter()).thenReturn(conversor());
        when(mongo.getCollectionName(Auditoria.class)).thenReturn("auditoria");
        AuditoriaRepository repository = new MongoRepositoryFactory(mongo).getRepository(AuditoriaRepository.class);
        Auditoria evento = new Auditoria();
        repository.insert(evento);
        verify(mongo).insert(evento, "auditoria");
    }
}
