# Documentação Técnica do AutoFlow

![V1.1.0](https://img.shields.io/badge/V1.1.0-gray?style=for-the-badge)

_Visão técnica do sistema, incluindo stack, arquitetura em camadas, segurança, persistência, infraestrutura e qualidade._

---

## 🗄️ Seções do Documento

| Seção                                                     | Subseções                                                                                                            |
| --------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------- |
| [🎯 Contexto e Stack](#-contexto-e-stack)                 | [Contexto](#contexto-técnico), [Stack](#stack-tecnológica)                                                           |
| [🏗️ Arquitetura e Código](#-arquitetura-e-código)         | [Camadas](#arquitetura-em-camadas), [Estrutura](#estrutura-principal-do-código), [Domínio técnico](#domínio-técnico) |
| [🔐 Segurança e Persistência](#-segurança-e-persistência) | [API](#api-e-integração), [Segurança](#segurança), [Banco](#persistência-e-banco)                                    |
| [✅ Qualidade e Operação](#-qualidade-e-operação)         | [Testes](#testes-e-qualidade), [Infraestrutura](#operação-e-infraestrutura), [Conclusão](#conclusão-técnica)         |

---

## 🎯 Contexto e Stack

### Contexto técnico

O AutoFlow é uma aplicação Java 25 com Spring Boot 4.1.0. A solução foi construída como um monólito modular voltado a atender o processo de oficina mecânica, com regras centralizadas em serviços e entidades de domínio.

### Stack tecnológica

| Categoria           | Tecnologia               |
| ------------------- | ------------------------ |
| Linguagem           | Java 25                  |
| Framework principal | Spring Boot 4.1.0        |
| Web                 | Spring Web MVC           |
| Persistência        | Spring Data JPA          |
| Banco de dados      | PostgreSQL               |
| Segurança           | Spring Security + JWT    |
| Documentação de API | Springdoc OpenAPI        |
| Mapeamento          | MapStruct                |
| Boilerplate         | Lombok                   |
| Build               | Maven                    |
| Conteinerização     | Docker / Docker Compose  |
| Qualidade           | SonarQube + JaCoCo       |
| Testes              | JUnit / Spring Boot Test |

---

## 🏗️ Arquitetura e Código

### Arquitetura em camadas

```text
Controller
   ↓
Service
   ↓
Domain / Entity / Rules
   ↓
Repository (JPA)
   ↓
PostgreSQL
```

**Camadas do projeto:**

- `application`: serviços, DTOs, regras de caso de uso;
- `domain`: entidades, enums, modelos e regras de negócio;
- `interfaces`: controllers REST;
- `infrastructure`: segurança, mappers, infraestrutura técnica;
- `exception`: tratamento centralizado de erros.

### Estrutura principal do código

```text
src/
├── main/java/br/com/autoflow/
│   ├── application/
│   │   ├── dto/
│   │   └── service/
│   ├── domain/
│   │   ├── enums/
│   │   ├── model/
│   │   └── repository/
│   ├── exception/
│   ├── infrastructure/
│   │   ├── mapper/
│   │   └── security/
│   └── interfaces/
│       └── controller/
├── test/java/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── ARCHITECTURE.md
├── README.md
└── docs/
```

### Domínio técnico

#### Entidades centrais

- `OrdemServico`
- `Orcamento`
- `OrcamentoServico`
- `OrcamentoItem`
- `OsServico`
- `Veiculo`
- `Funcionario`
- `Usuario`
- `Estoque`

#### Enums de status

- `StatusOS`
- `StatusOrcamento`
- `StatusPagamento`

Esses elementos fazem a base do fluxo principal de atendimento e de tomada de decisão no sistema.

---

## 🔐 Segurança e Persistência

### API e integração

#### Endpoints principais

- autenticação;
- ordem de serviço;
- orçamento;
- veículo;
- funcionário;
- estoque;
- serviços.

#### OpenAPI

A documentação de API é gerada por Springdoc OpenAPI, permitindo acesso via interface Swagger no ambiente executando a aplicação.

### Segurança

#### Autenticação

A aplicação usa JWT com política stateless. O token é gerado e validado em componentes de segurança do projeto.

#### Autorização

A autorização é realizada por perfil e caminho de rota. Há separação funcional entre admin, mecânico e cliente, com regras de acesso definidas no mecanismo de segurança do Spring.

**Medidas observadas:**

- codificação de senha com `BCryptPasswordEncoder`;
- filtro de autenticação antes da autenticação padrão;
- controle de acesso por perfil;
- tratamento centralizado de exceções da API;
- validação de entrada por Bean Validation.

### Persistência e banco

#### JPA

A aplicação usa Spring Data JPA com entidades persistidas em banco relacional. O domínio conta com relações entre OS, orçamento, itens, serviços e histórico.

#### PostgreSQL

O projeto referencia PostgreSQL como banco principal e a conexão depende de variáveis de ambiente no ambiente de execução.

**Validações e regras no modelo:**

- alteração de status da OS;
- aprovação e rejeição do orçamento;
- transição de etapas e restrições do domínio;
- regras de existência de orçamento para avanço;
- pagamento pendente para entrega;
- reserva e devolução de peças.

---

## ✅ Qualidade e Operação

### Testes e qualidade

#### Testes automatizados

Os testes estão em `src/test/java` e cobrem parte dos módulos principais do sistema, incluindo autenticação, segurança, serviços e controladores.

#### JaCoCo

O projeto configura a coleta de cobertura via JaCoCo, permitindo relatórios de cobertura e integração com Sonar.

#### SonarQube

Há configuração de análise via SonarQube e execução com Docker Compose.

```bash
docker compose up -d sonarqube
./mvnw clean verify
./mvnw sonar:sonar -Dsonar.host.url=http://localhost:9000
```

### Operação e infraestrutura

#### Dockerfile

O projeto possui um `Dockerfile` multistage:

- etapa de build com Maven + JDK;
- etapa de runtime com JRE para execução do jar compilado.

#### Docker Compose

O compose define a aplicação e um ambiente de SonarQube. Há também a configuração do banco PostgreSQL em ambiente externo/variável.

#### Execução local

```bash
./mvnw clean package
./mvnw spring-boot:run
```

**No Windows:**

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

### Conclusão técnica

A solução atual do AutoFlow está bem alinhada a um monólito de negócio focado em oficina mecânica, com a ordem de serviço como centro operacional e o orçamento/estoque como pilares de decisão. A arquitetura combina separação em camadas, controle de regras em domínio e serviços, persistência relacional, segurança JWT e automatização por agendamento, tudo em um conjunto coerente e pronto para evoluir.

---
