# Relatório do Grupo — BiblioTech

Data: 2026-07-08

Integrantes:

- Rafael Fiuza de Santana
- Yuri Bispo Figueiredo
- Renan Omena Teles

## Descrição do Projeto

O BiblioTech é um sistema de gestão de biblioteca desenvolvido em Java com Spring Boot. O objetivo do projeto é permitir o cadastro e gerenciamento de livros, usuários e empréstimos, bem como gerar relatórios e evidências de testes. Principais pontos:

- Funcionalidades: cadastro de livros, gerenciamento de usuários, registro de empréstimos e devoluções, cálculo de multas e interface web para administração.
- Tecnologias: Java, Spring Boot, Thymeleaf (templates), Maven.
- Testes e evidências: inclui testes unitários e testes automatizados (Selenium), além de relatórios de execução e capturas de tela na pasta `evidencias`.

## Observações

Este arquivo foi gerado a pedido do usuário para documentar os integrantes e resumir o projeto.

## Resumo Executivo

1. Arquitetura: Aplicação Java com Spring Boot seguindo padrão MVC, usando Thymeleaf para templates e Maven para gerenciamento de dependências e build.

2. Como rodar (pré-requisitos): instalar JDK (11+), Maven e, para testes Selenium, um WebDriver compatível (ex.: ChromeDriver). Comandos:

```
mvn clean package
mvn spring-boot:run
// ou
java -jar target/*.jar

// rodar testes
mvn test
```

3. Testes e evidências: o projeto contém testes unitários e testes automatizados (Selenium). Relatórios e capturas estão em `evidencias` e `evidencias/reports/surefire.html`.

4. Dependências e build: gestão via `pom.xml` (Maven). Para inspecionar dependências: `mvn dependency:tree`.

5. Observações finais: alguns arquivos mostraram avisos de conversão LF/CRLF no Windows — isso é comum; revise o `.gitattributes` se necessário. Para dúvidas, consulte `README_GRUPO.md` ou solicite que eu inclua instruções adicionais.