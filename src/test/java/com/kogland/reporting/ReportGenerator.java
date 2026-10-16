package com.kogland.reporting;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {
	
	  public static void main(String[] args) throws Exception {
	        File history = new File("target/run-history.json");
	        if (!history.exists()) {
	            System.out.println("No run-history.json yet. Run the suite first.");
	            return;
	        }
	        
	        ObjectMapper mapper = new ObjectMapper();
	        List<RunHistory> runs = mapper.readValue(history, mapper.getTypeFactory()
	                .constructCollectionType(List.class, RunHistory.class));
	        
	        Map<String, List<RunHistory.ScenarioResult>> byName = new LinkedHashMap<>();
	        for (RunHistory run : runs) {
	            for (RunHistory.ScenarioResult r : run.results) {
	                byName.computeIfAbsent(r.name, k -> new ArrayList<>()).add(r);
	            }
	        }
	        
	        StringBuilder html = new StringBuilder();
	        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>")
	            .append("<title>Flaky & Failed Test Report</title>")
	            .append("<style>")
	            .append("body{font-family:Segoe UI,Arial,sans-serif;margin:24px;background:#f6f8fa;color:#24292f}")
	            .append("h1{margin-bottom:4px} h2{margin-top:32px}")
	            .append(".summary{display:flex;gap:16px;margin:16px 0}")
	            .append(".card{background:#fff;border:1px solid #d0d7de;border-radius:8px;padding:16px 24px;min-width:140px}")
	            .append(".card .n{font-size:28px;font-weight:600}")
	            .append(".card.pass .n{color:#1a7f37} .card.fail .n{color:#cf222e}")
	            .append(".card.flaky .n{color:#bf8700} .card.skip .n{color:#57606a}")
	            .append("table{border-collapse:collapse;width:100%;background:#fff;border:1px solid #d0d7de;border-radius:8px;overflow:hidden}")
	            .append("th,td{padding:10px 14px;text-align:left;border-bottom:1px solid #eaeef2;font-size:14px;vertical-align:top}")
	            .append("th{background:#f6f8fa;font-weight:600}")
	            .append("tr:last-child td{border-bottom:none}")
	            .append(".badge{display:inline-block;padding:2px 8px;border-radius:12px;font-size:12px;font-weight:600}")
	            .append(".badge.PASSED{background:#dafbe1;color:#1a7f37}")
	            .append(".badge.FAILED{background:#ffebe9;color:#cf222e}")
	            .append(".badge.FLAKY{background:#fff8c5;color:#9a6700}")
	            .append(".badge.SKIPPED{background:#eaeef2;color:#57606a}")
	            .append(".err{font-family:Consolas,monospace;font-size:12px;color:#57606a;max-width:520px;word-break:break-word}")
	            .append("</style></head><body>");

	        html.append("<h1>Flaky & Failed Test Report</h1>");
	        html.append("<p>Generated ").append(new Date()).append(" from ")
	            .append(runs.size()).append(" run(s).</p>");

	        // Summary cards
	        long passed = 0, failed = 0, flaky = 0, skipped = 0;
	        for (RunHistory run : runs) {
	            for (RunHistory.ScenarioResult r : run.results) {
	                switch (r.status) {
	                    case "PASSED": passed++; break;
	                    case "FAILED": failed++; break;
	                    case "FLAKY":  flaky++;  break;
	                    case "SKIPPED": skipped++; break;
	                }
	            }
	        }

	        html.append("<div class='summary'>")
	            .append(card("passed", passed))
	            .append(card("failed", failed))
	            .append(card("flaky", flaky))
	            .append(card("skipped", skipped))
	            .append("</div>");

	        // Flaky section
	        html.append("<h2>Flaky Tests</h2>");
	        List<String> flakyNames = byName.entrySet().stream()
	                .filter(e -> e.getValue().stream().anyMatch(r -> "FLAKY".equals(r.status)))
	                .map(Map.Entry::getKey).collect(Collectors.toList());
	        if (flakyNames.isEmpty()) {
	            html.append("<p>No flaky tests detected.</p>");
	        } else {
	            html.append("<table><tr><th>Scenario</th><th>Runs</th><th>Pass/Fail/Flaky</th><th>Last status</th></tr>");
	            for (String n : flakyNames) {
	                List<RunHistory.ScenarioResult> rs = byName.get(n);
	                long p = rs.stream().filter(r -> "PASSED".equals(r.status)).count();
	                long f = rs.stream().filter(r -> "FAILED".equals(r.status)).count();
	                long fl = rs.stream().filter(r -> "FLAKY".equals(r.status)).count();
	                RunHistory.ScenarioResult last = rs.get(rs.size() - 1);
	                html.append("<tr><td>").append(esc(n)).append("</td>")
	                    .append("<td>").append(rs.size()).append("</td>")
	                    .append("<td>").append(p).append(" / ").append(f).append(" / ").append(fl).append("</td>")
	                    .append("<td><span class='badge ").append(last.status).append("'>")
	                    .append(last.status).append("</span></td></tr>");
	            }
	            html.append("</table>");
	        }

	        // Failed section
	        html.append("<h2>Failed Tests</h2>");
	        List<RunHistory.ScenarioResult> failures = runs.stream()
	                .flatMap(r -> r.results.stream())
	                .filter(r -> "FAILED".equals(r.status))
	                .collect(Collectors.toList());
	        if (failures.isEmpty()) {
	            html.append("<p>No failures recorded.</p>");
	        } else {
	            html.append("<table><tr><th>Scenario</th><th>Error</th><th>Message</th></tr>");
	            for (RunHistory.ScenarioResult r : failures) {
	                html.append("<tr><td>").append(esc(r.name)).append("</td>")
	                    .append("<td>").append(esc(r.errorType == null ? "" : r.errorType)).append("</td>")
	                    .append("<td class='err'>").append(esc(r.errorMessage == null ? "" : r.errorMessage)).append("</td>")
	                    .append("</tr>");
	            }
	            html.append("</table>");
	        }

	        html.append("</body></html>");

	        Path out = Path.of("target", "flaky-and-failed-report.html");
	        Files.createDirectories(out.getParent());
	        Files.writeString(out, html.toString());
	        System.out.println("Report: " + out.toAbsolutePath());
	    }

	    private static String card(String cls, long n) {
	        return "<div class='card " + cls + "'><div class='n'>" + n + "</div><div>" + cls + "</div></div>";
	    }

	    private static String esc(String s) {
	        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	    }

}
