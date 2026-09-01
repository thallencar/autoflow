# DAS - Documentação de Arquitetura de Software

![V1.2.0](https://img.shields.io/badge/V1.2.0-gray?style=for-the-badge)

_Visão consolidada da solução AutoFlow, relacionando contexto de negócio, arquitetura, principais componentes, decisões arquiteturais e documentação complementar._

---

## 🗄️ Seções do Documento

| Seção | Subseções |
| --- | --- |
| [🎯 Objetivo e Visão](#-objetivo-e-visão) | [Objetivo](#objetivo), [Visão da solução](#visão-da-solução), [Escopo](#escopo) |
| [🏗️ Visão Arquitetural](#️-visão-arquitetural) | [Estrutura](#estrutura-da-solução), [Componentes](#componentes-principais), [Diagrama](#diagrama-da-solução) |
| [🔄 Negócio e Arquitetura](#-negócio-e-arquitetura) | [Fluxo central](#fluxo-central), [Responsabilidades](#responsabilidades-arquiteturais) |
| [📐 Decisões e Qualidade](#-decisões-e-qualidade) | [Decisões](#decisões-arquiteturais), [Qualidade](#qualidade-e-testes), [Segurança](#segurança) |
| [⚠️ Limites e Evolução](#️-limites-e-evolução) | [Limites](#limites-atuais), [Evoluções](#evoluções-previstas) |
| [📚 Documentação Relacionada](#-documentação-relacionada) | [Documentos](#mapa-da-documentação) |

---

## 🎯 Objetivo e Visão

### Objetivo

Esta DAS apresenta uma visão consolidada da arquitetura de software do AutoFlow.

Seu objetivo não é reproduzir o detalhamento técnico existente no `ARCHITECTURE.md`, mas permitir a compreensão da solução como um todo e indicar onde cada aspecto do sistema está documentado.

A DAS relaciona:

- contexto de negócio;
- organização arquitetural;
- componentes principais;
- fluxo central da solução;
- persistência;
- segurança;
- qualidade;
- decisões arquiteturais;
- pontos de evolução.

### Visão da Solução

O AutoFlow é um sistema de gestão operacional para oficinas mecânicas que centraliza o ciclo de atendimento através da Ordem de Serviço.

A solução integra os processos relacionados a:

- clientes;
- veículos;
- funcionários;
- Ordens de Serviço;
- diagnósticos;
- orçamentos;
- serviços;
- estoque;
- pagamento;
- histórico;
- métricas operacionais.

A aplicação é implementada em Java com Spring Boot e organizada como uma aplicação monolítica com separação interna de responsabilidades.

A Ordem de Serviço atua como elemento central do fluxo operacional, relacionando o atendimento aos demais componentes do domínio.

### Escopo

A solução atual contempla principalmente:

- autenticação e autorização de usuários;
- gestão de clientes e veículos;
- gestão de funcionários;
- abertura e acompanhamento de Ordens de Serviço;
- diagnóstico;
- orçamento inicial e complementar;
- aprovação ou recusa de orçamento;
- controle e reserva de estoque;
- execução dos serviços;
- acompanhamento do pagamento;
- encerramento e entrega da OS;
- histórico por veículo;
- métricas operacionais;
- processamento automático de regras temporais.

O detalhamento das regras associadas a esses processos está disponível em [BUSINESS.md](BUSINESS.md).

---

## 🏗️ Visão Arquitetural

### Estrutura da Solução

A aplicação está organizada nas seguintes responsabilidades principais:

| Responsabilidade | Papel |
| --- | --- |
| **Interface** | Exposição da API REST e recebimento das requisições |
| **Application** | Orquestração dos casos de uso e fluxos da aplicação |
| **Domain** | Entidades, estados e comportamentos relacionados ao domínio |
| **Repository** | Abstração de acesso aos dados |
| **Infrastructure** | Segurança, mapeamento, persistência e mecanismos técnicos |
| **Exception** | Tratamento centralizado das falhas da aplicação |

A estrutura detalhada dos packages, classes, DTOs, mappers, services, repositories e controllers está documentada no [ARCHITECTURE.md](ARCHITECTURE.md).

### Componentes Principais

Os principais componentes da solução são:

**API REST**

Responsável por disponibilizar as funcionalidades do AutoFlow aos consumidores da aplicação.

**Spring Security + JWT**

Responsável pela autenticação, validação dos tokens e controle de acesso aos recursos protegidos.

**Application Services**

Responsáveis pela coordenação dos casos de uso e integração entre diferentes elementos do domínio.

**Domain Model**

Representa os conceitos e comportamentos centrais da oficina, incluindo Ordem de Serviço, orçamento, estoque, funcionário, veículo e serviço.

**Repositories**

Responsáveis pela abstração das operações de persistência através do Spring Data JPA.

**PostgreSQL**

Banco de dados relacional utilizado para persistência das informações da aplicação.

**Scheduled Jobs**

Responsáveis pela execução das regras automáticas dependentes de tempo.

**OpenAPI**

Responsável pela descrição dos contratos HTTP da aplicação, disponibilizados através do Swagger UI.

### Diagrama da Solução

```mermaid
flowchart TD
    Users["Recepcionista (ADMIN) / Mecânico / Cliente"]

    API["AutoFlow REST API"]

    Security["Spring Security + JWT"]
    Controllers["REST Controllers"]
    Services["Application Services"]
    Domain["Domain Model"]
    Repository["JPA Repositories"]
    Scheduler["Scheduled Jobs"]

    DB[("PostgreSQL")]
    Docs["Springdoc OpenAPI / Swagger UI"]

    Users -->|HTTP / JSON| API

    API --> Security
    Security --> Controllers
    Controllers --> Services

    Scheduler --> Services

    Services --> Domain
    Services --> Repository

    Repository --> DB

    API --> Docs