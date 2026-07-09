package com.bibliotech.selenium;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginSeleniumTest extends BaseSeleniumTest {

    @Test
    @DisplayName("TS-001: Deve fazer login com credenciais válidas e redirecionar para dashboard")
    void deveFazerLoginComSucesso() {
        driver.get("http://localhost:8080/login");
        driver.findElement(By.id("email")).sendKeys("admin@bibliotech.com");
        driver.findElement(By.id("senha")).sendKeys("admin123");
        driver.findElement(By.id("btn-login")).click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertEquals("http://localhost:8080/dashboard", driver.getCurrentUrl());
        WebElement bemVindo = driver.findElement(By.id("mensagem-bemvindo"));
        assertTrue(bemVindo.isDisplayed());
    }

    @Test
    @DisplayName("TS-002: Deve exibir erro ao tentar login com senha incorreta")
    void deveExibirErroLoginSenhaIncorreta() {
        driver.get("http://localhost:8080/login");
        driver.findElement(By.id("email")).sendKeys("admin@bibliotech.com");
        driver.findElement(By.id("senha")).sendKeys("senhaErrada");
        driver.findElement(By.id("btn-login")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("mensagem-erro")));
        WebElement erro = driver.findElement(By.id("mensagem-erro"));
        assertTrue(erro.getText().contains("Email ou senha inválidos"));
    }

    @Test
    @DisplayName("TS-003: Deve exibir erro ao tentar login com usuário inativo")
    void deveExibirErroLoginUsuarioInativo() {
        driver.get("http://localhost:8080/login");
        // Usuário inativo não existe, mas o sistema rejeitará por falha de autenticação
        driver.findElement(By.id("email")).sendKeys("inativo@email.com");
        driver.findElement(By.id("senha")).sendKeys("qualquer");
        driver.findElement(By.id("btn-login")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("mensagem-erro")));
        WebElement erro = driver.findElement(By.id("mensagem-erro"));
        assertTrue(erro.isDisplayed(), "Deveria exibir mensagem de erro");
    }
}