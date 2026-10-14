package com.kogland.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import static org.junit.Assert.*;


public class KoglandJUnitTest {
	  private WebDriver driver;
	  
	  @Before
	  
	  public void setUp() {
	        driver = new ChromeDriver();
	    }
	  
	  @Test
	    public void verifyKoglandHomepage() {
	        driver.get("https://www.kogland.com/");

	        assertTrue(
	            driver.getCurrentUrl().contains("kogland.com")
	        );

	        assertNotNull(driver.getTitle());
	        assertFalse(driver.getTitle().trim().isEmpty());
	    }
	  
	  @After
	    public void tearDown() {
	        if (driver != null) {
	            driver.quit();
	        }
	
	  }
	

}
