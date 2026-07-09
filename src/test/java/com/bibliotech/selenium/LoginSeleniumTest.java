package com.bibliotech.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes Selenium para o módulo de Login.
 *
 * Bugs identificados:
 * - BUG-003: Login com credenciais válidas falha porque autenticar() usa == para comparar Strings
 */
@DisplayName("Testes Selenium - Login")
public class LoginSeleniumTest extends BaseSeleniumTest {

    @Test
    @DisplayName("TS-001: Página de login deve estar acessível em /login")
    void paginaDeLoginDeveEstarAcessivel() {
        driver.get(BASE_URL + "/login");

        String titulo = driver.getTitle();
        assertTrue(titulo.contains("BiblioTech") || titulo.contains("Login"),
            "Título da página de login deve conter 'BiblioTech' ou 'Login'");

        WebElement campoEmail = driver.findElement(By.id("email"));
        WebElement campoSenha = driver.findElement(By.id("senha"));
        WebElement btnLogin = driver.findElement(By.id("btn-login"));

        assertTrue(campoEmail.isDisplayed(), "Campo email deve estar visível");
        assertTrue(campoSenha.isDisplayed(), "Campo senha deve estar visível");
        assertTrue(btnLogin.isDisplayed(), "Botão de login deve estar visível");
    }

    @Test
    @DisplayName("TS-002: Login com credenciais válidas deve redirecionar para /dashboard (RF-15) [BUG-003]")
    void loginComCredenciaisValidasDeveRedirecionarParaDashboard() {
        fazerLogin(ADMIN_EMAIL, ADMIN_SENHA);

        String urlAtual = driver.getCurrentUrl();

        assertTrue(urlAtual.contains("/dashboard"),
            "BUG-003: Após login válido deveria redirecionar para /dashboard, mas foi para: " + urlAtual +
            ". O bug usa == em vez de .equals() para comparar a senha no UsuarioService.autenticar().");

        if (!urlAtual.contains("/dashboard")) {
            capturarScreenshot("bug-003-login-falha-credenciais-validas");
        }
    }

    @Test
    @DisplayName("TS-003: Login com senha incorreta deve exibir mensagem de erro")
    void loginComSenhaIncorretaDeveExibirErro() {
        fazerLogin(ADMIN_EMAIL, "senhaErrada123");

        String urlAtual = driver.getCurrentUrl();
        assertTrue(urlAtual.contains("/login"),
            "Deve permanecer na página de login após credenciais inválidas");

        WebElement mensagemErro = aguardarElemento(By.id("mensagem-erro"));
        assertTrue(mensagemErro.isDisplayed(),
            "Mensagem de erro deve ser exibida");
        assertTrue(mensagemErro.getText().toLowerCase().contains("inválido") ||
                   mensagemErro.getText().toLowerCase().contains("invalido"),
            "Mensagem de erro deve indicar credenciais inválidas");
    }

    @Test
    @DisplayName("TS-004: Login com email inexistente deve exibir mensagem de erro")
    void loginComEmailInexistenteDeveExibirErro() {
        fazerLogin("naoexiste@email.com", "qualquersenha");

        String urlAtual = driver.getCurrentUrl();
        assertTrue(urlAtual.contains("/login"),
            "Deve permanecer na página de login com email inexistente");

        WebElement mensagemErro = aguardarElemento(By.id("mensagem-erro"));
        assertTrue(mensagemErro.isDisplayed(),
            "Mensagem de erro deve ser exibida para email inexistente");
    }

    @Test
    @DisplayName("TS-005: Raiz do sistema (/) deve redirecionar para /login")
    void raizDoSistemaDeveRedirecionarParaLogin() {
        driver.get(BASE_URL + "/");

        String urlAtual = driver.getCurrentUrl();
        assertTrue(urlAtual.contains("/login"),
            "A raiz '/' deve redirecionar para /login");
    }
}
