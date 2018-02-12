package com.neotys.newrelic.infrastucture;

import java.text.DecimalFormat;
import java.util.List;

import io.swagger.client.model.ElementDefinition;
import io.swagger.client.model.ElementValues;

public class Metric {

	private String name;
	private String path;
	private float responsetime;
	private float hitPerSecond;
	private float throughput;	
	private String userPath;

	public Metric(final ElementDefinition definition, final ElementValues values) {
		super();
		this.name = definition.getName();
		this.path = getPath(definition.getPath());
		this.responsetime = values.getAvgDuration() / 1000;
		this.hitPerSecond = values.getElementPerSecond();
		this.throughput = values.getDownloadedBytesPerSecond();
		this.userPath = definition.getPath().get(0);	
	}
	
	private static String getPath(final List<String> list) {
		String result = "";
		for (String p : list) {
			result += p + "/";
		}
		return result.substring(0, result.length() - 1);
	}

	public String[] getElementValue() {
		final DecimalFormat df = new DecimalFormat("#.##########");
		final String[] result = new String[7];
		result[0] = name;
		result[1] = userPath;
		result[2] = path;
		result[3] = df.format(responsetime);
		result[4] = df.format(throughput);
		result[5] = df.format(hitPerSecond);
		result[6] = "TRANSACTION";
		return result;
	}
}
