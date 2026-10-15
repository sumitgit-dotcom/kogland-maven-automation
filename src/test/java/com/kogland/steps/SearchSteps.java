package com.kogland.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class SearchSteps {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public SearchSteps() {
        this.driver = com.kogland.hooks.Hooks.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @Given("I navigate to the Kogland home page")
    public void iNavigateToTheKoglandHomePage() {
        driver.get("https://www.kogland.com/");
        // Wait for body + the search input to exist (not necessarily visible)
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='search']")));
        System.out.println(">>> Home page loaded: " + driver.getCurrentUrl());
    }

    @When("I click on the search box")
    public void iClickOnTheSearchBox() {
        WebElement search = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='search']"))
        );
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", search);
        search.click();
        System.out.println(">>> Clicked search box");
    }

    @And("I type {string} into the search box")
    public void iTypeIntoTheSearchBox(String text) {
        WebElement search = driver.findElement(By.xpath("//input[@id='search']"));
        search.clear();
        search.sendKeys(text);
        System.out.println(">>> Typed: " + text);

        // Give the autocomplete API a moment to render suggestions
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        // Optional: dump any "View all"-like links that appeared
        List<WebElement> viewAll = driver.findElements(
                By.xpath("//a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'view all')]")
        );
        System.out.println(">>> 'View all'-like links found: " + viewAll.size());
        for (WebElement el : viewAll) {
            System.out.println("    - text='" + el.getText() + "' href=" + el.getAttribute("href"));
        }
    }

    @And("I click on the {string} link")
    public void iClickOnTheLink(String linkText) {
        // Case-insensitive, whitespace-tolerant match
        String xpath = "//a[contains(translate(normalize-space(.),"
                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"
                + linkText.toLowerCase() + "')]";

        List<WebElement> links = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(xpath)));
        if (links.isEmpty()) {
            // Fallback: try buttons / divs with the same text
            links = driver.findElements(By.xpath(
                    "//*[self::a or self::button or self::span or self::div]"
                    + "[contains(translate(normalize-space(.),"
                    + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"
                    + linkText.toLowerCase() + "')]"
            ));
        }
        if (links.isEmpty()) {
            throw new RuntimeException("No element found containing text: " + linkText);
        }

        WebElement target = links.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", target);
        try {
            target.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", target);
        }

        // Confirm navigation actually happened
        try { Thread.sleep(2500); } catch (InterruptedException ignored) {}
        System.out.println(">>> After clicking '" + linkText + "', URL = " + driver.getCurrentUrl());
    }

    @And("I click on the {string} filter")
    public void iClickOnTheFilter(String filterName) {
        WebElement filter = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='instock']"))
        );
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", filter);
        if (!filter.isSelected()) {
            try { filter.click(); }
            catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", filter); }
        }
        System.out.println(">>> Applied filter: " + filterName);
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
    }

    @And("I click on the product {string}")
    public void iClickOnTheProduct(String productAlt) {
        WebElement img = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//img[@alt='" + productAlt + "']"))
        );
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", img);
        try { img.click(); }
        catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", img); }
        System.out.println(">>> Clicked product, URL = " + driver.getCurrentUrl());
    }

    @And("I scroll to the quantity section on the product detail page")
    public void iScrollToTheQuantitySectionOnTheProductDetailPage() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
    }

    @And("I increase the product quantity by {int}")
    public void iIncreaseTheProductQuantityBy(int times) {
        // Prefer robust, relative locators over the absolute XPath
        By plusButton = By.xpath(
                "//button[contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'increase') "
              + "or contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'plus')]"
        );
        List<WebElement> candidates = driver.findElements(plusButton);
        if (candidates.isEmpty()) {
            throw new RuntimeException("Could not find a quantity '+' button. Inspect the DOM and update the locator.");
        }
        WebElement plus = candidates.get(0);
        for (int i = 0; i < times; i++) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", plus);
            try { plus.click(); }
            catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", plus); }
            try { Thread.sleep(700); } catch (InterruptedException ignored) {}
            System.out.println(">>> Qty increased, click " + (i + 1));
        }
    }

    @And("I click on the add to cart button")
    public void iClickOnTheAddToCartButton() {
        // Try several likely locators in order
        By[] options = {
                By.xpath("//button[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'add to cart')]"),
                By.xpath("//button[@name='add']"),
                By.xpath("//form[@action='/cart/add']//button[@type='submit']"),
                By.xpath("//span[@role='status']/ancestor::button[1]")
        };

        WebElement btn = null;
        for (By by : options) {
            List<WebElement> found = driver.findElements(by);
            if (!found.isEmpty()) { btn = found.get(0); System.out.println(">>> Add-to-cart locator matched: " + by); break; }
        }
        if (btn == null) throw new RuntimeException("Could not find an add-to-cart button.");

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
        try { btn.click(); }
        catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn); }

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
    }

    @Then("the product should be added to the cart successfully")
    public void theProductShouldBeAddedToTheCartSuccessfully() {
        // Accept any of several success signals
        boolean ok = wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'added to cart')]")),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'added to bag')]")),
                ExpectedConditions.urlContains("/cart")
        )) != null;
        if (!ok) throw new AssertionError("No cart confirmation detected.");
    }
}