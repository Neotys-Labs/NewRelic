package com.neotys.newrelic.rest;

import org.json.JSONObject;

/**
 * 
 * @author srichert
 * @date 14 févr. 2018
 */
public class NewRelicApplicationHost {
	
	private final String hostId;
	private final String hostName;

	public NewRelicApplicationHost(final JSONObject jsonObject) {
		this.hostId = String.valueOf(jsonObject.getInt("id"));
		this.hostName = jsonObject.getString("host");
	}
	
	public String getHostId() {
		return hostId;
	}
	
	public String getHostName() {
		return hostName;
	}
}
