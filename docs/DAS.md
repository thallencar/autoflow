# DAS - Design Approval Sheet

![V1.2.0](https://img.shields.io/badge/V1.2.0-gray?style=for-the-badge)

Registro de avaliação e aprovação do design da solução AutoFlow, consolidando escopo, arquitetura proposta, requisitos atendidos, riscos, restrições e pendências.

---

## 🗄️ Seções do Documento

| Seção | Subseções |
| --- | --- |
| [🎯 Identificação](#-identificação) | [Projeto](#projeto), [Objetivo](#objetivo), [Escopo da avaliação](#escopo-da-avaliação) |
| [🏗️ Solução Avaliada](#️-solução-avaliada) | [Visão resumida](#visão-resumida), [Arquitetura](#arquitetura), [Persistência](#persistência), [Segurança](#segurança) |
| [✅ Critérios de Aprovação](#-critérios-de-aprovação) | [Requisitos funcionais](#requisitos-funcionais), [Requisitos técnicos](#requisitos-técnicos), [Qualidade](#qualidade) |
| [⚠️ Riscos e Restrições](#️-riscos-e-restrições) | [Riscos identificados](#riscos-identificados), [Restrições atuais](#restrições-atuais) |
| [📌 Pendências e Evoluções](#-pendências-e-evoluções) | [Pendências](#pendências), [Evoluções futuras](#evoluções-futuras) |
| [📝 Decisão](#-decisão) | [Resultado da avaliação](#resultado-da-avaliação), [Condições de aprovação](#condições-de-aprovação) |
| [📚 Referências](#-referências) | [Documentação relacionada](#documentação-relacionada) |

---

## 🎯 Identificação

### Projeto

*Nome:* AutoFlow

*Grupo:* Grupo 31

### Objetivo

Este Design Approval Sheet registra a avaliação do design da solução AutoFlow e consolida os principais elementos considerados para sua aprovação.

O documento não substitui a documentação arquitetural detalhada. Seu objetivo é registrar, de forma resumida, se a solução proposta atende aos requisitos funcionais, técnicos, de segurança e qualidade definidos para o projeto.

### Escopo da Avaliação

A avaliação considera:

- arquitetura da aplicação;
- principais fluxos de negócio;
- persistência;
- segurança;
- APIs;
- testes;
- infraestrutura;
- requisitos técnicos;
- riscos e limitações conhecidas.

---

## 🏗️ Solução Avaliada

### Visão Resumida

O AutoFlow é uma aplicação back-end para gerenciamento dos processos operacionais de uma oficina mecânica.

A solução centraliza:

- clientes;
- veículos;
- funcionários;
- Ordens de Serviço;
- diagnósticos;
- orçamentos;
- serviços;
- estoque;
- pagamentos;
- histórico;
- métricas operacionais.

### Arquitetura

A solução utiliza uma aplicação monolítica em Java com Spring Boot, organizada internamente por responsabilidades.

Os principais elementos são:

- controllers REST;
- application services;
- entidades de domínio;
- repositories;
- DTOs;
- mappers;
- componentes de segurança;
- tratamento centralizado de exceções;
- rotinas agendadas.

O detalhamento completo está disponível no ARCHITECTURE.md.

### Persistência

A persistência utiliza PostgreSQL com Spring Data JPA.

O modelo relacional atende às principais relações do domínio, incluindo:

- cliente e veículo;
- endereço;
- Ordem de Serviço;
- orçamento;
- serviços;
- itens;
- estoque;
- funcionário;
- usuários.

O modelo físico e o MER estão documentados no ARCHITECTURE.md.

### Segurança

A aplicação utiliza:

- Spring Security;
- autenticação JWT;
- política stateless;
- autorização por perfil;
- BCrypt;
- Bean Validation;
- tratamento centralizado de exceções.

Os perfis previstos são:

- ADMIN;
- MECANICO;
- CLIENTE.

O perfil ADMIN é utilizado pela recepcionista para operações administrativas.

---

## ✅ Critérios de Aprovação

### Requisitos Funcionais

| Critério | Situação |
| --- | --- |
| Identificação e cadastro de clientes | Atendido |
| Cadastro de veículos | Atendido |
| Criação e acompanhamento de OS | Atendido |
| Controle de status da OS | Atendido |
| Diagnóstico | Atendido |
| Geração de orçamento | Atendido |
| Aprovação e recusa de orçamento | Atendido |
| Orçamento complementar | Atendido |
| Gestão de serviços | Atendido |
| Gestão de peças e insumos | Atendido |
| Controle de estoque | Atendido |
| Histórico por veículo | Atendido |
| Controle de pagamento | Atendido |
| Métricas operacionais | Atendido |

### Requisitos Técnicos

| Critério | Situação |
| --- | --- |
| Back-end monolítico | Atendido |
| Organização em camadas | Atendido |
| API REST | Atendido |
| Documentação OpenAPI / Swagger UI | Atendido |
| Banco de dados relacional | Atendido |
| Justificativa da escolha do banco | Atendido |
| Dockerfile | Atendido |
| Docker Compose | Atendido |
| README com instruções | Atendido |
| Documentação DDD | Atendido |
| Collection para testes da API | Atendido |

### Qualidade

A solução utiliza:

- testes automatizados;
- JUnit;
- Spring Boot Test;
- JaCoCo;
- SonarQube.

Na análise realizada, foi registrada cobertura de *84,2%*.

O resultado apresentado pelo SonarQube não indicou issues de Security, Reliability ou Maintainability no painel analisado.

O Quality Gate geral apresentou status Failed, devendo o detalhamento das condições configuradas ser consultado para identificação da causa específica.

---

## ⚠️ Riscos e Restrições

### Riscos Identificados

| Risco | Impacto |
| --- | --- |
| Dependência de uma única aplicação executável | Uma falha na aplicação pode impactar todos os módulos |
| Processos agendados executados pela própria aplicação | Dependem da disponibilidade da instância |
| Segredo JWT configurado externamente | Requer gerenciamento adequado nos ambientes |
| Crescimento do domínio | Pode exigir revisão da separação modular |
| Quality Gate não aprovado | Requer análise das condições responsáveis pelo resultado |

### Restrições Atuais

A implementação atual não contempla:

- gateway externo de pagamento;
- serviço externo completo de notificações;
- autenticação multifator;
- observabilidade avançada;
- rate limiting;
- separação em serviços independentes.

Esses itens não fazem parte do escopo obrigatório atual.

---

## 📌 Pendências e Evoluções

### Pendências

As principais pendências identificadas são:

- verificar a condição responsável pelo Failed do Quality Gate;
- manter documentação e diagramas sincronizados com a implementação;
- garantir que alterações no modelo de dados sejam refletidas no MER;
- manter testes e cobertura atualizados conforme evolução do código.

### Evoluções Futuras

Possíveis evoluções incluem:

- gateway de pagamento;
- notificações externas;
- autenticação multifator;
- observabilidade;
- automação de CI/CD;
- políticas de segurança mais granulares;
- revisão da modularização em caso de crescimento da solução.

Essas evoluções devem ser avaliadas separadamente antes de serem incorporadas à arquitetura.

---

## 📝 Decisão

### Resultado da Avaliação

*Status:* Aprovado com ressalvas

O design da solução AutoFlow atende aos principais requisitos funcionais e técnicos definidos para o projeto.

A arquitetura adotada é adequada ao escopo atual do MVP, mantendo as responsabilidades internas separadas e permitindo evolução incremental.

### Condições de Aprovação

A aprovação considera as seguintes ressalvas:

- acompanhamento do Quality Gate do SonarQube;
- manutenção da cobertura mínima de testes;
- sincronização contínua entre código, modelo de dados e documentação;
- registro de novas decisões arquiteturais relevantes no ADR.md.

As ressalvas identificadas não impedem a utilização da arquitetura proposta no escopo atual do projeto.

---

## 📚 Referências

### Documentação Relacionada

| Documento | Finalidade |
| --- | --- |
| README.md | Overview, instalação e execução |
| BUSINESS.md | Domínio, regras de negócio e DDD |
| ARCHITECTURE.md | Arquitetura técnica completa, HLD e LLD |
| ADR.md | Decisões arquiteturais e justificativas |
| DAS.md | Registro de avaliação e aprovação do design |