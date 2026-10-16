package com.kogland.reporting;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class RunHistory {
	
	 public String browser;
	    public String startedAt;
	    public String finishedAt;
	    public List<ScenarioResult> results = new ArrayList<>();
	    
	    public static class ScenarioResult {
	        public String name;
	        public String feature;
	        public String status;         
	        public long durationMs;
	        public int retries;
	        public String errorMessage;
	        public String errorType;
	    }

}
