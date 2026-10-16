package com.kogland.steps;

import com.kogland.hooks.Hooks;
import io.cucumber.java.en.And;
//import io.cucumber.java.en.Given;
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



public class WishlistSteps {
	
	private final WebDriver driver;
    private final WebDriverWait wait;
    
    public WishlistSteps()
    {
    	  this.driver = Hooks.getDriver();
          this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));

    }
    
   // @Given("I navigate to the Kogland home page")
   // public void iNavigateToTheKoglandHomePage()
    //{
    	 //driver.get("https://www.kogland.com/");
       //  wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        // System.out.println(">>> Home page loaded: " + driver.getCurrentUrl());

       // }
    @When("I scroll to the {string} category on the home page")
    public void iScrollToTheCategoryOnTheHomePage(String category) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String lower = category.toLowerCase();

        By locator = By.xpath(
            "//*[self::a or self::span or self::div or self::p or self::h3 or self::h4]"
          + "[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + lower + "')]"
        );

        for (int i = 0; i < 25; i++) {
            List<WebElement> found = driver.findElements(locator);
            for (WebElement el : found) {
                try {
                    if (el.isDisplayed() && el.getText().trim().toLowerCase().contains(lower)) {
                        js.executeScript("arguments[0].scrollIntoView({block:'center'});", el);
                        System.out.println(">>> Found category element for: " + category
                                + " | tag=" + el.getTagName()
                                + " | text='" + el.getText().trim() + "'");
                        return;
                    }
                } catch (Exception ignored) {}
            }
            js.executeScript("window.scrollBy(0, 600);");
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }

        // Diagnostic dump on failure
        System.out.println(">>> FAILED to find '" + category + "'. Dumping candidate links:");
        List<WebElement> allLinks = driver.findElements(By.tagName("a"));
        for (int i = 0; i < Math.min(allLinks.size(), 50); i++) {
            WebElement a = allLinks.get(i);
            try {
                String t = a.getText().trim();
                if (!t.isEmpty()) {
                    System.out.println("    [" + i + "] '" + t + "'  href=" + a.getAttribute("href"));
                }
            } catch (Exception ignored) {}
        }
        throw new RuntimeException("Category link not found after scrolling: " + category);
    }

    
    @And("I click on the {string} category link")
    public void iClickOnTheCategoryLink(String category) {
        String lower = category.toLowerCase();

        By locator = By.xpath(
            "//*[self::a or self::span or self::div]"
          + "[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + lower + "')]"
        );

        List<WebElement> candidates = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(locator));

        // Prefer the smallest matching element (usually the direct text node)
        WebElement target = null;
        int shortestLen = Integer.MAX_VALUE;
        for (WebElement el : candidates) {
            try {
                String t = el.getText().trim();
                if (t.toLowerCase().contains(lower) && t.length() < shortestLen && el.isDisplayed()) {
                    shortestLen = t.length();
                    target = el;
                }
            } catch (Exception ignored) {}
        }
        if (target == null) throw new RuntimeException("No clickable category link found: " + category);

        // If the target itself isn't a link, walk up to the nearest <a>
        if (!"a".equalsIgnoreCase(target.getTagName())) {
            try {
                WebElement anchor = target.findElement(By.xpath("ancestor::a[1]"));
                if (anchor != null) target = anchor;
            } catch (Exception ignored) {}
        }

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", target);
        try { target.click(); }
        catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", target); }

        try { Thread.sleep(2500); } catch (InterruptedException ignored) {}
        System.out.println(">>> Clicked category '" + category + "', URL = " + driver.getCurrentUrl());
    }
    
    @And("I add the {string} product to the wishlist")
    
    public void iAddTheProductToTheWishlist(String productAlt)
    
    {
    	 By wishlistBy = By.xpath(
                 "//button[contains(@aria-label,'Add to Wish List " + productAlt + "')]"
         );
    	 
    	 List<WebElement> buttons = wait.until(
                 ExpectedConditions.presenceOfAllElementsLocatedBy(wishlistBy));
    	 
    	 if (buttons.isEmpty()) {
             // Fallback: any wishlist button on the page whose img alt matches
             buttons = driver.findElements(
                     By.xpath("//button[contains(@aria-label,'Add to Wish List')]")
             );
         }
         if (buttons.isEmpty()) {
             throw new RuntimeException("No wishlist button found for: " + productAlt);
         }

         
         WebElement button = buttons.get(0);
         ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", button);
         try {
             button.click();
         } catch (Exception e) {
             ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
         }
         try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
         System.out.println(">>> Added to wishlist: " + productAlt);
     }
    
    
    @And("I click on the customer menu")
    public void iClickOnTheCustomerMenu() {
        By menuBy = By.xpath("//button[@id='customer-menu']");
        WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(menuBy));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", menu);
        try {
            menu.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menu);
        }
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        System.out.println(">>> Opened customer menu");
    }

    
    @And("I click on the {string} login tab")
    public void iClickOnTheLoginTab(String tabName) {
        // "Email" tab has id='tab-email'
        By tabBy = By.xpath("//button[@id='tab-" + tabName.toLowerCase() + "']");
        WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(tabBy));
        try {
            tab.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tab);
        }
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        System.out.println(">>> Switched to '" + tabName + "' login tab");
    }
    
    
    @And("I enter {string} into the guest email field")
    public void iEnterIntoTheGuestEmailField(String email) {
        WebElement input = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='guest-login-email']")));
        input.clear();
        input.sendKeys(email);
        System.out.println(">>> Email entered: " + email);
    }

    
    @And("I enter {string} into the guest password field")
    public void iEnterIntoTheGuestPasswordField(String password) {
        WebElement input = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='guest-login-password']")));
        input.clear();
        input.sendKeys(password);
        System.out.println(">>> Password entered (masked)");
    }
    
    
    @Then("I should be logged in successfully")
    public void iShouldBeLoggedInSuccessfully() {
        // Adjust if your flow requires pressing a submit button first:
        //   WebElement submit = driver.findElement(By.xpath("//button[@type='submit']"));
        //   submit.click();

        // Wait for a post-login indicator. Common signals on Shopify stores:
        boolean ok = wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'My Account')]")),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'Logout')]")),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'Sign out')]")),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'My Wishlist')]")),
                ExpectedConditions.urlContains("/account")
        )) != null;
        if (!ok) throw new AssertionError("No login confirmation detected.");
        System.out.println(">>> Login confirmed, URL = " + driver.getCurrentUrl());
    }
    
    }


