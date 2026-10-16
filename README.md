# PediuPatrão

Sistema de pizzaria em Java 21, Spring Boot, Spring Security, Thymeleaf e MongoDB.

## Executar

É necessário ter Java 21 e uma instância MongoDB em execução. O projeto inclui Maven Wrapper.

Preencha o arquivo `.env` na raiz com sua URI em `MONGODB_URI`, o banco em `MONGODB_DATABASE` e a senha inicial em `ADMIN_PASSWORD`, sem aspas. O Spring Boot lê esse arquivo automaticamente e ele está ignorado pelo Git. Em outro checkout, copie `.env.example` para `.env`.

Execute na raiz do projeto, no PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Abra http://localhost:8080/login. Em um banco sem usuários, a aplicação cria o usuário `admin` com perfil ADMIN e a senha definida em `ADMIN_PASSWORD`. A senha não é redefinida nas próximas execuções. Entre como ADMIN e cadastre os usuários GERENTE e ATENDENTE em **Configurações → Usuários**.

A conexão usa o banco definido em `MONGODB_DATABASE` (por padrão, `pizzaria`). Para usar outra instância, informe sua URI em `MONGODB_URI`.

Para reutilizar um banco antigo, atribua o perfil ADMIN ao administrador existente diretamente no MongoDB antes de entrar. Usuários antigos com perfil USER ou sem perfil são autenticados como ATENDENTE; o ADMIN pode atualizar seus perfis pelo sistema. Pedidos antigos continuam disponíveis para consulta; preços, horários e responsáveis que não foram registrados não são inventados. A aplicação inicializa o campo de versão dos pedidos antigos para permitir o controle de concorrência.

## Requisitos implementados

| Perfil | Permissões |
| --- | --- |
| ADMIN | Administrar usuários e perfis, consultar pedidos e auditoria. |
| GERENTE | Consultar clientes, alterar status dos pedidos, aplicar descontos, cancelar pedidos e consultar auditoria. |
| ATENDENTE | Cadastrar, atualizar e excluir clientes, registrar pedidos e consultar seu andamento. |

A gestão de produtos existente foi mantida para os usuários autenticados. As permissões são verificadas no backend pelo Spring Security, incluindo chamadas diretas às APIs. ADMIN também recebe acesso negado ao tentar cancelar ou aplicar desconto.

- **RF01:** três perfis, autenticação com o perfil persistido, gestão de usuários restrita ao ADMIN e senhas criptografadas sem exposição nas respostas ou formulários.
- **RF02:** itens com quantidades e preços registrados, desconto percentual, valor total, responsável e entrada. Na criação, o servidor consulta os preços dos produtos e calcula o total, ignorando valores comerciais, status e responsáveis enviados pelo cliente.
- **RF03:** fluxo RECEBIDO → EM_PREPARACAO → PRONTO → SAIU_PARA_ENTREGA ou RETIRADO → FINALIZADO. Saída registra horário e responsável. Transições inválidas são rejeitadas.
- **RF04:** desconto entre 0% e 20% e cancelamento com justificativa, exclusivos do GERENTE e somente antes da saída. Pedidos cancelados preservam o histórico e não permitem novas alterações.
- **RF05:** auditoria persistida na coleção `auditoria`, com usuário, perfil, data/hora, operação, entidade, identificador e campos anteriores/novos. Inclui pedidos, status, descontos, cancelamentos e dados de clientes. A consulta é exclusiva de ADMIN e GERENTE, sem rotas para editar ou excluir eventos. Os eventos usam somente campos necessários e não incluem credenciais.

As regras ficam nos serviços; os controladores e formulários utilizam essas mesmas regras. Formulários POST incluem CSRF automaticamente pelo Thymeleaf. Chamadas de API que alteram dados também exigem sessão autenticada e token CSRF. Erros de regra retornam HTTP 400; operações sem permissão retornam 403. Pedidos possuem controle de versão para impedir atualizações concorrentes perdidas.

## APIs principais

| Método | Rota | Acesso |
| --- | --- | --- |
| GET | /api/pedidos | Três perfis |
| POST | /api/pedidos | ATENDENTE |
| PUT | /api/pedidos/{id}/status?status=PRONTO | GERENTE |
| PUT | /api/pedidos/{id}/desconto?percentual=20 | GERENTE |
| PUT | /api/pedidos/{id}/cancelamento?justificativa=Motivo | GERENTE |
| GET | /api/clientes | GERENTE e ATENDENTE |
| POST, PUT, DELETE | /api/clientes e /api/clientes/{id} | ATENDENTE |
| GET, POST, PUT, DELETE | /api/usuarios e /api/usuarios/{id} | ADMIN |
| GET | /api/auditoria | ADMIN e GERENTE |

Exemplo de corpo para registrar um pedido:

```json
{
  "clienteId": "id-do-cliente",
  "itens": [
    {"produtoId": "id-do-produto", "quantidade": 2}
  ]
}
```

## Testes

```powershell
.\mvnw.cmd test
```

Os testes verificam cálculo no servidor, transições e saída, limites de desconto, cancelamento, auditoria de telefone e endereço, perfis de autenticação, proteção contra atribuição indevida de perfis, respostas sem senhas, CSRF e bloqueio das APIs. Também verificam os formulários Thymeleaf. Usam repositórios simulados, sem acessar MongoDB externo.

## Demonstração individual em vídeo

Grave a tela com a aplicação executando:

1. Entre como ADMIN, cadastre GERENTE e ATENDENTE e mostre a consulta de pedidos.
2. Entre como ATENDENTE, cadastre um cliente, altere telefone/endereço e registre um pedido com produtos ativos.
3. Entre como GERENTE, aplique desconto, avance até PRONTO e registre saída e finalização. Mostre os horários e responsáveis.
4. Registre outro pedido e cancele como GERENTE com justificativa. Mostre que não é possível alterar o pedido cancelado.
5. Consulte a auditoria como GERENTE e ADMIN e mostre os valores anteriores e novos das operações.
6. Demonstre tentativas de acesso direto às APIs sem autorização, com CSRF válido, incluindo ADMIN tentando descontar e ATENDENTE tentando consultar auditoria.

Para demonstrar o bloqueio no navegador, entre como ADMIN, abra **Pedidos**, copie o token do formulário de saída e execute no console, substituindo o ID:

```javascript
const token = document.querySelector('input[name="_csrf"]').value;
const resposta = await fetch('/api/pedidos/ID_DO_PEDIDO/desconto?percentual=10', {
  method: 'PUT',
  headers: {'X-CSRF-TOKEN': token}
});
console.log(resposta.status);
```

O vídeo individual deve mostrar a execução real e ser entregue junto ao repositório.

