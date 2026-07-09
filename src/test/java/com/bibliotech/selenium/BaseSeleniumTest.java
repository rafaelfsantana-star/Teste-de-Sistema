package com.bibliotech.selenium;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

@ExtendWith(BaseSeleniumTest.ScreenshotOnFinish.class)
public class BaseSeleniumTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
        new File("Evidencias_GrupoX/screenshots").mkdirs();
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless"); // descomente para não ver o navegador
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDown() {
        // O fechamento é feito pelo TestWatcher após o screenshot
    }

    protected void takeScreenshot(String fileName) {
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(scrFile, new File("Evidencias_GrupoX/screenshots/" + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void closeDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    static class ScreenshotOnFinish implements TestWatcher {
        @Override
        public void testSuccessful(ExtensionContext context) {
            capture(context, "PASSED");
        }

        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            capture(context, "FAILED");
        }

        @Override
        public void testAborted(ExtensionContext context, Throwable cause) {
            capture(context, "ABORTED");
        }

        private void capture(ExtensionContext context, String status) {
            Object testInstance = context.getRequiredTestInstance();
            if (testInstance instanceof BaseSeleniumTest) {
                BaseSeleniumTest test = (BaseSeleniumTest) testInstance;
                String testName = context.getDisplayName()
                        .replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
                String timestamp = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
                String fileName = status + "-" + testName + "-" + timestamp + ".png";
                test.takeScreenshot(fileName);
                test.closeDriver();
            }
        }
    }
}