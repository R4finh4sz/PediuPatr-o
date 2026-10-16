package com.umc.pediupatrao.entity;
import java.math.BigDecimal;
@lombok.Data
public class ItemPedido {
 private String produtoId;
 private String nome;
 private int quantidade;
 private BigDecimal precoUnitario;
}
