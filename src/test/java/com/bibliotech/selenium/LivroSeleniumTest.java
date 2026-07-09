package com.bibliotech.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes Selenium para o módulo de Livros.
 *
 * Bugs identificados:
 * - BUG-004: Sistema permite cadastro de livros com ISBN duplicado (RN-02)
 */
@DisplayName("Testes Selenium - Livros")
public class LivroSeleniumTest extends BaseSeleniumTest {

    @Test
    @DisplayName("TS-006: Deve cadastrar novo livro com sucesso (RF-01)")
    void deveCadastrarNovoLivroComSucesso() {
        // Como o login tem bug, acessa direto se já autenticado via session trick
        // Neste teste, verificamos o fluxo completo
        driver.get(BASE_URL + "/login");
        driver.findElement(By.id("email")).sendKeys(ADMIN_EMAIL);
        driver.findElement(By.id("senha")).sendKeys(ADMIN_SENHA);
        driver.findElement(By.id("btn-login")).click();

        // Se login falhar por causa do bug, vai para /livros/novo diretamente
        // (pois o sistema não tem autenticação forçada nos Controllers)
        driver.get(BASE_URL + "/livros/novo");

        preencherCampo("titulo", "Test Driven Development");
        preencherCampo("autor", "Kent Beck");
        preencherCampo("isbn", "978-0321146533");
        preencherCampo("editora", "Addison-Wesley");
        preencherCampo("ano", "2003");
        preencherCampo("quantidadeExemplares", "5");

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        String urlAtual = driver.getCurrentUrl();
        assertTrue(urlAtual.contains("/livros"),
            "Após salvar livro deve redirecionar para lista de livros");

        WebElement mensagemSucesso = aguardarElemento(By.cssSelector(".alert-success"));
        assertTrue(mensagemSucesso.isDisplayed(),
            "Mensagem de sucesso deve ser exibida");
    }

    @Test
    @DisplayName("TS-007: Deve listar livros cadastrados (RF-02)")
    void deveListarLivrosCadastrados() {
        driver.get(BASE_URL + "/livros");

        WebElement tabelaOuMensagem = aguardarElemento(
            By.cssSelector("table, .alert-info"));
        assertTrue(tabelaOuMensagem.isDisplayed(),
            "Página de livros deve exibir tabela ou mensagem de lista vazia");
    }

    @Test
    @DisplayName("TS-008: Formulário de novo livro deve exibir todos os campos obrigatórios")
    void formularioDeveExibirCamposObrigatorios() {
        driver.get(BASE_URL + "/livros/novo");

        assertTrue(driver.findElement(By.id("titulo")).isDisplayed(), "Campo título deve estar presente");
        assertTrue(driver.findElement(By.id("autor")).isDisplayed(), "Campo autor deve estar presente");
        assertTrue(driver.findElement(By.id("isbn")).isDisplayed(), "Campo ISBN deve estar presente");
        assertTrue(driver.findElement(By.id("quantidadeExemplares")).isDisplayed(),
            "Campo quantidade de exemplares deve estar presente");
    }

    @Test
    @DisplayName("TS-009: Deve impedir cadastro de livro com ISBN duplicado (RN-02) [BUG-004]")
    void deveImpedirCadastroComISBNDuplicado() {
        String isbn = "978-1111111111";

        // Cadastrar primeiro livro
        cadastrarLivro("Livro Original", "Autor A", isbn);

        // Tentar cadastrar segundo com mesmo ISBN
        driver.get(BASE_URL + "/livros/novo");
        preencherCampo("titulo", "Livro Duplicado");
        preencherCampo("autor", "Autor B");
        preencherCampo("isbn", isbn);
        preencherCampo("editora", "Editora");
        preencherCampo("ano", "2022");
        preencherCampo("quantidadeExemplares", "2");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        String urlAtual = driver.getCurrentUrl();

        // O sistema deveria exibir erro, não sucesso
        List<WebElement> alertasSucesso = driver.findElements(By.cssSelector(".alert-success"));
        boolean temSucesso = alertasSucesso.stream().anyMatch(e -> e.isDisplayed() &&
            e.getText().toLowerCase().contains("sucesso"));

        assertFalse(temSucesso,
            "BUG-004: Sistema não deveria permitir cadastro de ISBN duplicado (RN-02). " +
            "O LivroService.salvar() não valida unicidade do ISBN antes de salvar.");

        if (temSucesso) {
            capturarScreenshot("bug-004-isbn-duplicado-permitido");
        }
    }

    @Test
    @DisplayName("TS-010: Busca por título deve retornar resultados corretos (RF-04)")
    void buscaPorTituloDeveRetornarResultados() {
        // Garante que existe um livro com palavra conhecida
        cadastrarLivro("Java Programming Guide", "James Gosling", "978-9999999991");

        driver.get(BASE_URL + "/livros?busca=Java&tipo=titulo");

        WebElement tabela = aguardarElemento(By.cssSelector("table"));
        assertTrue(tabela.isDisplayed(), "Tabela de resultados deve ser exibida");

        String conteudo = tabela.getText();
        assertTrue(conteudo.contains("Java"),
            "Resultado da busca deve conter 'Java'");
    }
}
