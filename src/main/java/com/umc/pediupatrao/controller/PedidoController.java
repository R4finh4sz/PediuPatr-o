package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    public Pedido criarPedido(@RequestBody Pedido pedido) {
        return pedidoService.criarPedido(pedido);
    }

    @GetMapping
    public List<Pedido> listarPedidos() {
        return pedidoService.listarPedidos();
    }

    @PutMapping("/{id}/status")
    public Pedido atualizarStatus(@PathVariable String id, @RequestParam String status) {
        return pedidoService.atualizarStatus(id, status);
    }
    @PutMapping("/{id}/desconto")
    public Pedido aplicarDesconto(@PathVariable String id, @RequestParam java.math.BigDecimal percentual) {
        return pedidoService.aplicarDesconto(id, percentual);
    }

    @PutMapping("/{id}/cancelamento")
    public Pedido cancelar(@PathVariable String id, @RequestParam String justificativa) {
        return pedidoService.cancelar(id, justificativa);
    }
}
