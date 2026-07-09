package com.bibliotech.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

/**
 * Classe base para todos os testes Selenium.
 * Configura o WebDriver, fornece métodos auxiliares e captura screenshots em falhas.
 */
public abstract class BaseSeleniumTest {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected static final String BASE_URL = "http://localhost:8080";
    protected static final String ADMIN_EMAIL = "admin@bibliotech.com";
    protected static final String ADMIN_SENHA = "admin123";

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Realiza login no sistema com as credenciais de admin.
     */
    protected void fazerLogin() {
        fazerLogin(ADMIN_EMAIL, ADMIN_SENHA);
    }

    /**
     * Realiza login com credenciais específicas.
     */
    protected void fazerLogin(String email, String senha) {
        driver.get(BASE_URL + "/login");
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("senha")).sendKeys(senha);
        driver.findElement(By.id("btn-login")).click();
    }

    /**
     * Aguarda e retorna um elemento visível.
     */
    protected WebElement aguardarElemento(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Captura screenshot e salva na pasta evidencias/screenshots/.
     */
    protected void capturarScreenshot(String nomeArquivo) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destino = new File("evidencias/screenshots/" + nomeArquivo + ".png");
            destino.getParentFile().mkdirs();
            FileUtils.copyFile(screenshot, destino);
            System.out.println("Screenshot salvo: " + destino.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Erro ao capturar screenshot: " + e.getMessage());
        }
    }

    /**
     * Seleciona opção em um <select> pelo valor.
     */
    protected void selecionarOpcao(String elementId, String value) {
        Select select = new Select(driver.findElement(By.id(elementId)));
        select.selectByValue(value);
    }

    /**
     * Preenche um campo e limpa antes.
     */
    protected void preencherCampo(String elementId, String valor) {
        WebElement campo = driver.findElement(By.id(elementId));
        campo.clear();
        campo.sendKeys(valor);
    }

    /**
     * Cadastra um livro de teste.
     */
    protected void cadastrarLivro(String titulo, String autor, String isbn) {
        driver.get(BASE_URL + "/livros/novo");
        preencherCampo("titulo", titulo);
        preencherCampo("autor", autor);
        preencherCampo("isbn", isbn);
        preencherCampo("editora", "Editora Teste");
        preencherCampo("ano", "2020");
        preencherCampo("quantidadeExemplares", "3");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }
}
