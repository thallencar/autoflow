# 🏛️ Documentação Arquitetural - Sistema Autoflow

Este documento descreve a arquitetura de software do sistema **Autoflow** utilizando a metodologia do **Modelo C4** com gráficos renderizados via Mermaid.

---

## 🗺️ Nível 1: Contexto do Sistema (System Context)

O Autoflow é uma plataforma centralizada para o gerenciamento de oficinas mecânicas. Neste nível macro, o sistema é tratado como uma caixa preta única, detalhando apenas como os atores interagem com ele.

```mermaid
graph TD
    admin["Administrador (Pessoa) - Gerencia a oficina, usuários, estoque e configurações do sistema."]
    mecanico["Mecânico (Pessoa) - Gerencia e atualiza ordens de serviço e veículos sob manutenção."]
    cliente["Cliente da Oficina (Pessoa) - Acompanha o status de veículos, ordens de serviço e orçamentos."]

    autoflow["Sistema Autoflow (Sistema de Software) - Plataforma central para gerenciamento de oficinas mecânicas, controlando fluxos de ordens, orçamentos e estoque."]

    admin -->|HTTPS / JSON| autoflow
    mecanico -->|HTTPS / JSON| autoflow
    cliente -->|HTTPS / JSON| autoflow

    classDef person fill:#08427b,stroke:#073b6f,color:#fff;
    classDef system fill:#1168bd,stroke:#0f5da9,color:#fff;
    class admin,mecanico,cliente person;
    class autoflow system;
```

---

## 📦 Nível 2: Contêineres (Containers)

Neste nível, dividimos o Sistema Autoflow nas suas aplicações executáveis e armazenamentos de dados reais encontrados no repositório do projeto.

```mermaid
graph TD
    users["Usuários da Oficina (Pessoa) - Administradores, Mecânicos e Clientes."]

    subgraph Fronteira do Sistema Autoflow
        api["Autoflow API (Contêiner: Java 25 / Spring Boot 4.1) - Fornece os endpoints REST para o domínio da oficina, valida regras de negócio e executa rotinas agendadas."]
        db[("Banco de Dados PostgreSQL (Contêiner: Supabase Host) - Armazena as informações persistentes do sistema.")]
    end

    users -->|HTTP / JSON - Porta 8080| api
    api -->|JDBC / Spring Data JPA| db

    classDef person fill:#08427b,stroke:#073b6f,color:#fff;
    classDef container fill:#438dd5,stroke:#3b7bba,color:#fff;
    classDef db fill:#438dd5,stroke:#3b7bba,color:#fff;
    class users person;
    class api container;
    class db db;
```

---

## 🧩 Nível 3: Componentes (Components)

Detalhamento interno do contêiner **Autoflow API**, demonstrando como as pastas e pacotes estruturais do Spring Boot se comunicam internamente.

```mermaid
graph TD
    client["Browser / API Client (Aplicação Externa) - Interface que dispara as requisições."]
    db_ext[("Banco de Dados PostgreSQL (Contêiner: Supabase) - Armazenamento relacional.")]

    subgraph Autoflow API [Contêiner Spring Boot]
        security["Filtro de Segurança (Spring Security / java-jwt) - Intercepta requisições, valida tokens JWT e gerencia perfis (ADMIN, MECANICO, CLIENTE)."]
        controllers["Controllers REST (Spring MVC @RestController) - Expõe as rotas públicas e privadas (/auth, /veiculos, /ordens-servico)."]
        services["Services de Aplicação (Spring @Service) - Orquestra e executa o fluxo das regras de negócio e validações."]
        scheduler["Agendador de Tarefas (Spring @Scheduled) - Rotinas assíncronas (cancelamentos automáticos)."]
        mappers["Mappers de Dados (MapStruct) - Converte objetos DTO para Entidades e vice-versa."]
        repos["Repositórios (Spring Data JPA) - Abstrai as operações de CRUD no banco."]
    end

    client -->|HTTP / JSON| security
    security --> controllers
    controllers --> services
    scheduler --> services
    services --> mappers
    services --> repos
    repos --> db_ext

    classDef ext fill:#999999,stroke:#888888,color:#fff;
    classDef comp fill:#85bbf0,stroke:#6f9ecb,color:#000;
    class client,db_ext ext;
    class security,controllers,services,scheduler,mappers,repos comp;
```

---

## 🔍 4. Lacunas Técnicas e Próximos Passos [TODO]

As seguintes definições arquiteturais não puderam ser extraídas diretamente do código-fonte e precisam ser validadas manualmente:

* 🔐 **[TODO: SEGURANÇA DE CREDENCIAIS]**: Mapear onde a senha de produção do banco de dados (oculta no `application.properties`) será armazenada (ex: AWS Secrets Manager, variáveis de ambiente do Supabase).
* 🚀 **[TODO: INFRAESTRUTURA E DEPLOY]**: O projeto possui um `Dockerfile` expondo a porta `8080`, mas falta documentar o ambiente onde esse contêiner rodará (ex: AWS ECS, Kubernetes, Render).
* 📧 **[TODO: INTEGRAÇÕES EXTERNAS]**: Mapear se haverá serviços de terceiros integrados no futuro (ex: APIs de envio de e-mail ou WhatsApp para avisar o cliente sobre o fim de uma Ordem de Serviço).
