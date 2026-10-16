package com.kogland.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Hooks {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<WebDriverWait> WAIT = new ThreadLocal<>();
    private static final ThreadLocal<String> BROWSER = new ThreadLocal<>();

    public static WebDriver getDriver() { return DRIVER.get(); }
    public static WebDriverWait getWait() { return WAIT.get(); }
    public static String getBrowser() { return BROWSER.get(); }

    @Before(order = 0)
    public void setUp(Scenario scenario) {
        String browser = System.getProperty("browser", "chrome").toLowerCase();
        BROWSER.set(browser);

        WebDriver driver;
        switch (browser) {
            case "firefox": {
                FirefoxOptions opts = new FirefoxOptions();
                opts.addArguments("--start-maximized");
                driver = new FirefoxDriver(opts);
                break;
            }
            case "edge": {
                EdgeOptions opts = new EdgeOptions();
                opts.addArguments("--start-maximized");
                opts.addArguments("--disable-notifications");
                driver = new EdgeDriver(opts);
                break;
            }
            case "chrome":
            default: {
                ChromeOptions opts = new ChromeOptions();
                opts.addArguments("--start-maximized");
                opts.addArguments("--disable-notifications");
                opts.addArguments("--remote-allow-origins=*");
                driver = new ChromeDriver(opts);
                break;
            }
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        driver.manage().window().maximize();

        DRIVER.set(driver);
        WAIT.set(new WebDriverWait(driver, Duration.ofSeconds(25)));

        System.out.println(">>> @Before: scenario='" + scenario.getName()
                + "' browser=" + browser + " thread=" + Thread.currentThread().getId());
    }

    @After(order = 0)
    public void tearDown(Scenario scenario) {
        WebDriver driver = DRIVER.get();
        try {
            if (scenario.isFailed() && driver instanceof TakesScreenshot) {
                attachFailureArtifacts(scenario, driver);
            }
        } catch (Exception e) {
            System.err.println(">>> Failure capture error: " + e.getMessage());
        } finally {
            if (driver != null) {
                try { driver.quit(); } catch (Exception ignored) {}
                DRIVER.remove();
                WAIT.remove();
                BROWSER.remove();
            }
            System.out.println("<<< @After: scenario='" + scenario.getName()
                    + "' status=" + scenario.getStatus());
        }
    }

    private void attachFailureArtifacts(Scenario scenario, WebDriver driver) {
       
        byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        scenario.attach(png, "image/png", "failure-screenshot-" + scenario.getName());

       
        try {
            Path dir = Path.of("target", "failures");
            Files.createDirectories(dir);
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String safe = scenario.getName().replaceAll("[^a-zA-Z0-9-_]", "_");
            File out = dir.resolve(safe + "-" + stamp + ".png").toFile();
            Files.write(out.toPath(), png);
            System.out.println(">>> Screenshot saved: " + out.getAbsolutePath());
        } catch (IOException e) {
            System.err.println(">>> Could not save screenshot: " + e.getMessage());
        }

      
        String url, title;
        try { url = driver.getCurrentUrl(); } catch (Exception e) { url = "(unavailable)"; }
        try { title = driver.getTitle(); } catch (Exception e) { title = "(unavailable)"; }

        StringBuilder meta = new StringBuilder();
        meta.append("Scenario: ").append(scenario.getName()).append("\n");
        meta.append("Browser: ").append(BROWSER.get()).append("\n");
        meta.append("URL: ").append(url).append("\n");
        meta.append("Title: ").append(title).append("\n");
        meta.append("Time: ").append(LocalDateTime.now()).append("\n");

        scenario.attach(meta.toString().getBytes(), "text/plain", "failure-context");
        System.out.println(">>> Failure context attached for: " + scenario.getName());
    }
}