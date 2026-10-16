package com.umc.pediupatrao.entity;
import java.time.Instant;
import java.util.Map;
@lombok.Data
@org.springframework.data.mongodb.core.mapping.Document(collection="auditoria")
public class Auditoria {
 @org.springframework.data.annotation.Id private String id;
 private String usuario;
 private String perfil;
 private Instant dataHora;
 private String operacao;
 private String entidade;
 private String entidadeId;
 private Map<String, Alteracao> campos;
 private String justificativa;
 public record Alteracao(Object anterior, Object novo) {}
}
