package com.kogland.steps;


import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class KoglandHomePageSteps {
	
	 private WebDriver driver;
	    private int linkCount = 0;
	    
	    @Given("I open the Kogland homepage")
	    public void openKoglandHomepage() {

	        driver = new ChromeDriver();
	        driver.manage().window().maximize();

	        driver.get("https://www.kogland.com/");

	        System.out.println("==================================");
	        System.out.println("Kogland Homepage Opened");
	        System.out.println("Page Title: " + driver.getTitle());
	        System.out.println("Page URL: " + driver.getCurrentUrl());
	        System.out.println("==================================");
	    }

	    
	    @When("I collect all clickable links")
	    public void collectClickableLinks() {

	        List<WebElement> links =
	                driver.findElements(By.cssSelector("a[href]"));

	        System.out.println("\n========== CLICKABLE LINKS ==========");

	        for (WebElement link : links) {

	            if (link.isDisplayed() && link.isEnabled()) {

	                String linkText = link.getText().trim();
	                String linkUrl = link.getAttribute("href");

	                if (linkUrl != null && !linkUrl.isBlank()) {
	                    linkCount++;

	                    System.out.println(
	                        linkCount + ". Text: " +
	                        (linkText.isEmpty() ? "[No visible text]" : linkText)
	                    );

	                    System.out.println("   URL: " + linkUrl);
	                }
	            }
	        }

	        System.out.println("=====================================");
	    }

	    @Then("I print the total number of clickable links")
	    public void printTotalLinks() {

	        System.out.println(
	            "TOTAL CLICKABLE LINKS FOUND: " + linkCount
	        );
	    }

	    @After
	    public void tearDown() {

	        if (driver != null) {
	            driver.quit();
	        }
	    }

}
