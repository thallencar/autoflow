# AutoFlow

Sistema para gestão de oficina mecânica, com foco no ciclo completo da Ordem de Serviço (OS): recebimento do veículo, diagnóstico, orçamento, execução, pagamento e entrega.

## Visão geral

O AutoFlow foi pensado para substituir processos manuais e descentralizados por um fluxo centralizado de atendimento. A aplicação organiza o domínio de oficina em módulos de cliente, veículo, funcionário, serviço, orçamento, estoque e ordem de serviço.

### Funcionalidades principais

- Cadastro e gestão de clientes, funcionários e veículos;
- Abertura e acompanhamento de Ordens de Serviço;
- Registro de diagnóstico e serviços executados;
- Criação e acompanhamento de orçamentos;
- Controle de peças, reservas e estoque;
- Registro de execução do serviço e indicadores de tempo;
- Controle de pagamento e entrega do veículo;
- Autenticação e autorização por perfil.

---

## Arquitetura atual

O projeto foi reorganizado para seguir a abordagem de arquitetura hexagonal (Ports and Adapters), com o núcleo de negócio independente de frameworks e infraestrutura.

### Principio

- O domínio não conhece detalhes de banco, web ou frameworks externos;
- os adaptadores conectam a aplicação ao mundo externo;
- os ports definem as interfaces de entrada e saída da aplicação.

### Estrutura principal

```text
src/main/java/br/com/autoflow/
├── adapters/
│   ├── inbound/      # controllers, mappers e integração HTTP
│   ├── outbound/     # persistência e integrações externas
│   └── config/       # configuração do Spring
├── application/
│   ├── usecase/      # casos de uso da aplicação
│   └── validator/    # validações de regras de aplicação
├── domain/
│   ├── model/        # entidades de domínio
│   ├── enums/        # enums do negócio
│   └── exception/    # exceções de domínio
├── ports/
│   ├── inbound/      # interfaces de entrada (casos de uso / serviços)
│   └── outbound/     # interfaces de persistência e integrações
├── AutoflowApplication.java
└── resources/
   └── application.properties
```

### Fluxo de execução

```text
HTTP Request
   ↓
Controller (adapter inbound)
   ↓
Use Case (application)
   ↓
Domain model / business rules
   ↓
Port outbound
   ↓
Persistence adapter / JPA / database
```

Esse desenho deixa o núcleo do negócio protegido de detalhes de tecnologia e facilita troca de infraestrutura sem impactar a lógica principal.

---

## Fluxo da Ordem de Serviço

```text
Recebida
 ↓
Em diagnóstico
 ↓
Aguardando aprovação
 ↓
Em execução
 ↓
Finalizada
 ↓
Entregue
```

Em cenários excepcionais, a OS pode ir para cancelamento ou replanejamento conforme regras do negócio.

---

## Principais atores

### Cliente
- acompanha OS;
- aprova ou rejeita orçamento;
- retira o veículo;
- acompanha histórico e status.

### Recepcionista
- cadastra clientes e veículos;
- abre ordens de serviço;
- acompanha andamento da OS;
- registra dados de pagamento e entrega.

### Mecânico
- realiza diagnóstico;
- define serviços e peças necessárias;
- registra execução;
- acompanha alertas de estoque.

---

## Tecnologias

- Java 25
- Spring Boot 4.1
- Spring Web MVC
- Spring Validation
- Spring Security
- JWT (java-jwt)
- Spring Data JPA
- Hibernate
- PostgreSQL
- MapStruct
- Lombok
- Maven / Maven Wrapper
- OpenAPI / Swagger
- GitHub Actions
- Terraform
- AWS ECS / ECR

### Pipeline de CI/CD

O repositório já possui uma pipeline em GitHub Actions para executar:

- validação do projeto;
- testes unitários;
- build da aplicação;
- empacotamento da imagem Docker;
- push da imagem para Amazon ECR.

O provisionamento da infraestrutura AWS é realizado com Terraform, com foco em ECS/Fargate e ECR.

Observação: o projeto não inclui manifests ou configuração de Kubernetes no repositório no momento. Se a intenção for migrar para Kubernetes, isso deve ser implementado como etapa adicional.

---

## Como executar

### Requisitos

- JDK 25
- Maven Wrapper ou Maven instalado
- Banco PostgreSQL disponível

### Comandos

```bash
./mvnw clean install
./mvnw spring-boot:run
```

Se o ambiente usa Docker, também é possível levantar a infraestrutura com o compose disponível no projeto.

---

## Observações de domínio

### Linguagem do domínio

- OS / Ordem de Serviço
- Diagnóstico
- Orçamento
- Serviço
- Peça / item de estoque
- Reserva de estoque
- Aprovação do orçamento
- Pagamento
- Entrega do veículo

---

## Status da estrutura

Este README foi atualizado para refletir a arquitetura hexagonal adotada no projeto e a organização real dos pacotes do código-fonte.
