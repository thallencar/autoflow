# AutoFlow
Sistema para gerenciamento dos processos de uma oficina mecânica, centralizando o ciclo de vida das Ordens de Serviço (OS), desde a abertura até a entrega do veículo.

## Contexto
O AutoFlow foi desenvolvido para substituir processos manuais e descentralizados, como anotações e planilhas, por um fluxo centralizado de atendimento e execução de serviços.

O sistema fornece suporte para:

- Cadastro e gerenciamento de clientes e veículos;
- Abertura e acompanhamento de Ordens de Serviço;
- Registro de diagnóstico e serviços necessários;
- Elaboração e acompanhamento de orçamentos;
- Reserva, baixa e devolução de peças e insumos;
- Registro e acompanhamento da execução dos serviços;
- Controle do pagamento da OS;
- Registro da entrega do veículo;
- Notificações relacionadas ao andamento da OS;
- Registro de informações necessárias para acompanhamento do tempo de execução.

---

## Fluxo da Ordem de Serviço

A OS representa o principal fluxo do sistema:

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

Em determinadas situações, como fluxos e exceções. a OS pode assumir o status de ``Cancelada``.

## Principais Atores

### Cliente
Interage com o sistema para:

- Acompanhar sua Ordem de Serviço;
- Receber orçamentos;
- Aprovar ou rejeitar orçamentos;
- Realizar o pagamento;
- Retirar o veículo.

## Recepcionista
Utiliza o sistema para:

- Cadastrar e identificar clientes;
- Cadastrar e vincular veículos;
- Abrir Ordens de Serviço;
- Acompanhar o andamento das OS;
- Registrar e confirmar informações relacionadas ao pagamento;
- Registrar ou confirmar a entrega do veículo.

## Mecânico
Utiliza o sistema para:

- Consultar novas Ordens de Serviço;
- Registrar diagnósticos;
- Identificar serviços, peças e insumos necessários;
- Participar da elaboração de orçamentos;
- Registrar necessidades adicionais durante a execução;
- Registrar informações da execução dos serviços;
- Acompanhar alertas relacionados ao estoque.

## Regras de Negócio


## Linguagem do Domínio
| Termo                      | Significado                                                           |
| -------------------------- | --------------------------------------------------------------------- |
| **OS / Ordem de Serviço**  | Registro que acompanha o atendimento e execução do serviço do veículo |
| **Diagnóstico**            | Avaliação técnica para identificar necessidades do veículo            |
| **Orçamento**              | Proposta de serviços e peças para a OS                                |
| **Orçamento inicial**      | Proposta baseada no diagnóstico inicial                               |
| **Orçamento complementar** | Proposta para necessidades identificadas durante a execução           |
| **Serviço**                | Trabalho realizado no veículo                                         |
| **Peça**                   | Item utilizado durante a execução                                     |
| **Insumo**                 | Material consumível utilizado durante a execução                      |
| **Reserva**                | Separação temporária de itens do estoque para uma OS                  |
| **Baixa**                  | Retirada definitiva de itens do estoque                               |
| **Estoque mínimo**         | Limite utilizado para geração de alertas                              |
| **Aguardando peça**        | Situação em que a execução depende da disponibilidade de uma peça     |
| **Aprovação**              | Aceite do orçamento pelo cliente                                      |
| **Rejeição**               | Recusa do orçamento pelo cliente                                      |
| **Pagamento**              | Quitação do valor devido pela OS                                      |
| **Entrega**                | Devolução do veículo ao cliente                                       |

## Métricas

## Notificações

## Arquitetura

O projeto é implementado como um **monolito em arquitetura em camadas**, organizado em:

```text
src/main/java/br/com/autoflow/

├── interface/
├── application/
├── domain/
└── infrastructure/
```
A aplicação possui uma única unidade de execução e deploy, mantendo a separação de responsabilidades entre as camadas.

```text
Interface → Application → Domain
                  ↓
           Infrastructure
```

As regras de negócio são mantidas no domínio, enquanto detalhes de persistência e infraestrutura permanecem isolados das regras centrais da aplicação.

## Estrutura do projeto

```text
src/
└── main/
    ├── java/
    │   └── br/com/autoflow/
    │       ├── interface/
    │       ├── application/
    │       ├── domain/
    │       └── infrastructure/
    │
    └── resources/
        └── application.properties
```

### Tecnologias
- **Java 25** — linguagem principal
- **Spring Boot 4** — framework da aplicação
- **Spring Web** — API REST
- **Spring Data JPA** — persistência
- **Hibernate** — ORM
- **PostgreSQL** — banco de dados relacional
- **Supabase** — hospedagem inicial do PostgreSQL
- **Maven** — build e gerenciamento de dependências
- **Maven Wrapper** — execução padronizada do Maven
- **OpenAPI / Swagger** — documentação da API

## Executando com Docker

É necessário ter o Docker instalado localmente para executar o projeto via Docker Compose.

Se for a primeira vez, execute:

```
docker compose up --build
```

Se já houve build anterior e você quiser subir apenas os containers novamente, use:

```
docker compose up
```

Após iniciar, a documentação Swagger estará disponível em:

```
http://localhost:8080/swagger-ui/index.html#/
```

Para encerrar, use Ctrl+C ou `docker compose down`.

## SonarQube local

Para acompanhar qualidade de código e cobertura localmente com SonarQube, siga os passos abaixo.

### Pré-requisitos

- Docker instalado e em execução
- Docker Compose disponível
- Java 25 e Maven instalados na máquina
- Projeto clonado localmente

### 1) Subir o SonarQube

```bash
docker compose up -d sonarqube
```

Acesse a interface web em:

```text
http://localhost:9000
```

Na primeira vez, faça login com:

```text
usuário: admin
senha: admin
```

Recomendado: altere a senha e crie um projeto local no Sonar.

### 2) Gerar a cobertura de testes

O projeto já está configurado com JaCoCo e gera o relatório em:

```text
target/site/jacoco/jacoco.xml
```

Execute:

```bash
mvn clean verify
```

Isso gera os relatórios de testes e cobertura para o Sonar consumir.

### 3) Enviar análise para o SonarQube

Para analisar o projeto no Sonar local, execute:

```bash
mvn sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=admin \
  -Dsonar.password=admin
```

Se preferir usar um token do projeto no Sonar:

```bash
mvn sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=SEU_TOKEN_AQUI
```

### 4) Observações

- O comando `docker compose up` sobe todos os serviços do projeto (aplicação + SonarQube), conforme necessário.
- Para subir apenas o SonarQube, use `docker compose up -d sonarqube`.
- Para o build local em CI ou desenvolvimento, o comando recomendado é:

```bash
mvn clean verify
```

- Para rodar tudo em Docker pela primeira vez, ainda use:

```bash
docker compose up --build
```

- Se já tiver o ambiente montado, o uso mais rápido é:

```bash
docker compose up
```