# ADR - Architecture Decision Record

![V1.2.0](https://img.shields.io/badge/V1.2.0-gray?style=for-the-badge)

_Registro das decisões arquiteturais tomadas para o AutoFlow, com contexto, decisão e consequências observadas no projeto._

---

## 🗄️ Seções do Documento

| Seção | Subseções |
| --- | --- |
| [🎯 Decisões Atuais](#-decisões-atuais) | [ADR-01](#adr-01--monólito-modular-em-spring-boot), [ADR-02](#adr-02--persistência-relacional-com-jpa--postgresql) |
| [🏗️ Arquitetura e Segurança](#️-arquitetura-e-segurança) | [ADR-03](#adr-03--autenticação-stateless-com-jwt), [ADR-04](#adr-04--autorização-por-perfil-e-rota) |
| [💼 Regras e Automação](#-regras-e-automação) | [ADR-05](#adr-05--regras-de-negócio-em-serviço-e-domínio), [ADR-06](#adr-06--regras-agendadas-para-processos-automáticos) |
| [⚙️ Infraestrutura](#️-infraestrutura) | [ADR-07](#adr-07--docker-como-ambiente-de-execução-e-validação) |
| [📊 Design e Contratos](#-design-e-contratos) | [ADR-08](#adr-08--dtos-como-contratos-da-api), [ADR-09](#adr-09--mapstruct-para-conversão-entre-dtos-e-entidades) |
| [🗃️ Modelo de Dados](#️-modelo-de-dados) | [ADR-10](#adr-10--ordem-de-serviço-com-múltiplos-orçamentos), [ADR-11](#adr-11--composição-do-orçamento-por-serviços-e-itens) |
| [⚠️ Tratamento de Falhas](#️-tratamento-de-falhas) | [ADR-12](#adr-12--tratamento-centralizado-de-exceções-da-api) |
| [🔭 Decisões Futuras](#-decisões-futuras) | [Em aberto](#decisões-futuras-em-aberto) |

---

## 🎯 Decisões Atuais

### ADR-01 — Monólito modular em Spring Boot

**Status:** Aceito

**Contexto:**

O domínio principal é operacional e relativamente coeso, concentrando gestão de Ordem de Serviço, orçamento, estoque, atendimento e acompanhamento da oficina.

O projeto não apresenta, no estado atual, necessidade comprovada de decomposição em serviços independentes.

**Decisão:**

Adotar uma aplicação monolítica em Java com Spring Boot, estruturada internamente por responsabilidades e camadas.

A aplicação mantém os principais componentes dentro de uma única unidade de execução, utilizando pacotes para separar domínio, aplicação, interface e infraestrutura.

**Consequências:**

- desenvolvimento e manutenção mais simples para a fase atual do projeto;
- menor complexidade operacional e infraestrutural;
- facilidade de rastrear regras e fluxos dentro do mesmo processo;
- comunicação direta entre componentes internos;
- menor independência de implantação e escalabilidade entre partes do domínio;
- eventual crescimento do sistema pode exigir revisão dessa decisão.

### ADR-02 — Persistência relacional com JPA + PostgreSQL

**Status:** Aceito

**Contexto:**

O domínio possui relações fortes entre:

- cliente;
- veículo;
- Ordem de Serviço;
- orçamento;
- serviços;
- itens;
- estoque;
- usuários;
- funcionários.

Parte relevante das operações exige consistência entre diferentes registros e manutenção do histórico dos atendimentos.

**Decisão:**

Utilizar PostgreSQL como banco de dados relacional principal e Spring Data JPA como abstração de persistência da aplicação.

Os repositories realizam o acesso aos dados através das abstrações fornecidas pelo Spring Data JPA.

**Consequências:**

- suporte a integridade referencial;
- suporte a operações transacionais;
- aderência ao modelo de relacionamentos do domínio;
- facilidade de consulta por veículo, OS, orçamento e status;
- possibilidade de utilizar recursos relacionais do PostgreSQL;
- dependência de um schema relacional bem definido;
- mudanças estruturais no domínio podem exigir evolução do modelo de dados.

---

## 🏗️ Arquitetura e Segurança

### ADR-03 — Autenticação stateless com JWT

**Status:** Aceito

**Contexto:**

A API precisa autenticar diferentes perfis e manter a comunicação entre cliente e servidor sem depender de sessão armazenada na aplicação.

Também existe necessidade de proteger os endpoints e transportar informações de autenticação entre requisições REST.

**Decisão:**

Implementar autenticação utilizando JWT integrado ao Spring Security, com política stateless.

O token é gerado durante a autenticação e posteriormente validado pelo filtro de segurança nas requisições protegidas.

**Consequências:**

- ausência de estado de sessão mantido pelo servidor;
- facilidade de integração com consumidores REST;
- autenticação baseada em token;
- possibilidade de transportar perfil e informações de autorização;
- necessidade de gerenciamento seguro do segredo utilizado para assinatura;
- necessidade de controle adequado do tempo de validade dos tokens.

### ADR-04 — Autorização por perfil e rota

**Status:** Aceito

**Contexto:**

O sistema possui diferentes perfis de acesso:

- `ADMIN`;
- `MECANICO`;
- `CLIENTE`.

O perfil `ADMIN` é utilizado pela recepcionista para executar as operações administrativas da oficina.

As funcionalidades disponíveis variam de acordo com o perfil autenticado e com a operação solicitada.

**Decisão:**

Aplicar autorização utilizando Spring Security, considerando perfil e rota acessada.

As operações públicas, administrativas e autenticadas são configuradas explicitamente na camada de segurança.

**Consequências:**

- maior segregação entre responsabilidades;
- controle explícito de acesso aos endpoints;
- redução do risco de operações administrativas por usuários sem permissão;
- regras de autorização concentradas em componentes específicos;
- possibilidade de evolução para regras mais granulares caso o domínio exija.

---

## 💼 Regras e Automação

### ADR-05 — Regras de negócio em serviço e domínio

**Status:** Aceito

**Contexto:**

A aplicação possui regras relacionadas a:

- ciclo de vida da Ordem de Serviço;
- transições de status;
- orçamento;
- pagamento;
- estoque;
- alocação de funcionário;
- execução dos serviços;
- cancelamento e abandono técnico.

Concentrar essas validações apenas nos controllers aumentaria o acoplamento da interface HTTP com o comportamento do negócio.

**Decisão:**

Distribuir as regras conforme sua responsabilidade.

Regras intrínsecas ao estado das entidades e suas transições permanecem no domínio, enquanto coordenação de casos de uso, validações entre componentes e orquestração permanecem nos services.

Os controllers ficam responsáveis principalmente pela interface HTTP e delegação dos casos de uso.

**Consequências:**

- melhor separação de responsabilidades;
- maior consistência das regras;
- redução de lógica de negócio nos controllers;
- maior reutilização das regras em diferentes fluxos;
- necessidade de manter claros os limites entre domínio e application services;
- regras distribuídas exigem documentação e testes adequados para preservar rastreabilidade.

### ADR-06 — Regras agendadas para processos automáticos

**Status:** Aceito

**Contexto:**

Algumas regras dependem do tempo transcorrido e precisam ser verificadas independentemente de interação manual do usuário.

Entre elas estão:

- processamento de cancelamentos automáticos;
- identificação de abandono técnico.

**Decisão:**

Utilizar `@Scheduled` do Spring para executar rotinas periódicas responsáveis por verificar as Ordens de Serviço elegíveis e aplicar as regras correspondentes.

As rotinas principais são executadas diariamente em horários definidos pela aplicação.

**Consequências:**

- redução da necessidade de execução manual;
- aplicação automática das regras temporais;
- centralização das verificações recorrentes;
- dependência da disponibilidade da aplicação durante a execução das tarefas;
- necessidade de observar logs e possíveis falhas das rotinas;
- mudanças nas regras de prazo precisam ser refletidas tanto no domínio quanto no agendamento.

---

## ⚙️ Infraestrutura

### ADR-07 — Docker como ambiente de execução e validação

**Status:** Aceito

**Contexto:**

O projeto precisa oferecer um ambiente reproduzível para execução local e suporte às ferramentas utilizadas durante desenvolvimento e análise de qualidade.

Também existe necessidade de executar componentes auxiliares, como SonarQube.

**Decisão:**

Utilizar Docker para empacotamento da aplicação e Docker Compose para orquestração dos componentes utilizados no ambiente local.

O `Dockerfile` utiliza múltiplos estágios para separar build e runtime.

**Consequências:**

- maior previsibilidade entre ambientes;
- facilidade de replicação do ambiente local;
- isolamento das dependências da aplicação;
- suporte à execução de ferramentas auxiliares;
- necessidade de Docker disponível no ambiente;
- manutenção adicional dos arquivos de containerização.

---

## 📊 Design e Contratos

### ADR-08 — DTOs como contratos da API

**Status:** Aceito

**Contexto:**

As entidades utilizadas pelo domínio também representam estruturas persistidas pela aplicação.

Expor essas entidades diretamente através da API criaria acoplamento entre:

- contratos HTTP;
- modelo interno;
- persistência;
- relacionamentos JPA.

Além disso, diferentes operações exigem conjuntos distintos de dados de entrada e saída.

**Decisão:**

Utilizar DTOs específicos para representar os contratos de entrada e saída da API.

Os DTOs permanecem organizados na camada `application` e são utilizados pelos controllers e services para comunicação entre a interface e a aplicação.

**Consequências:**

- redução do acoplamento entre API e entidades;
- contratos específicos por operação;
- menor exposição de atributos internos;
- possibilidade de validações específicas nos objetos de entrada;
- maior independência para evolução do modelo persistido;
- aumento da quantidade de classes mantidas pela aplicação;
- necessidade de conversão entre DTOs e entidades.

### ADR-09 — MapStruct para conversão entre DTOs e entidades

**Status:** Aceito

**Contexto:**

A separação entre os contratos da API e as entidades exige conversões recorrentes entre DTOs e objetos do domínio.

A implementação manual de todos os mapeamentos aumentaria repetição e risco de inconsistência.

**Decisão:**

Utilizar MapStruct nos componentes de mapeamento da infraestrutura para realizar a maior parte das conversões entre entidades e DTOs.

Os mappers concentram as transformações necessárias e evitam que controllers assumam essa responsabilidade.

**Consequências:**

- redução de código repetitivo;
- centralização das conversões;
- separação entre contratos e modelo interno;
- facilidade de manutenção de mapeamentos semelhantes;
- geração automática de implementações em tempo de build;
- alterações em DTOs ou entidades podem exigir atualização das interfaces de mapper.

---

## 🗃️ Modelo de Dados

### ADR-10 — Ordem de Serviço com múltiplos orçamentos

**Status:** Aceito

**Contexto:**

O fluxo da oficina permite que uma Ordem de Serviço possua mais de um orçamento ao longo do atendimento.

Além do orçamento inicial, podem existir orçamentos complementares associados à mesma OS.

Uma associação limitada a um único orçamento não representa adequadamente esse comportamento.

**Decisão:**

Modelar a relação entre Ordem de Serviço e orçamento como `1:N`.

A referência persistida é mantida em `TB_ORCAMENTOS.id_os`.

```text
TB_ORDENS_SERVICOS
        1
        │
        │
        N
TB_ORCAMENTOS