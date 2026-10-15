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
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class Hooks {
	
	 private static WebDriver driver;
	    private static WebDriverWait wait;
	    
	    public static WebDriver getDriver() {
	        return driver;
	    }

	    public static WebDriverWait getWait() {
	        return wait;
	    }

	    @Before(order = 0)
	    public void setUp(Scenario scenario) {
	        String browser = System.getProperty("browser", "chrome").toLowerCase();

	        switch (browser) {
	            case "firefox":
	                driver = new FirefoxDriver();
	                break;
	            case "edge":
	                driver = new EdgeDriver();
	                break;
	            case "chrome":
	            default:
	                ChromeOptions options = new ChromeOptions();
	                options.addArguments("--start-maximized");
	                options.addArguments("--disable-notifications");
	                options.addArguments("--remote-allow-origins=*");
	               
	                driver = new ChromeDriver(options);
	                break;
	        }

	        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
	        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
	        driver.manage().window().maximize();

	        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

	        System.out.println(">>> Starting scenario: " + scenario.getName()
	                + " | Browser: " + browser);
	    }

	    @After(order = 0)
	    public void tearDown(Scenario scenario) {
	        try {
	            if (scenario.isFailed() && driver instanceof TakesScreenshot) {
	                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
	                scenario.attach(screenshot, "image/png", "failure-" + scenario.getName());
	            }
	            System.out.println("<<< Keeping browser open for 10s for inspection...");
	            Thread.sleep(10000);   // <-- temporary, remove later
	        } catch (Exception e) {
	            System.err.println("Teardown issue: " + e.getMessage());
	        } finally {
	            if (driver != null) { driver.quit(); driver = null; wait = null; }
	        }
	    }
	    }


