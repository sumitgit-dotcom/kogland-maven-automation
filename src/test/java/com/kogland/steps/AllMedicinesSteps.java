package com.kogland.steps;

import com.kogland.hooks.Hooks;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class AllMedicinesSteps {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public AllMedicinesSteps() {
        this.driver = Hooks.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @Given("I open the Kogland home page")
    public void iOpenTheKoglandHomePage() {
        driver.get("https://www.kogland.com/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        System.out.println(">>> Home page loaded: " + driver.getCurrentUrl());
    }
    
    

    @When("I go to the All Medicines category")
    public void iGoToTheAllMedicinesCategory() {
        // Try direct URL first — most reliable for Shopify-based sites
        String directUrl = "https://www.kogland.com/collections/all-medicines";
        driver.get(directUrl);

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//body[contains(@class,'template-collection') or "
                           + "contains(@class,'collection')] | //h1[contains(.,'All Medicines')]")));
            System.out.println(">>> Direct URL worked: " + driver.getCurrentUrl());
            return;
        } catch (Exception e) {
            System.out.println(">>> Direct URL failed, falling back to home-page click");
        }

        // Fallback: home page scroll + click
        driver.get("https://www.kogland.com/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        By linkBy = By.xpath(
                "//a[contains(normalize-space(.),'All Medicines')]"
        );

        for (int i = 0; i < 20; i++) {
            List<WebElement> links = driver.findElements(linkBy);
            for (WebElement link : links) {
                try {
                    js.executeScript("arguments[0].scrollIntoView({block:'center'});", link);
                    Thread.sleep(300);
                    if (link.isDisplayed()) {
                        js.executeScript("arguments[0].click();", link);
                        Thread.sleep(2500);
                        System.out.println(">>> Clicked All Medicines link, URL = " + driver.getCurrentUrl());
                        return;
                    }
                } catch (Exception ignored) {}
            }
            js.executeScript("window.scrollBy(0, 500);");
            try { Thread.sleep(400); } catch (InterruptedException ignored) {}
        }
        throw new RuntimeException("Could not navigate to All Medicines");
    }

    @And("I open the filter panel")
    public void iOpenTheFilterPanel() {
        // The filter-options-title buttons are accordions. Click the first one to expand.
        By filterBy = By.xpath("//button[contains(@class,'filter-options-title')]");

        List<WebElement> buttons = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(filterBy));

        if (buttons.isEmpty()) {
            throw new RuntimeException("No filter-options-title buttons found on the page");
        }

        WebElement btn = buttons.get(0);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", btn);
        try { btn.click(); }
        catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }

        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        System.out.println(">>> Filter panel expanded");
    }

    @And("I select the Fast&Up brand filter")
    public void iSelectTheFastUpBrandFilter() {
        String brand = "Fast&Up";

        // The brand label is typically a <span>, <label>, or <a> with exact text.
        // Note: '&' is '&' in XPath (not '&amp;') because XPath sees the parsed DOM.
        By labelBy = By.xpath(
                "//*[self::span or self::label or self::a or self::div]"
              + "[normalize-space()='" + brand + "']"
        );

        List<WebElement> matches = driver.findElements(labelBy);

        // Fallback: contains-match if the exact one finds nothing
        if (matches.isEmpty()) {
            matches = driver.findElements(By.xpath(
                    "//*[self::span or self::label or self::a or self::div]"
                  + "[contains(normalize-space(.),'" + brand + "')]"
            ));
        }

        // Prefer shortest visible match
        WebElement target = null;
        int shortest = Integer.MAX_VALUE;
        for (WebElement el : matches) {
            try {
                String t = el.getText().trim();
                if (t.equalsIgnoreCase(brand) || t.toLowerCase().contains(brand.toLowerCase())) {
                    if (t.length() < shortest && el.isDisplayed()) {
                        shortest = t.length();
                        target = el;
                    }
                }
            } catch (Exception ignored) {}
        }

        if (target == null) {
            // Diagnostic dump
            System.out.println(">>> FAILED: Fast&Up filter not found. Visible filter labels:");
            List<WebElement> labels = driver.findElements(By.xpath(
                    "//*[contains(@class,'filter-options')]//*[normalize-space()]"));
            for (int i = 0; i < Math.min(labels.size(), 30); i++) {
                try {
                    String t = labels.get(i).getText().trim();
                    if (!t.isEmpty() && t.length() < 60) {
                        System.out.println("    '" + t + "'");
                    }
                } catch (Exception ignored) {}
            }
            throw new RuntimeException("Fast&Up brand filter not found");
        }

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", target);
        try { target.click(); }
        catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", target);
        }

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
        System.out.println(">>> Fast&Up filter applied, URL = " + driver.getCurrentUrl());
    }

    @And("I click the wishlist button for {string}")
    public void iClickTheWishlistButtonFor(String productName) {
        // Match by aria-label containing the product title (this is the most stable attribute)
        By wishlistBy = By.xpath(
                "//button[contains(@aria-label,'Add to Wish List')]"
              + "[contains(@aria-label,\"" + productName + "\")]"
        );

        List<WebElement> buttons = driver.findElements(wishlistBy);

        if (buttons.isEmpty()) {
            // Diagnostic: dump all available wishlist button labels
            System.out.println(">>> FAILED: wishlist button for '" + productName + "' not found");
            List<WebElement> all = driver.findElements(
                    By.xpath("//button[contains(@aria-label,'Add to Wish List')]"));
            System.out.println(">>> Available wishlist buttons: " + all.size());
            for (int i = 0; i < Math.min(all.size(), 20); i++) {
                try {
                    System.out.println("    aria-label='" + all.get(i).getAttribute("aria-label") + "'");
                } catch (Exception ignored) {}
            }
            throw new RuntimeException("Wishlist button not found for: " + productName);
        }

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(buttons.get(0)));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", btn);
        try { btn.click(); }
        catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        System.out.println(">>> Wishlist button clicked for: " + productName);
    }

    @Then("the wishlist button should show as added")
    public void theWishlistButtonShouldShowAsAdded() {
        // After adding, Shopify-style wishlist buttons typically change aria-label to "Remove from Wish List"
        boolean confirmed = wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//button[contains(@aria-label,'Remove from Wish List')]")),
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//*[contains(text(),'added to your wishlist')]")),
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//*[contains(@class,'wishlist') and contains(@class,'active')]"))
        )) != null;

        if (!confirmed) {
            throw new AssertionError("No wishlist confirmation detected after click");
        }
        System.out.println(">>> Wishlist confirmed");
    }
}