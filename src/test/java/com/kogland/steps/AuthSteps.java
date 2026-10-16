package com.kogland.steps;


import com.kogland.hooks.Hooks;
import io.cucumber.java.en.Given;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

public class AuthSteps {
	
    private final WebDriver driver;

    public AuthSteps() {
        this.driver = Hooks.getDriver();
    }
    
    @Given("I am logged in as a guest via cookie")
    public void iAmLoggedInAsAGuestViaCookie() {
        // 1. Navigate to the domain first (cookie must be set on the correct domain)
        driver.get("https://www.kogland.com/");
        
        driver.manage().addCookie(new Cookie("_secure_session_id", "PASTE_YOUR_VALUE_HERE"));
        driver.navigate().refresh();

        System.out.println(">>> Session cookie injected. Current URL: " + driver.getCurrentUrl());
    }

}
