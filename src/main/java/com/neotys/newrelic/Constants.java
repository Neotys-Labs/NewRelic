package com.neotys.newrelic;

import java.util.Arrays;
import java.util.List;

public final class Constants {

	/*** New Relic ***/
	public static final String NEW_RELIC = "NewRelic";
	public static final String NEW_RELIC_API_URL = "https://api.newrelic.com/v2/";
	public static final String NEW_RELIC_PLATFORM_API_URL = "https://platform-api.newrelic.com/platform/v1/metrics";
	public static final String NEW_RELIC_INSIGHT_URL = "https://insights-collector.newrelic.com/v1/accounts/";
	public static final String METRICS_JSON = "/metrics.json";
	public static final String HOSTS_JSON = "/hosts.json";
	public static final String DATA_JSON = "/metrics/data.json";
	public static final String APPLICATIONS_JSON = "applications.json";
	
	/*** NeoLoad Web ***/
	public static final String NLWEB_TRANSACTION = "TRANSACTION";
	public static final String NLWEB_PAGE = "PAGE";
	public static final String NLWEB_REQUEST = "REQUEST";
	
	/*** Custom action ***/
	public static final String CUSTOM_ACTION_DISPLAY_NAME = "New Relic Monitoring";
	public static final String CUSTOM_ACTION_DISPLAY_PATH = "APM/New Relic";
	public static final String CUSTOM_ACTION_ICON = "newrelic.png";
	public static final String CUSTOM_ACTION_TYPE = "NewRelicMonitoringAction";
	public static final String CUSTOM_ACTION_HOST = "com.neotys.NeoLoad.plugin";
	public static final String CUSTOM_ACTION_VERSION = "1.0.0";
			
	/*** NeoLoad error codes ***/
	public static final String STATUS_CODE_INVALID_PARAMETER = "NL-NEW_RELIC_MONITORING_ACTION-01";	
	public static final String STATUS_CODE_BAD_CONTEXT = "NL-NEW_RELIC_MONITORING_ACTION-03";	
	
	/*** NeoLoad context (Data Exchange API) ***/
	public static final String NEOLOAD_CONTEXT_HARDWARE = NEW_RELIC;
	public static final String NEOLOAD_CONTEXT_LOCATION = NEW_RELIC;
	public static final String NEOLOAD_CONTEXT_SOFTWARE = NEW_RELIC;
	
	/*** Send Data from NLWeb to New Relic ***/
	public static final int MIN_NEW_RELIC_DURATION = 30;
	
	/*** Retrieve Data from New Relic to NeoLoad ***/	
	public static final List<String> RELEVANT_METRIC_NAMES = Arrays.asList("min", "max", "average", "used_mb", "percent");
	public static final List<String> RELEVANT_METRIC_NAMES_FOR_HOST = Arrays.asList("Datastore/statement", "Datastore/instance", "CPU", "Memory", "Error/", "connects");	
	
}
