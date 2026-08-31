# ADR - Architecture Decision Record

![V1.1.0](https://img.shields.io/badge/V1.1.0-gray?style=for-the-badge)

_Registro das decisões arquiteturais tomadas para o AutoFlow, com contexto, decisão e consequências observadas no projeto._

---

## 🗄️ Seções do Documento

| Seção                                                   | Subseções                                                                                                                 |
| ------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------- |
| [🎯 Decisões Atuais](#-decisões-atuais)                 | [ADR-01](#adr-01--monólito-modular-em-spring-boot), [ADR-02](#adr-02--persistência-relacional-com-jpa--postgresql)        |
| [🏗️ Arquitetura e Segurança](#-arquitetura-e-segurança) | [ADR-03](#adr-03--autenticação-stateless-com-jwt), [ADR-04](#adr-04--autorização-por-perfil-e-rota)                       |
| [💼 Regras e Automação](#-regras-e-automação)           | [ADR-05](#adr-05--regras-de-negócio-em-serviço-e-domínio), [ADR-06](#adr-06--regras-agendadas-para-processos-automáticos) |
| [⚙️ Infraestrutura](#-infraestrutura)                   | [ADR-07](#adr-07--docker-como-ambiente-de-execução-e-validação), [Em aberto](#decisões-futuras-em-aberto)                 |

---

## 🎯 Decisões Atuais

### ADR-01 — Monólito modular em Spring Boot

**Status:** Aceito

**Contexto:**

O domínio principal é operacional e relativamente coeso: gestão de ordem de serviço, orçamento, estoque e acompanhamento. O projeto não apresenta, no momento, necessidade comprovada de decomposição em serviços independentes.

**Decisão:**

Adotar um monólito modular em Java com Spring Boot, organizando o código por pacotes e camadas.

**Consequências:**

- desenvolvimento e manutenção mais simples para a primeira fase do sistema;
- baixa complexidade operacional e infraestrutural;
- maior facilidade de rastrear regras de negócio dentro de um mesmo processo de execução;
- limitação de autonomia de escala por domínio, caso o sistema cresça em volume e complexidade.

### ADR-02 — Persistência relacional com JPA + PostgreSQL

**Status:** Aceito

**Contexto:**

O domínio possui relações fortes entre cliente, veículo, OS, orçamento, serviços e estoque. Isso favorece modelos transacionais e consultas históricas estruturadas.

**Decisão:**

Usar Spring Data JPA com PostgreSQL como banco principal da aplicação.

**Consequências:**

- bom suporte a integridade e consistência transacional;
- aderência à modelagem de histórico e status do atendimento;
- facilidade de consulta por veículo, OS e status;
- dependência de um banco relacional e de schema bem definido.

---

## 🏗️ Arquitetura e Segurança

### ADR-03 — Autenticação stateless com JWT

**Status:** Aceito

**Contexto:**

A API precisa autenticar múltiplos papéis e manter a comunicação sem estado entre requisições. Foi necessário reforçar segurança sem depender de sessão do servidor.

**Decisão:**

Implementar autenticação com JWT e política stateless em Spring Security.

**Consequências:**

- maior simplicidade para integrações REST;
- melhor aderência a aplicações com API pública ou controlada por token;
- proteção por token e perfil de usuário;
- necessidade de reforço de ciclo de vida de token e gestão de segredos em ambiente real.

### ADR-04 — Autorização por perfil e rota

**Status:** Aceito

**Contexto:**

O sistema possui diferentes papéis operacionais: admin, mecânico e cliente. As ações e permissões variam conforme o tipo de usuário e o tipo de operação.

**Decisão:**

Mapear perfis e aplicar autorizações por rota e por ação, com controle explícito no Spring Security.

**Consequências:**

- melhor segregação de responsabilidades operacionais;
- maior controle de acesso aos endpoints;
- manutenção mais clara de permissões por perfil;
- possibilidade de evolução para políticas mais granulares no futuro.

---

## 💼 Regras e Automação

### ADR-05 — Regras de negócio em serviço e domínio

**Status:** Aceito

**Contexto:**

A aplicação lida com transições de status, validações de orçamento, posições de pagamento, regras de execução e controle de estoque. Essas regras são sensíveis e exigem consistência.

**Decisão:**

Centralizar a validação principal em serviços e na própria entidade do domínio, em vez de apenas validar no controller.

**Consequências:**

- maior consistência de regras operacionais;
- melhor aderência ao conceito de domínio da oficina;
- maior clareza de responsabilidade entre API, serviço e modelo;
- necessidade de manter a lógica de negócio bem documentada para evitar acoplamento.

### ADR-06 — Regras agendadas para processos automáticos

**Status:** Aceito

**Contexto:**

O sistema precisa verificar pendências e aplicar ações automáticas, como cancelamento de orçamento e reconhecimento de abandono técnico.

**Decisão:**

Usar agendamento com `@Scheduled` para executar varreduras diárias em períodos definidos.

**Consequências:**

- melhora na automação operacional;
- redução da necessidade de ação manual para processos recorrentes;
- depende de cron e de regras de tempo bem definidas;
- exige monitoramento dos logs para garantir consistência da operação.

---

## ⚙️ Infraestrutura

### ADR-07 — Docker como ambiente de execução e validação

**Status:** Aceito

**Contexto:**

O projeto exige execução local consistente e também apoio à análise de qualidade com SonarQube.

**Decisão:**

Utilizar Docker e Docker Compose para a aplicação e para o ambiente de análise estática.

**Consequências:**

- execução mais previsível em diferentes ambientes;
- fácil replicação de ambiente de desenvolvimento e validação;
- suporte à análise de qualidade e observabilidade local;
- aumenta a dependência de contêineres para execução do projeto.

### Decisões futuras em aberto

- integração com gateway de pagamento externo;
- notificações automáticas para cliente e equipe;
- evolução para autenticação multifator ou policies mais avançadas;
- separação operacional em módulos mais independentes, caso o sistema cresça.

---
