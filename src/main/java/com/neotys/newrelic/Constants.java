package com.neotys.newrelic;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;

import com.google.common.base.Optional;
import com.neotys.action.argument.Arguments;

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
	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##########");
	
	/*** Custom action ***/
	public static final String CUSTOM_ACTION_DISPLAY_NAME = "New Relic Monitoring";
	public static final String CUSTOM_ACTION_DISPLAY_PATH = "APM/New Relic";
	public static final Optional<String> CUSTOM_ACTION_MINIMUM_VERSION = Optional.of("6.3");
	public static final Optional<String> CUSTOM_ACTION_MAXIMIM_VERSION = Optional.absent();	
	public static final String CUSTOM_ACTION_TYPE = "NewRelicMonitoringAction";
	public static final String CUSTOM_ACTION_HOST = "com.neotys.NeoLoad.plugin";
	public static final String CUSTOM_ACTION_VERSION = "1.0.0";
	public static final ImageIcon CUSTOM_ACTION_ICON;
	public static final String CUSTOM_ACTION_DESCRIPTION;
	static {
		final URL iconURL = NewRelicAction.class.getResource("newrelic.png");
		if (iconURL != null) {CUSTOM_ACTION_ICON = new ImageIcon(iconURL);}
		else {CUSTOM_ACTION_ICON = null;}
		final StringBuilder description = new StringBuilder();	
		// TODO: review description
		description.append("New Relic Monitoring Action will retrieve all the counters measured by NewRelic Infrastructure\n").append(
				Arguments.getArgumentDescriptions(NewRelicOption.values()));
		CUSTOM_ACTION_DESCRIPTION = description.toString();
	}		
			
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
