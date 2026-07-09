package com.bibliotech.selenium;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LivroSeleniumTest extends BaseSeleniumTest {

    @Test
    @DisplayName("TS-004: Deve cadastrar novo livro com sucesso")
    void deveCadastrarNovoLivro() {
        driver.get("http://localhost:8080/livros/novo");

        driver.findElement(By.id("titulo")).sendKeys("Livro Teste Selenium");
        driver.findElement(By.id("autor")).sendKeys("Autor Teste");
        driver.findElement(By.id("isbn")).sendKeys("978-1234567890");
        driver.findElement(By.id("editora")).sendKeys("Editora Teste");
        driver.findElement(By.id("ano")).sendKeys("2023");
        driver.findElement(By.id("quantidadeExemplares")).clear();
        driver.findElement(By.id("quantidadeExemplares")).sendKeys("5");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/livros"));
        WebElement alert = driver.findElement(By.cssSelector(".alert-success"));
        assertTrue(alert.getText().contains("sucesso"));
    }

    @Test
    @DisplayName("TS-005: Deve impedir cadastro de livro com ISBN duplicado")
    void deveImpedirISBNDuplicado() {
        // Cadastra primeiro livro
        driver.get("http://localhost:8080/livros/novo");
        driver.findElement(By.id("titulo")).sendKeys("Livro Original");
        driver.findElement(By.id("autor")).sendKeys("Autor Original");
        driver.findElement(By.id("isbn")).sendKeys("978-1111111111");
        driver.findElement(By.id("ano")).sendKeys("2022");
        driver.findElement(By.id("quantidadeExemplares")).clear();
        driver.findElement(By.id("quantidadeExemplares")).sendKeys("2");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/livros"));

        // Tenta cadastrar outro com mesmo ISBN
        driver.get("http://localhost:8080/livros/novo");
        driver.findElement(By.id("titulo")).sendKeys("Livro Duplicado");
        driver.findElement(By.id("autor")).sendKeys("Outro Autor");
        driver.findElement(By.id("isbn")).sendKeys("978-1111111111"); // ISBN repetido
        driver.findElement(By.id("ano")).sendKeys("2021");
        driver.findElement(By.id("quantidadeExemplares")).clear();
        driver.findElement(By.id("quantidadeExemplares")).sendKeys("3");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Comportamento correto: permanecer na página e exibir erro
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        WebElement erro = driver.findElement(By.cssSelector(".alert-danger"));
        assertTrue(erro.getText().contains("ISBN já cadastrado"),
                "Deveria exibir mensagem de ISBN duplicado");
    }

    @Test
    @DisplayName("TS-011: Deve buscar livro por título e exibir resultados")
    void deveBuscarLivroPorTitulo() {
        driver.get("http://localhost:8080/livros");
        driver.findElement(By.name("busca")).sendKeys("Clean");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".livro-item")));
        WebElement livro = driver.findElement(By.cssSelector(".livro-item"));
        assertTrue(livro.getText().contains("Clean Code"));
    }

    @Test
    @DisplayName("TS-012: Deve exibir mensagem para busca sem resultados")
    void deveExibirMensagemNenhumLivroEncontrado() {
        driver.get("http://localhost:8080/livros");
        driver.findElement(By.name("busca")).sendKeys("XYZNONEXISTENT");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement alert = driver.findElement(By.cssSelector(".alert-info"));
        assertTrue(alert.getText().contains("Nenhum livro encontrado"));
    }
}