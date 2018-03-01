package com.neotys.newrelic.fromnlweb;

import java.util.List;
import java.util.Optional;

public class NLWebMainStatistics {

	private final List<String[]> nlData;
	private final int duration;
	
	public NLWebMainStatistics(final List<String[]> nlData, final Optional<Long> lastTimestamp, final long currentTimestamp) {
		this.nlData = nlData;
		this.duration = lastTimestamp.isPresent() ? (int) ((currentTimestamp - lastTimestamp.get())/1000) : 0;
	}
	
	public List<String[]> getNlData() {
		return nlData;
	}
	
	public int getDuration() {
		return duration;
	}
}
