package com.umc.pediupatrao.service;
import com.umc.pediupatrao.entity.*;
import com.umc.pediupatrao.repository.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class PedidoServiceTest {
 PedidoRepository pedidos=mock(PedidoRepository.class);
 ProdutoRepository produtos=mock(ProdutoRepository.class);
 ClienteRepository clientes=mock(ClienteRepository.class);
 AuditoriaService auditoria=mock(AuditoriaService.class);
 PedidoService service=new PedidoService(pedidos,produtos,clientes,auditoria);
 @BeforeEach void autenticar() {
  SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("funcionario","",List.of(new SimpleGrantedAuthority("ROLE_GERENTE"))));
  when(pedidos.save(any())).thenAnswer(i->{ Pedido p=i.getArgument(0); p.setId("p1"); return p; });
 }
 @AfterEach void limpar() { SecurityContextHolder.clearContext(); }
 Pedido recebido() {
  Pedido p=new Pedido(); p.setId("p1"); p.setStatus("RECEBIDO");
  ItemPedido item=new ItemPedido(); item.setProdutoId("produto"); item.setNome("Pizza");
  item.setQuantidade(2); item.setPrecoUnitario(new BigDecimal("35.50")); p.setItens(List.of(item));
  p.setValorTotal(new BigDecimal("71.00")); when(pedidos.findById("p1")).thenReturn(Optional.of(p)); return p;
 }
 @Test void criacaoIgnoraValoresEStatusEnviadosPeloCliente() {
  when(clientes.existsById("c1")).thenReturn(true);
  Produto produto=new Produto(); produto.setId("produto"); produto.setNome("Pizza"); produto.setPreco(35.5); produto.setAtivo(true);
  when(produtos.findById("produto")).thenReturn(Optional.of(produto));
  Pedido entrada=recebido(); entrada.setClienteId("c1"); entrada.setStatus("FINALIZADO");
  entrada.setValorTotal(BigDecimal.ONE); entrada.setDescontoPercentual(new BigDecimal("99")); entrada.setResponsavelRegistro("outra pessoa");
  entrada.getItens().get(0).setPrecoUnitario(BigDecimal.ONE);
  Pedido salvo=service.criarPedido(entrada);
  assertEquals("RECEBIDO",salvo.getStatus()); assertEquals(new BigDecimal("71.00"),salvo.getValorTotal());
  assertEquals(BigDecimal.ZERO,salvo.getDescontoPercentual()); assertEquals("funcionario",salvo.getResponsavelRegistro());
  assertNotNull(salvo.getEntrada()); assertNull(salvo.getSaida());
  verify(auditoria).registrar(eq("INCLUSAO_PEDIDO"),eq("Pedido"),eq("p1"),eq(Map.of()),anyMap(),isNull());
 }
 @Test void fluxoCompletoRegistraSaida() {
  Pedido p=recebido();
  service.atualizarStatus("p1","EM_PREPARACAO"); service.atualizarStatus("p1","PRONTO");
  service.atualizarStatus("p1","SAIU_PARA_ENTREGA"); assertNotNull(p.getSaida()); assertEquals("funcionario",p.getResponsavelSaida());
  service.atualizarStatus("p1","FINALIZADO"); assertEquals("FINALIZADO",p.getStatus());
  assertThrows(IllegalArgumentException.class,()->service.atualizarStatus("p1","RECEBIDO"));
  verify(auditoria,times(4)).registrar(eq("ALTERACAO_STATUS"),eq("Pedido"),eq("p1"),anyMap(),anyMap(),isNull());
 }
 @Test void retiradaTambemRegistraSaida() {
  Pedido p=recebido(); p.setStatus("PRONTO"); service.atualizarStatus("p1","RETIRADO");
  assertNotNull(p.getSaida()); assertEquals("funcionario",p.getResponsavelSaida());
 }
 @Test void naoPermitePularEtapasOuCancelarPeloStatus() {
  recebido();
  assertThrows(IllegalArgumentException.class,()->service.atualizarStatus("p1","FINALIZADO"));
  assertThrows(IllegalArgumentException.class,()->service.atualizarStatus("p1","CANCELADO")); verify(pedidos,never()).save(any());
 }
 @Test void descontoCalculaERegistraAntesEDepois() {
  recebido(); Pedido p=service.aplicarDesconto("p1",new BigDecimal("20"));
  assertEquals(new BigDecimal("56.80"),p.getValorTotal());
  ArgumentCaptor<Map<String,Object>> antes=ArgumentCaptor.forClass(Map.class),depois=ArgumentCaptor.forClass(Map.class);
  verify(auditoria).registrar(eq("DESCONTO_PEDIDO"),eq("Pedido"),eq("p1"),antes.capture(),depois.capture(),isNull());
  assertEquals(new BigDecimal("71.00"),antes.getValue().get("valorTotal")); assertEquals(new BigDecimal("56.80"),depois.getValue().get("valorTotal"));
 }
 @ParameterizedTest @ValueSource(strings={"-1","20.01","100"})
 void rejeitaDescontoForaDoLimite(String percentual) {
  recebido(); assertThrows(IllegalArgumentException.class,()->service.aplicarDesconto("p1",new BigDecimal(percentual)));
  verify(pedidos,never()).save(any());
 }
 @ParameterizedTest @ValueSource(strings={"SAIU_PARA_ENTREGA","RETIRADO","FINALIZADO","CANCELADO"})
 void bloqueiaDescontoECancelamentoAposSaidaOuEncerramento(String status) {
  Pedido p=recebido(); p.setStatus(status);
  assertThrows(IllegalArgumentException.class,()->service.aplicarDesconto("p1",BigDecimal.TEN));
  assertThrows(IllegalArgumentException.class,()->service.cancelar("p1","Motivo")); verify(pedidos,never()).save(any());
 }
 @Test void cancelamentoExigeMotivoEBloqueiaMudancas() {
  Pedido p=recebido(); assertThrows(IllegalArgumentException.class,()->service.cancelar("p1"," "));
  service.cancelar("p1","Cliente desistiu");
  assertEquals("CANCELADO",p.getStatus()); assertEquals("Cliente desistiu",p.getJustificativaCancelamento());
  assertThrows(IllegalArgumentException.class,()->service.atualizarStatus("p1","EM_PREPARACAO"));
  assertThrows(IllegalArgumentException.class,()->service.aplicarDesconto("p1",BigDecimal.TEN));
  verify(auditoria).registrar(eq("CANCELAMENTO_PEDIDO"),eq("Pedido"),eq("p1"),anyMap(),anyMap(),eq("Cliente desistiu"));
 }
 @Test void quantidadeInvalidaNaoPersistePedido() {
  when(clientes.existsById("c1")).thenReturn(true);
  Pedido p=recebido(); p.setClienteId("c1"); p.getItens().get(0).setQuantidade(0);
  assertThrows(IllegalArgumentException.class,()->service.criarPedido(p)); verify(pedidos,never()).save(any());
 }
}
