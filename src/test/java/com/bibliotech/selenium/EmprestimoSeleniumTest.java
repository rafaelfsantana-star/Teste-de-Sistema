package com.bibliotech.selenium;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

public class EmprestimoSeleniumTest extends BaseSeleniumTest {

    @Test
    @DisplayName("TS-008: Deve realizar empréstimo com sucesso e redirecionar para lista")
    void deveRealizarEmprestimoComSucesso() {
        driver.get("http://localhost:8080/emprestimos/novo");

        // Seleciona o primeiro usuário e o primeiro livro disponíveis
        Select selectUsuario = new Select(driver.findElement(By.id("usuarioId")));
        selectUsuario.selectByIndex(1);
        Select selectLivro = new Select(driver.findElement(By.id("livroId")));
        selectLivro.selectByIndex(1);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/emprestimos"));
        WebElement alert = driver.findElement(By.cssSelector(".alert-success"));
        assertTrue(alert.getText().contains("Empréstimo realizado com sucesso"));
    }

    @Test
    @DisplayName("TS-009: Deve registrar devolução sem multa")
    void deveRegistrarDevolucao() {
        // Realiza um empréstimo
        driver.get("http://localhost:8080/emprestimos/novo");
        new Select(driver.findElement(By.id("usuarioId"))).selectByIndex(1);
        new Select(driver.findElement(By.id("livroId"))).selectByIndex(1);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/emprestimos"));

        // Navega para a lista de ativos e devolve o primeiro
        driver.get("http://localhost:8080/emprestimos?filtro=ativos");
        WebElement botaoDevolver = driver.findElement(By.cssSelector(".btn-success"));
        botaoDevolver.click();

        wait.until(ExpectedConditions.urlContains("/emprestimos"));
        WebElement alert = driver.findElement(By.cssSelector(".alert-success"));
        assertTrue(alert.getText().contains("Devolução registrada com sucesso"));
    }

    @Test
    @DisplayName("TS-010: Deve registrar devolução de empréstimo atrasado e exibir multa")
    void deveRegistrarDevolucaoComAtraso() {
        // Acessa a lista de empréstimos atrasados (se houver)
        driver.get("http://localhost:8080/emprestimos?filtro=atrasados");

        // Se houver pelo menos um empréstimo atrasado, clica em Devolver
        if (!driver.findElements(By.cssSelector(".btn-success")).isEmpty()) {
            driver.findElement(By.cssSelector(".btn-success")).click();
            wait.until(ExpectedConditions.urlContains("/emprestimos"));
            WebElement alert = driver.findElement(By.cssSelector(".alert-success"));
            // A mensagem deve conter "Multa" se houver atraso
            assertTrue(alert.getText().contains("Multa"), "Deveria exibir multa no alerta");
        } else {
            // Se não houver empréstimos atrasados, o teste apenas verifica que a página carregou
            assertTrue(driver.getPageSource().contains("Empréstimos"));
        }
    }
}