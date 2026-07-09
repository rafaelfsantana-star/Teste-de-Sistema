# BiblioTech — Testes de Sistemas

**Disciplina:** Testes de Sistemas  
**Período:** 2026.1  
**Instituição:** SENAI — Campus Integrado de Manufatura e Tecnologia

---

## Integrantes do Grupo

- Amanda Dantas Laudelino
- Diego José Barbosa da Cunha
- Jhonata Enzo Silva Gomes
- Jonathas Barbosa da Anunciação
- Matheus Ryan Alves Santos

---

## Como Executar os Testes

### Pré-requisitos

- Java 17 LTS (OpenJDK)
- Apache Maven 3.2.5+
- Google Chrome instalado
- Git

### 1. Clonar o repositório

```bash
git clone https://github.com/diegojbcunha/bibliotech
cd bibliotech
```

### 2. Iniciar o sistema (necessário para os testes Selenium)

```bash
mvn spring-boot:run
```

A aplicação ficará disponível em: http://localhost:8080  
Credenciais de acesso: `admin@bibliotech.com` / `admin123`

### 3. Executar todos os testes (em outro terminal)

```bash
mvn clean test
```

### 4. Executar apenas os testes unitários

```bash
mvn test -Dtest="com.bibliotech.unit.*"
```

### 5. Executar apenas os testes Selenium

```bash
mvn test -Dtest="com.bibliotech.selenium.*"
```

### 6. Gerar o relatório HTML do Maven Surefire

```bash
mvn surefire-report:report
```

O relatório estará em: `target/site/surefire-report.html`

---

## Resultados Obtidos

| Métrica | Valor |
|---|---|
| Total de testes implementados | 37 |
| Testes unitários (JUnit 5) | 25 |
| Testes de interface (Selenium) | 12 |
| Testes aprovados | 25 (67,6%) |
| Testes reprovados | 12 (32,4%) |
| Bugs identificados e documentados | 6 |
| Requisitos funcionais testados | 15 de 15 (100%) |
| Regras de negócio testadas | 10 de 12 (83,3%) |

### Bugs Encontrados

| ID | Descrição | Severidade | Módulo |
|---|---|---|---|
| BUG-001 | Sistema permite cadastro de livros com ISBN duplicado | ALTA | Gerenciamento de Livros |
| BUG-002 | Multa calculada com R$ 3,00/dia em vez de R$ 2,00/dia | ALTA | Gerenciamento de Empréstimos |
| BUG-003 | Prazo de devolução calculado com 7 dias em vez de 14 | ALTA | Gerenciamento de Empréstimos |
| BUG-004 | Sistema permite empréstimo sem verificar disponibilidade | ALTA | Gerenciamento de Empréstimos |
| BUG-005 | Sistema permite excluir usuário com empréstimos ativos | ALTA | Gerenciamento de Usuários |
| BUG-006 | Autenticação quebrada por comparação de senha com `==` | CRÍTICA | Autenticação |

---

## Relatório Completo

O relatório de testes em PDF está disponível no repositório:  
[Relatorio_Testes_BiblioTech.pdf](./Relatorio_Testes_BiblioTech.pdf)

---

## Estrutura dos Testes

```
src/test/java/com/bibliotech/
├── unit/
│   ├── EmprestimoServiceTest.java   (TU-001 a TU-009)
│   ├── LivroServiceTest.java        (TU-010 a TU-018)
│   ├── UsuarioServiceTest.java      (TU-019 a TU-024)
│   └── DashboardServiceTest.java    (TU-025)
└── selenium/
    ├── BaseSeleniumTest.java
    ├── LoginSeleniumTest.java        (TS-001, TS-002, TS-003)
    ├── LivroSeleniumTest.java        (TS-004, TS-005, TS-011, TS-012)
    ├── UsuarioSeleniumTest.java      (TS-006, TS-007)
    ├── EmprestimoSeleniumTest.java   (TS-008, TS-009, TS-010)
    └── NavegacaoSeleniumTest.java    (TS-013, TS-014)
```
