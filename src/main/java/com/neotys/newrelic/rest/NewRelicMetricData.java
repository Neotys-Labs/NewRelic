package com.neotys.newrelic.rest;

import java.util.Arrays;
import java.util.List;

import com.neotys.newrelic.Constants;
import com.neotys.rest.dataexchange.model.Entry;
import com.neotys.rest.dataexchange.model.EntryBuilder;

public class NewRelicMetricData {
	
	private final String applicationName;
	private final String hostName;
	private final String path;
	private final String name;
	private final double value;
	private final String unit;
	private final String timestamp;
	
	public NewRelicMetricData(final String applicationName, final String hostName, final String path, final String name, final double value, final String unit, final String timestamp) {
		this.applicationName = applicationName;
		this.hostName = hostName;
		this.path = path;
		this.name = name;
		this.value = value;
		this.unit = unit;
		this.timestamp = timestamp;		
	}
	
	public final Entry buildEntry(){	
		final List<String> dataEntryPath = Arrays.asList(Constants.NEW_RELIC, applicationName, hostName);
		dataEntryPath.addAll(Arrays.asList(path.split("/")));
		dataEntryPath.add(name);
		final EntryBuilder entryBuilder = new EntryBuilder(dataEntryPath, Long.parseLong(timestamp));
		entryBuilder.unit(unit);
		entryBuilder.value(value);
		return entryBuilder.build();
	}

}
