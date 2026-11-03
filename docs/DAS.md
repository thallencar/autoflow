# DAS - Design Approval Sheet

![V2.0.0](https://img.shields.io/badge/V2.0.0-gray?style=for-the-badge)

Registro de avaliação e aprovação do design da solução AutoFlow, consolidando escopo, arquitetura proposta, requisitos atendidos, riscos, restrições e pendências.

---

## 🗄️ Seções do Documento

| Seção | Subseções |
| --- | --- |
| [🎯 Identificação](#-identificação) | [Projeto](#projeto), [Objetivo](#objetivo), [Escopo da avaliação](#escopo-da-avaliação) |
| [🏗️ Solução Avaliada](#️-solução-avaliada) | [Visão resumida](#visão-resumida), [Arquitetura](#arquitetura), [Persistência](#persistência), [Segurança](#segurança) |
| [✅ Critérios de Aprovação](#-critérios-de-aprovação) | [Requisitos funcionais](#requisitos-funcionais), [Requisitos técnicos](#requisitos-técnicos), [Qualidade](#qualidade) |
| [⚠️ Riscos e Restrições](#️-riscos-e-restrições) | [Riscos identificados](#riscos-identificados), [Restrições atuais](#restrições-atuais) |
| [📌 Pendências e Evoluções](#-pendências-e-evoluções) | [Evoluções futuras](#evoluções-futuras) |
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

A solução utiliza uma aplicação monolítica modular em Java com Spring Boot, seguindo arquitetura hexagonal (Ports and Adapters).

Os principais elementos são:

- adaptadores de entrada com controllers REST e DTOs;
- portas de entrada e casos de uso da aplicação;
- domínio com entidades e regras de negócio;
- portas e adaptadores de persistência;
- mappers para conversão entre contratos, domínio e entidades JPA;
- componentes de segurança;
- tratamento centralizado de exceções;
- rotinas agendadas.

O detalhamento completo está disponível no ARCHITECTURE.md.

### Persistência

A persistência utiliza PostgreSQL hospedado no Supabase, com Spring Data JPA.

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
| Back-end monolítico modular | Atendido |
| Arquitetura hexagonal (Ports and Adapters) | Atendido |
| API REST | Atendido |
| Documentação OpenAPI / Swagger UI | Atendido |
| Banco de dados PostgreSQL hospedado no Supabase | Atendido |
| Justificativa da escolha do banco | Atendido |
| Containerização com Docker e execução local com Docker Compose | Atendido |
| Infraestrutura AWS declarada com Terraform (ECR, EKS e node group) | Atendido |
| Implantação Kubernetes com Deployment, Service e HPA | Atendido |
| Pipeline GitHub Actions para testes, build e deploy | Atendido com ressalvas |
| README com instruções | Atendido |
| Documentação DDD | Atendido |
| Collection para testes da API | Atendido |

### Qualidade

A solução utiliza:

- testes automatizados;
- JUnit;
- Spring Boot Test;
- JaCoCo;
- SonarQube para análise local.

Na avaliação registrada para o MVP, foi medida cobertura de *84,2%*. 

O painel do SonarQube consultado naquela avaliação não indicou issues de Security, Reliability ou Maintainability.

O Quality Gate geral apresentou status Failed naquela avaliação. O workflow atual não executa análise SonarQube nem bloqueia o pipeline por Quality Gate; o resultado precisa ser revalidado em uma nova análise.

---

## ⚠️ Riscos e Restrições

### Riscos Identificados

| Risco | Impacto |
| --- | --- |
| Dependência de uma única aplicação executável | Uma falha na aplicação pode impactar todos os módulos |
| Rotinas agendadas executadas em cada réplica | Com até duas réplicas e sem trava distribuída, uma tarefa pode ser processada mais de uma vez |
| Deploy sem restrição de evento ou branch no workflow | Um pull request pode alcançar o job de deploy quando as credenciais AWS estiverem disponíveis |
| Job `lint` não bloqueante | Falhas em `mvn validate` não interrompem o pipeline, e o job não executa uma ferramenta específica de lint |
| Segredos e dependências de IAM pré-existentes | O Secret Kubernetes precisa estar configurado fora do workflow; roles IAM, VPC e sub-redes são pré-requisitos do Terraform |
| Imagem publicada com tag mutável `latest` | A identificação e a reversão de versões da imagem ficam menos precisas |
| Infraestrutura EKS e EC2 ativa | Pode gerar custos recorrentes até a desmobilização dos recursos |
| Crescimento do domínio | Pode exigir revisão da separação modular |
| Quality Gate não aprovado na avaliação registrada | Requer nova análise e identificação das condições responsáveis pelo resultado |

### Restrições Atuais

A implementação atual não contempla:

- gateway externo de pagamento;
- serviço externo completo de notificações;
- autenticação multifator;
- observabilidade avançada;
- rate limiting;
- separação em serviços independentes.

O cluster Kubernetes utiliza um Service interno (`ClusterIP`), sem exposição pública configurada neste projeto. O workflow automatiza testes, build e deploy, mas não inclui análise SonarQube bloqueante nem validação de Quality Gate. Essas limitações não fazem parte do escopo obrigatório atual.

---

## 📌 Pendências e Evoluções

### Evoluções Futuras

Possíveis evoluções incluem:

- gateway de pagamento;
- notificações externas;
- autenticação multifator;
- observabilidade avançada;
- análise SonarQube e Quality Gate integrados à CI;
- versionamento imutável de imagens e promoção controlada entre ambientes;
- coordenação distribuída das tarefas agendadas;
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
- revisão das permissões e das condições que permitem a execução do deploy automático;
- provisionamento seguro dos segredos necessários no cluster;
- prevenção de processamento duplicado das rotinas caso o HPA aumente o número de réplicas;
- controle dos custos e desmobilização da infraestrutura AWS quando não estiver em uso;
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