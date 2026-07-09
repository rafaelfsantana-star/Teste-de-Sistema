package com.bibliotech.selenium;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

public class UsuarioSeleniumTest extends BaseSeleniumTest {

    @Test
    @DisplayName("TS-006: Deve cadastrar novo usuário com sucesso")
    void deveCadastrarNovoUsuario() {
        driver.get("http://localhost:8080/usuarios/novo");

        driver.findElement(By.id("nome")).sendKeys("Carlos Teste");
        driver.findElement(By.id("email")).sendKeys("carlos.teste@email.com");
        driver.findElement(By.id("cpf")).sendKeys("555.666.777-88");
        driver.findElement(By.id("senha")).sendKeys("senha123");
        new Select(driver.findElement(By.id("tipo"))).selectByVisibleText("Aluno");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/usuarios"));
        WebElement alert = driver.findElement(By.cssSelector(".alert-success"));
        assertTrue(alert.getText().contains("Usuário salvo com sucesso"));
    }

    @Test
    @DisplayName("TS-007: Deve exibir erro ao cadastrar usuário com CPF inválido")
    void deveExibirErroCPFInvalido() {
        driver.get("http://localhost:8080/usuarios/novo");

        driver.findElement(By.id("nome")).sendKeys("Teste CPF");
        driver.findElement(By.id("email")).sendKeys("cpf.teste@email.com");
        driver.findElement(By.id("cpf")).sendKeys("123.456.789"); // formato errado
        driver.findElement(By.id("senha")).sendKeys("senha123");
        new Select(driver.findElement(By.id("tipo"))).selectByVisibleText("Aluno");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Deve permanecer na mesma página com erro
        WebElement erro = driver.findElement(By.cssSelector(".alert-danger"));
        assertTrue(erro.getText().contains("CPF inválido"));
    }
}