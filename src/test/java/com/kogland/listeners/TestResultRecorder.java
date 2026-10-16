package com.kogland.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.kogland.reporting.RunHistory;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestResultRecorder implements ITestListener, ISuiteListener {

    private static final String HISTORY_FILE = "target/run-history.json";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Map<String, RunHistory.ScenarioResult> RESULTS = new ConcurrentHashMap<>();
    private static RunHistory current;

    @Override
    public void onStart(ISuite suite) {
        current = new RunHistory();
        current.startedAt = LocalDateTime.now().format(FMT);
        current.browser = System.getProperty("browser", "chrome");
        System.out.println(">>> Run started at " + current.startedAt + " (browser=" + current.browser + ")");
    }

    @Override
    public void onFinish(ISuite suite) {
        try {
            current.finishedAt = LocalDateTime.now().format(FMT);
            current.results.addAll(RESULTS.values());

            Path dir = Path.of("target");
            Files.createDirectories(dir);
            File out = new File(HISTORY_FILE);

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            List<RunHistory> all = new ArrayList<>();
            if (out.exists()) {
                all = mapper.readValue(
                    out,
                    mapper.getTypeFactory().constructCollectionType(List.class, RunHistory.class)
                );
            }
            all.add(current);
            mapper.writerWithDefaultPrettyPrinter().writeValue(out, all);

            System.out.println(">>> Run history written to " + out.getAbsolutePath());
        } catch (Exception e) {
            System.err.println(">>> Could not persist history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        record(result, "PASSED");
        CucumberRetryAnalyzer.markPassedOnRetry(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        record(result, "FAILED");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        record(result, "SKIPPED");
    }

    private void record(ITestResult result, String baseStatus) {
        String name = name(result);
        RunHistory.ScenarioResult r = RESULTS.getOrDefault(name, new RunHistory.ScenarioResult());
        r.name = name;
        r.durationMs = result.getEndMillis() - result.getStartMillis();

        if (result.getThrowable() != null) {
            r.errorType = result.getThrowable().getClass().getSimpleName();
            r.errorMessage = result.getThrowable().getMessage();
        }

        if ("PASSED".equals(baseStatus) && CucumberRetryAnalyzer.isFlaky(name)) {
            r.status = "FLAKY";
            r.retries = 1;
        } else {
            r.status = baseStatus;
        }

        RESULTS.put(name, r);
    }

    private String name(ITestResult result) {
        Object[] params = result.getParameters();
        if (params != null && params.length > 0 && params[0] != null) {
            return params[0].toString();
        }
        return result.getMethod().getMethodName();
    }
}