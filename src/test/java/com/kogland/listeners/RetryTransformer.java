package com.kogland.listeners;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class RetryTransformer implements IAnnotationTransformer {
	
    @Override

    public void transform(ITestAnnotation annotation,
            Class testClass,
            Constructor testConstructor,
            Method testMethod) {
if (annotation.getRetryAnalyzerClass() == null
  || annotation.getRetryAnalyzerClass().equals(org.testng.internal.annotations.DisabledRetryAnalyzer.class)) {
annotation.setRetryAnalyzer(CucumberRetryAnalyzer.class);
}
}

}
