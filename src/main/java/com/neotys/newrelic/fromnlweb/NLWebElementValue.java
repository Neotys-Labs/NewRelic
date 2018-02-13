package com.neotys.newrelic.fromnlweb;

import io.swagger.client.model.ElementDefinition;
import io.swagger.client.model.ElementValues;

public class NLWebElementValue {

	private final String name;
	private final String path;
	private float responsetime;
	private float hitPerSecond;
	private float throughput;	
	private final String userPath;

	public NLWebElementValue(final ElementDefinition definition, final ElementValues values) {
		super();
		this.name = definition.getName();
		String result = "";
		for (String p : definition.getPath()) {
			result += p + "/";
		}
		this.path = result.substring(0, result.length() - 1);
		
		this.responsetime = values.getAvgDuration() / 1000;
		this.hitPerSecond = values.getElementPerSecond();
		this.throughput = values.getDownloadedBytesPerSecond();
		this.userPath = definition.getPath().get(0);	
	}
	
	public String getPath() {
		return path;
	}
	
	public String getName() {
		return name;
	}
	public float getHitPerSecond() {
		return hitPerSecond;
	}
	
	public float getResponsetime() {
		return responsetime;
	}
	
	public float getThroughput() {
		return throughput;
	}
	
	public String getUserPath() {
		return userPath;
	}
	
	
}
