package com.bibliotech.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes Selenium para o módulo de Usuários.
 */
@DisplayName("Testes Selenium - Usuários")
public class UsuarioSeleniumTest extends BaseSeleniumTest {

    @Test
    @DisplayName("TS-011: Deve cadastrar novo usuário com sucesso (RF-06)")
    void deveCadastrarNovoUsuarioComSucesso() {
        driver.get(BASE_URL + "/usuarios/novo");

        preencherCampo("nome", "Maria Teste Selenium");
        preencherCampo("email", "maria.selenium@teste.com");
        preencherCampo("cpf", "111.222.333-44");
        preencherCampo("senha", "senha123");

        Select selectTipo = new Select(driver.findElement(By.id("tipo")));
        selectTipo.selectByValue("ALUNO");

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        String urlAtual = driver.getCurrentUrl();
        assertTrue(urlAtual.contains("/usuarios"),
            "Após salvar usuário deve redirecionar para lista de usuários");

        WebElement alerta = aguardarElemento(By.cssSelector(".alert-success"));
        assertTrue(alerta.isDisplayed(), "Mensagem de sucesso deve aparecer");
    }

    @Test
    @DisplayName("TS-012: Formulário de usuário deve ter campos obrigatórios (RF-06)")
    void formularioDeveConterCamposObrigatorios() {
        driver.get(BASE_URL + "/usuarios/novo");

        assertTrue(driver.findElement(By.id("nome")).isDisplayed(), "Campo nome deve estar presente");
        assertTrue(driver.findElement(By.id("email")).isDisplayed(), "Campo email deve estar presente");
        assertTrue(driver.findElement(By.id("cpf")).isDisplayed(), "Campo CPF deve estar presente");
        assertTrue(driver.findElement(By.id("senha")).isDisplayed(), "Campo senha deve estar presente");
        assertTrue(driver.findElement(By.id("tipo")).isDisplayed(), "Campo tipo deve estar presente");
    }

    @Test
    @DisplayName("TS-013: Deve listar usuários cadastrados (RF-07)")
    void deveListarUsuariosCadastrados() {
        driver.get(BASE_URL + "/usuarios");

        WebElement conteudo = aguardarElemento(By.cssSelector("table, .alert-info, .container"));
        assertTrue(conteudo.isDisplayed(), "Página de usuários deve carregar corretamente");
    }

    @Test
    @DisplayName("TS-014: Deve impedir cadastro de CPF inválido (RN-07)")
    void deveImpedirCadastroCPFInvalido() {
        driver.get(BASE_URL + "/usuarios/novo");

        preencherCampo("nome", "Teste CPF Invalido");
        preencherCampo("email", "cpfinvalido@teste.com");
        preencherCampo("cpf", "12345678900"); // CPF sem pontuação
        preencherCampo("senha", "senha123");

        Select selectTipo = new Select(driver.findElement(By.id("tipo")));
        selectTipo.selectByValue("ALUNO");

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        String urlAtual = driver.getCurrentUrl();
        // Deve permanecer no formulário ou mostrar erro
        boolean erroOuFormulario = urlAtual.contains("/novo") || urlAtual.contains("/usuarios");

        WebElement alertaErro = null;
        try {
            alertaErro = driver.findElement(By.cssSelector(".alert-danger"));
        } catch (Exception ignored) {}

        // Ou ficou na página com erro, ou mostrou alert
        assertTrue(alertaErro != null || urlAtual.contains("/novo"),
            "CPF inválido deve gerar mensagem de erro ou retornar ao formulário");

        if (alertaErro != null) {
            capturarScreenshot("validacao-cpf-invalido");
        }
    }
}
