# ADR - Architecture Decision Record

![V2.0.0](https://img.shields.io/badge/V2.0.0-gray?style=for-the-badge)

_Registro das decisões arquiteturais tomadas para o AutoFlow, com contexto, decisão e consequências observadas no projeto._

---

## 🗄️ Seções do Documento

| Seção | Subseções |
| --- | --- |
| [🎯 Decisões Atuais](#-decisões-atuais) | [ADR-01](#adr-01--adoção-de-monólito-em-spring-boot), [ADR-02](#adr-02--persistência-relacional-com-jpa--postgresql) |
| [🏗️ Arquitetura e Segurança](#️-arquitetura-e-segurança) | [ADR-03](#adr-03--autenticação-stateless-com-jwt), [ADR-04](#adr-04--autorização-por-perfil-e-rota) |
| [💼 Regras e Automação](#-regras-e-automação) | [ADR-05](#adr-05--regras-de-negócio-em-serviço-e-domínio), [ADR-06](#adr-06--regras-agendadas-para-processos-automáticos) |
| [⚙️ Infraestrutura](#️-infraestrutura) | [ADR-07](#adr-07--docker-para-containerização-e-execução-local) |
| [📊 Design e Contratos](#-design-e-contratos) | [ADR-08](#adr-08--dtos-como-contratos-da-api), [ADR-09](#adr-09--mapstruct-para-conversão-entre-dtos-e-entidades) |
| [🗃️ Modelo de Dados](#️-modelo-de-dados) | [ADR-10](#adr-10--ordem-de-serviço-com-múltiplos-orçamentos), [ADR-11](#adr-11--composição-do-orçamento-por-serviços-e-itens) |
| [⚠️ Tratamento de Falhas](#️-tratamento-de-falhas) | [ADR-12](#adr-12--tratamento-centralizado-de-exceções-da-api) |
| [🚀 Infraestrutura em Nuvem e CI/CD](#-infraestrutura-em-nuvem-e-cicd) | [ADR-13](#adr-13--infraestrutura-aws-gerenciada-com-terraform-e-eks), [ADR-14](#adr-14--pipeline-de-ci-cd-com-github-actions) |
| [🔄 Evolução da Arquitetura](#-evolução-da-arquitetura) | [ADR-15](#adr-15--adoção-da-arquitetura-hexagonal) |
| [🔭 Decisões Futuras](#-decisões-futuras) | [Em aberto](#decisões-futuras-em-aberto) |

---

## 🎯 Decisões Atuais

### ADR-01 — Adoção de monólito em Spring Boot

**Status:** Aceito. A decisão original sobre organização em camadas foi substituída pela ADR-15.

**Contexto:**

O domínio principal é operacional e relativamente coeso, concentrando gestão de Ordem de Serviço, orçamento, estoque, atendimento e acompanhamento da oficina. Não há necessidade comprovada de decomposição em serviços independentes.

A decisão original também descrevia a organização interna em camadas. Essa parte evoluiu posteriormente e sua substituição está registrada separadamente na ADR-15.

**Decisão:**

Manter o AutoFlow como uma aplicação monolítica em Java com Spring Boot, executada e implantada como uma única aplicação.

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

Utilizar PostgreSQL, hospedado no Supabase, como banco de dados relacional e Spring Data JPA como abstração de persistência da aplicação.

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

No estado atual, as rotinas de cancelamento e verificação de abandono técnico são executadas diariamente às 08:00 e às 09:00, respectivamente.

**Consequências:**

- redução da necessidade de execução manual;
- aplicação automática das regras temporais;
- centralização das verificações recorrentes;
- dependência da disponibilidade da aplicação durante a execução das tarefas;
- com múltiplas réplicas, cada instância pode executar as mesmas rotinas, pois não há eleição de líder ou trava distribuída;
- necessidade de observar logs e possíveis falhas das rotinas;
- mudanças nas regras de prazo precisam ser refletidas tanto no domínio quanto no agendamento.

---

## ⚙️ Infraestrutura

### ADR-07 — Docker para containerização e execução local

**Status:** Aceito

**Contexto:**

O projeto precisa de uma forma reproduzível de empacotar e executar a aplicação, além de facilitar o uso local dos serviços auxiliares de desenvolvimento.

**Decisão:**

Utilizar um `Dockerfile` multi-stage para compilar e executar a aplicação em container. Utilizar Docker Compose no ambiente local para iniciar a API e o SonarQube, com configurações fornecidas por ambiente.

**Consequências:**

- maior previsibilidade entre ambientes;
- facilidade de replicação do ambiente local;
- isolamento das dependências da aplicação;
- execução local integrada da aplicação e do SonarQube;
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

Os DTOs de requisição e resposta ficam nos adaptadores de entrada e representam os contratos HTTP. Os controllers convertem esses objetos para modelos de domínio antes de invocar as portas de entrada dos casos de uso.

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

Utilizar MapStruct nos mappers dos adaptadores de entrada e de persistência para converter entre DTOs, modelos de domínio e entidades JPA.

Os mappers concentram as transformações necessárias e evitam que controllers ou adapters de persistência implementem conversões repetitivas.

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
```

**Consequências:**

- uma OS pode manter seu orçamento inicial e orçamentos complementares;
- cada orçamento referencia obrigatoriamente uma OS por meio de `id_os`;
- o histórico de orçamentos permanece associado à OS;
- consultas e regras de negócio precisam considerar o orçamento aplicável, sem presumir uma relação `1:1`.

### ADR-11 — Composição do orçamento por serviços e itens

**Status:** Aceito

**Contexto:**

Um orçamento pode incluir serviços da oficina e itens de estoque associados à execução. Esses elementos possuem valores e relacionamentos próprios, que não devem ser representados como campos repetidos no registro principal do orçamento.

**Decisão:**

Modelar os serviços do orçamento em `TB_ORCAMENTO_SERVICOS` e os itens em `TB_ORCAMENTO_ITENS`. Cada serviço do orçamento referencia um serviço do catálogo e o orçamento correspondente. Cada item referencia o orçamento e o serviço orçado, mantendo quantidade, valor unitário, valor total e estado da reserva.

```text
TB_ORCAMENTOS
    1 ─── N TB_ORCAMENTO_SERVICOS
                    1 ─── N TB_ORCAMENTO_ITENS
    1 ─── N TB_ORCAMENTO_ITENS
```

**Consequências:**

- serviços e itens podem ser consultados e persistidos como componentes do orçamento;
- valores e estado de reserva são registrados por item;
- o total do item é calculado a partir da quantidade e do valor unitário;
- alterações no orçamento devem manter consistentes as associações entre orçamento, serviço e item.

## ⚠️ Tratamento de Falhas

### ADR-12 — Tratamento centralizado de exceções da API

**Status:** Aceito

**Contexto:**

Exceções originadas por validações, regras de negócio e buscas precisam ser convertidas em respostas HTTP compreensíveis, sem duplicar o tratamento em cada controller.

**Decisão:**

Utilizar `GlobalExceptionHandler`, baseado em `@RestControllerAdvice`, para tratar centralmente exceções conhecidas e produzir respostas com status HTTP apropriado. Erros de validação de entrada retornam os campos e mensagens de validação; as demais exceções tratadas usam `ApiErrorResponse`.

**Consequências:**

- tratamento consistente nos controllers para as exceções cobertas;
- uso de `404` para entidade não encontrada, `409` para dados já cadastrados e `400` para validação e regra de negócio;
- payloads de erro variam conforme a categoria, pois erros de validação retornam um mapa de campo e mensagem;
- novas exceções de domínio devem ter seu mapeamento HTTP definido explicitamente.

## 🚀 Infraestrutura em Nuvem e CI/CD

### ADR-13 — Infraestrutura AWS gerenciada com Terraform e EKS

**Status:** Aceito

**Contexto:**

A aplicação precisa de um ambiente de demonstração e implantação na nuvem, com registro de imagens e orquestração de containers. O banco PostgreSQL permanece hospedado externamente no Supabase.

**Decisão:**

Utilizar Terraform para declarar e provisionar o repositório Amazon ECR, o cluster Amazon EKS e seu Managed Node Group na região `us-east-2`. Executar a aplicação no Kubernetes com Deployment, Service interno do tipo `ClusterIP` e Horizontal Pod Autoscaler.

O Terraform consulta a VPC, as sub-redes e as roles IAM existentes; esses recursos e permissões não são criados pelo módulo atual. Os manifests Kubernetes são mantidos separadamente do Terraform.

**Consequências:**

- infraestrutura principal descrita como código e passível de reprodução;
- uso de serviços gerenciados AWS para registro e orquestração;
- escalabilidade horizontal da aplicação entre uma e duas réplicas, condicionada à disponibilidade de métricas de CPU no cluster;
- dependência de VPC, sub-redes e roles IAM previamente configuradas;
- custos de EKS e EC2 enquanto os recursos estiverem ativos;
- acesso ao Service `ClusterIP` restrito à rede do cluster, exigindo mecanismo como `kubectl port-forward` para acesso local;
- configuração de segredos da aplicação precisa estar disponível no cluster antes do Deployment, pois o workflow não aplica o manifesto `k8s/secret.yaml`.

### ADR-14 — Pipeline de CI/CD com GitHub Actions

**Status:** Aceito

**Contexto:**

O projeto precisa automatizar validação, testes, empacotamento e publicação da aplicação no ambiente Kubernetes.

**Decisão:**

Utilizar GitHub Actions para executar as etapas de validação Maven, testes com JaCoCo, empacotamento do JAR e implantação na AWS. O deploy constrói uma imagem Docker, publica no ECR e atualiza os recursos Kubernetes no EKS.

**Consequências:**

- etapas de teste, build e deploy encadeadas no workflow;
- artefato JAR disponibilizado por 14 dias;
- imagem Docker publicada com a tag mutável `latest`;
- a imagem do deploy é construída novamente pelo job de deploy, sem reutilizar o JAR publicado pelo job de build;
- credenciais AWS e permissões para ECR/EKS são necessárias nos segredos do GitHub;
- o job `lint` executa `mvn validate`, está marcado para não bloquear o fluxo em caso de falha e não substitui uma ferramenta de lint;
- o job de deploy não restringe evento ou branch, portanto pode ser acionado também em pull requests quando as credenciais estiverem disponíveis;
- a análise SonarQube não integra o workflow atual e o segredo Kubernetes da aplicação deve ser provisionado separadamente.

## 🔄 Evolução da Arquitetura

### ADR-15 — Adoção da arquitetura hexagonal

**Status:** Aceito (substitui a organização interna em camadas descrita originalmente na ADR-01)

**Contexto:**

A organização anterior em camadas apresentava oportunidades de melhoria no desacoplamento entre regras de negócio, interfaces e infraestrutura. A evolução da aplicação exigia tornar mais explícitas as fronteiras entre casos de uso e mecanismos de entrada e saída, sem alterar a decisão de manter o sistema como monólito.

**Decisão:**

Refatorar a organização interna do monólito para arquitetura hexagonal (Ports and Adapters). Os casos de uso e o domínio interagem com recursos externos por meio de portas, implementadas por adaptadores de entrada e saída.

**Consequências:**

- maior separação entre regras de negócio, interfaces e infraestrutura;
- facilita testes dos casos de uso sem dependência direta dos mecanismos externos;
- permite substituir ou evoluir adaptadores sem exigir mudanças equivalentes no núcleo da aplicação;
- aumenta a quantidade de interfaces, adaptadores e componentes a manter;
- a solução permanece um monólito: a adoção da arquitetura hexagonal não implica decomposição em microsserviços.

---

## 🔭 Decisões Futuras

### Decisões futuras em aberto

- Definir uma política de promoção de imagens com tags imutáveis e restrição de deploy a branches e ambientes autorizados.
- Avaliar a inclusão da análise SonarQube e de um Quality Gate bloqueante no workflow.
- Definir coordenação distribuída das rotinas agendadas antes de operar com múltiplas réplicas.
- Definir provisionamento e rotação dos segredos Kubernetes sem armazená-los em texto puro no repositório.