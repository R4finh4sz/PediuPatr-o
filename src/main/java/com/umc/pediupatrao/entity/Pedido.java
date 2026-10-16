package com.umc.pediupatrao.entity;

import jakarta.persistence.Entity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pedidos")
@Entity
public class Pedido {
    @lombok.Getter @lombok.Setter private java.util.List<ItemPedido> itens = new java.util.ArrayList<>();
    @lombok.Getter @lombok.Setter private java.math.BigDecimal descontoPercentual = java.math.BigDecimal.ZERO;
    @lombok.Getter @lombok.Setter private java.math.BigDecimal valorTotal;
    @lombok.Getter @lombok.Setter private java.time.Instant entrada;
    @lombok.Getter @lombok.Setter private java.time.Instant saida;
    @lombok.Getter @lombok.Setter private String responsavelRegistro;
    @lombok.Getter @lombok.Setter private String responsavelSaida;
    @lombok.Getter @lombok.Setter private String justificativaCancelamento;
    @org.springframework.data.annotation.Version
    @lombok.Getter @lombok.Setter private Long versao;
    @Id
    private String id;
    private String clienteId;
    private String tipoPizza;
    private String status;
    private int quantidade;
    private String sabor;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getTipoPizza() {
        return tipoPizza;
    }

    public void setTipoPizza(String tipoPizza) {
        this.tipoPizza = tipoPizza;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public String getSabor() {
        return sabor;
    }

    public void setSabor(String sabor) {
        this.sabor = sabor;
    }
    
    
}
