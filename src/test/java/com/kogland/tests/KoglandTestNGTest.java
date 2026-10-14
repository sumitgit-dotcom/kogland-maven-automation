package com.kogland.tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

public class KoglandTestNGTest {
	
	 @Test
	 
	 public void verifyKoglandHomepage()
	 {
		 
	        WebDriver driver = new ChromeDriver();

	        try
	        {
	        	driver.get("https://www.kogland.com/");
	        	
	        	String url = driver.getCurrentUrl();
	            String title = driver.getTitle();
	            
	            Assert.assertTrue(
	                    url.contains("kogland.com"),
	                    "Incorrect website URL"
	                );
	            
	            Assert.assertNotNull(title);
	            Assert.assertFalse(title.trim().isEmpty());

	            System.out.println("URL: " + url);
	            System.out.println("Title: " + title);

	        }
	        
	        finally
	        {
	        	
	        	driver.quit();
	        }
	 }

}
