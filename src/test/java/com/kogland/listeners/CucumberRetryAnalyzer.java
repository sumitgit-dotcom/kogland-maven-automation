package com.kogland.listeners;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CucumberRetryAnalyzer implements IRetryAnalyzer  {
	
    private static final int MAX_RETRIES = 2;

    private static final Map<String, Integer> ATTEMPTS = new ConcurrentHashMap<>();

    private static final Map<String, Boolean> FLAKY = new ConcurrentHashMap<>();

    private static final Map<String, Boolean> PASSED_ON_RETRY = new ConcurrentHashMap<>();

    @Override

    public boolean retry(ITestResult result)
    {
        String key = key(result);

        int count = ATTEMPTS.getOrDefault(key, 0) + 1;

        ATTEMPTS.put(key, count);

        if (count <= MAX_RETRIES) {
            System.out.println(">>> Retrying [" + key + "] attempt " + count + "/" + MAX_RETRIES);
            FLAKY.put(key, true);
            return true;
        }
        return false;
    }
    
    public static boolean isFlaky(String scenarioName) {
        return PASSED_ON_RETRY.containsKey(scenarioName);
    }
    
    public static void reset() {
        ATTEMPTS.clear();
        FLAKY.clear();
        PASSED_ON_RETRY.clear();
    }
    
    private static String key(ITestResult result) {
        Object[] params = result.getParameters();
        if (params != null && params.length > 0 && params[0] != null) {
            return params[0].toString();
        }
        return result.getMethod().getMethodName();
    }

	public static void markPassedOnRetry(ITestResult result) {
		
		
	}
    
    }


