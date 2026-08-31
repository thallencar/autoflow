# Documentação de Negócio do AutoFlow

![V1.1.0](https://img.shields.io/badge/V1.1.0-gray?style=for-the-badge)

_Visão do domínio, dos subdomínios e das regras de negócio identificadas no sistema atual._

---

## 🗄️ Seções do Documento

| Seção                                       | Subseções                                                                       |
| ------------------------------------------- | ------------------------------------------------------------------------------- |
| [🎯 Visão do Negócio](#-visão-do-negócio)   | [Objetivo](#objetivo-do-negócio), [Domínio](#domínio-e-subdomínios)             |
| [🏗️ Atores e Fluxos](#-atores-e-fluxos)     | [Atores](#atores-do-processo), [Fluxos](#fluxos-principais)                     |
| [💼 Regras de Negócio](#-regras-de-negócio) | [Regras](#regras-de-negócio-relevantes), [Observações](#observações-de-negócio) |
| [📚 Referência](#-referência)               | [Linguagem ubíqua](#linguagem-ubíqua), [Resumo](#resumo-do-domínio)             |

---

## 🎯 Visão do Negócio

### Objetivo do Negócio

O AutoFlow é um sistema de gestão operacional para oficina mecânica. A atividade principal está centrada na ordem de serviço, que reúne diagnóstico, orçamento, execução, pagamento e entrega do veículo.

**Propósito:**

- centralizar o atendimento da oficina;
- controlar o fluxo operacional do veículo desde a recepção até a entrega;
- organizar regras de orçamento, estoque e pagamento;
- fornecer histórico e métricas para decisão operacional.

### Domínio e subdomínios

#### Subdomínio principal

**Gestão da ordem de serviço e do atendimento automotivo**

Este é o núcleo do negócio. A ordem de serviço atua como unidade de trabalho do processo e integra:

- cliente;
- veículo;
- mecânico/funcionário;
- diagnóstico;
- orçamento;
- execução;
- pagamento;
- entrega do veículo.

#### Subdomínios genéricos

**Cadastro e gestão operacional**

- cadastro de clientes;
- cadastro de veículos;
- cadastro de funcionários;
- cadastro de serviços;
- histórico por veículo.

**Gestão financeira do atendimento**

- orçamento;
- aprovação/recusa;
- cálculo de valores;
- cobrança e status de pagamento;
- taxa de permanência.

#### Subdomínios de suporte

**Estoque e peças**

- controle de itens;
- reserva de peças;
- baixa e cancelamento de estoque;
- alerta de estoque baixo de peças compartilhadas.

**Operação e acompanhamento**

- agendamento;
- fila virtual;
- métricas de tempo;
- status de execução;
- regras agendadas de cancelamento e abandono técnico.

---

## 🏗️ Atores e Fluxos

### Atores do processo

| Papel                  | Descrição                                                                                                                                                |
| ---------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Admin do sistema       | Realiza gestão administrativa, acesso e operação da oficina. O código trata a recepção como papel de administração do sistema, não como perfil separado. |
| Mecânico / funcionário | Realiza diagnóstico, acompanha a execução da OS e pode ser alocado a uma ordem de serviço.                                                               |
| Cliente                | Aprova ou recusa orçamentos e acompanha o estado do atendimento.                                                                                         |

### Fluxos principais

#### Fluxo da ordem de serviço

```text
RECEBIDA
  ↓
EM_DIAGNOSTICO
  ↓
AGUARDANDO_APROVACAO
  ↓
ORCAMENTO_APROVADO
  ↓
EM_EXECUCAO
  ↓
FINALIZADA
  ↓
ENTREGUE
```

#### Fluxo de orçamento

```text
PENDENTE
  ↓
APROVADO / RECUSADO / EXPIRADO / CANCELADO
```

#### Fluxo de execução e estoque

- o orçamento gera serviços e itens;
- itens podem ser reservados e depois vendidos ou cancelados;
- serviços aprovados são carregados para execução da ordem;
- a execução exige a continuidade da operação da OS;
- os itens reservados ficam vinculados enquanto a OS estiver ativa.

---

## 💼 Regras de Negócio

### Regras de negócio relevantes

| Identificador | Regra                     | Descrição                                                                                                                                                                                                                                         |
| ------------- | ------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| RN-01         | Serviço duplicado         | A ordem de serviço não pode possuir o mesmo serviço mais de uma vez na execução.                                                                                                                                                                  |
| RN-02         | Mecânico único por vez    | O atendimento exige alocação clara do funcionário responsável. A troca de mecânico é tratada dentro da execução da OS.                                                                                                                            |
| RN-03         | Orçamento obrigatório     | Se a OS tenta avançar após o diagnóstico, deve existir orçamento vinculado ao atendimento.                                                                                                                                                        |
| RN-04         | Entrega bloqueada         | A OS só pode ser entregue se o status de pagamento não estiver pendente.                                                                                                                                                                          |
| RN-05         | Status do orçamento       | Apenas orçamentos em status pendente e não expirados podem ser aprovados ou recusados.                                                                                                                                                            |
| RN-06         | Recalculo financeiro      | O valor total do orçamento é recalculado com base em mão de obra e itens associados.                                                                                                                                                              |
| RN-07         | Aprovação do cliente      | A aprovação do orçamento é tratada como ação do cliente em canal do aplicativo, conforme o fluxo referência do sistema.                                                                                                                           |
| RN-08         | Orçamento complementar    | Um novo orçamento complementar só é gerado quando já existe um orçamento inicial para a OS.                                                                                                                                                       |
| RN-09         | Pausa de complementar     | Quando um orçamento complementar é gerado, a OS aguarda aprovação e fica pausada até a confirmação do cliente.                                                                                                                                    |
| RN-10         | Expiração do complementar | O orçamento complementar tem prazo de 24 horas para aceite ou recusa.                                                                                                                                                                             |
| RN-11         | Retorno de estoque        | Se o cliente recusa o orçamento, os itens reservados são devolvidos ao estoque.                                                                                                                                                                   |
| RN-12         | Reserva ativa             | A reserva de item não subsiste após o encerramento da OS ou na ausência de atividade válida.                                                                                                                                                      |
| RN-13         | Limite do pátio           | Se o pátio estiver lotado ou a equipe estiver sobrecarregada, a oficina para de receber carros sem agendamento. O código implementado considera um limite de 15 veículos no pátio.                                                                |
| RN-14         | Peça encomendada          | Quando a peça precisa ser encomendada, há ação de comunicação ao cliente e a OS é suspensa até a chegada do material.                                                                                                                             |
| RN-15         | Histórico por veículo     | É necessário garantir que o histórico de manutenções e atendimentos de um veículo possa ser consultado por placa/veículo.                                                                                                                         |
| RN-16         | Cancelamento automático   | A aplicação executa varredura diária às 08:00 para verificar orçamentos pendentes. Quando o prazo é excedido, o sistema aplica a regra de cancelamento automático. O comportamento implementado atualmente é: 3 dias e R$ 30,00 por dia excedido. |
| RN-17         | Abandono técnico          | A aplicação executa varredura diária às 09:00 para verificar pendências prolongadas em OS aguardando aprovação. O comportamento identificado hoje: a situação é marcada após 60 dias de pendência.                                                |
| RN-18         | Métricas de tempo         | O sistema registra tempo de diagnóstico, execução e finalização. São calculadas médias para acompanhamento do fluxo da operação.                                                                                                                  |
| RN-19         | Estoque crítico           | O projeto implementa foco em peças compartilhadas para alerta de estoque baixo.                                                                                                                                                                   |

### Observações de negócio

#### O que está como evolução futura

- gateway de pagamento externo;
- notificações automáticas para cliente e mecânico;
- canal de comunicação de confirmação de entrega e cobrança em fluxo integrado.

#### O que foi tratado como débito técnico

- a regra de quantidade mínima de estoque não foi confirmada como regra de negócio concluída;
- a recepção foi tratada como operação administrativa do sistema e não como perfil separado;
- a lógica de cancelamento/abandono técnico está implementada como regra agendada, mas requer validação de negócio para definição final do prazo e da política de comunicação.

---

## 📚 Referência

### Linguagem ubíqua

| Termo                  | Significado                                                        |
| ---------------------- | ------------------------------------------------------------------ |
| Ordem de Serviço       | Unidade central do atendimento e da execução da oficina.           |
| Diagnóstico            | Registro informado pelo técnico sobre o problema do veículo.       |
| Orçamento              | Proposta financeira e técnica para o atendimento.                  |
| Orçamento complementar | Alteração ou reforço do orçamento inicial.                         |
| Serviço                | Ação executada ou planejada sobre o veículo.                       |
| Reserva de estoque     | Separação de item para o atendimento em curso.                     |
| Entrega                | Encerramento do atendimento com o pagamento e a mudança de status. |
| Cancelamento           | Encerramento da OS por regra de negócio ou decisão operacional.    |
| Abandono técnico       | Situação de pendência prolongada da OS.                            |
| Pagamento              | Situação financeira da ordem e critério para entrega.              |

### Resumo do domínio

O AutoFlow é um sistema de operação de oficina mecânica com foco em:

- atendimento ao cliente;
- diagnóstico técnico e manutenção;
- orçamento e aprovação do cliente;
- execução em oficina;
- controle de estoque e peças;
- pagamento e entrega;
- histórico, métricas e operação administrativa.

O modelo principal é orientado pela ordem de serviço, enquanto o orçamento, o estoque, os serviços, o pagamento e o histórico funcionam como subdomínios que sustentam a execução do negócio.

---
