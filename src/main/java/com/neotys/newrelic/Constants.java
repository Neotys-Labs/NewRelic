package com.neotys.newrelic;

import java.net.URL;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;

import com.google.common.base.Optional;
import com.neotys.action.argument.Arguments;

public final class Constants {

	/*** New Relic ***/
	public static final String NEW_RELIC = "NewRelic";
	public static final String NEW_RELIC_API_URL = "https://api.newrelic.com/v2/";
	public static final String NEW_RELIC_API_APPLICATIONS_URL = NEW_RELIC_API_URL+"applications/";
	public static final String NEW_RELIC_API_APPLICATIONS_JSON_URL = NEW_RELIC_API_URL+"applications.json";
	public static final String NEW_RELIC_PLATFORM_API_URL = "https://platform-api.newrelic.com/platform/v1/metrics";
	public static final String NEW_RELIC_INSIGHT_URL = "https://insights-collector.newrelic.com/v1/accounts/";
	public static final String NEW_RELIC_METRICS_JSON = "/metrics.json";
	public static final String NEW_RELIC_HOSTS_JSON = "/hosts.json";
	public static final String NEW_RELIC_HOSTS = "/hosts/";
	public static final String NEW_RELIC_DATA_JSON = "/metrics/data.json";	
	public static final String NEW_RELIC_APPLICATION_HOSTS = "application_hosts";	
	public static final String NEW_RELIC_METRICS = "metrics";
	public static final String NEW_RELIC_X_API_KEY = "X-Api-Key";
	public static final String NEW_RELIC_X_INSERT_KEY = "X-Insert-Key";
	public static final String NEW_RELIC_X_LICENSE_KEY = "X-License-Key";
	
	/*** NeoLoad Web ***/
	public static final String NLWEB_TRANSACTION = "TRANSACTION";
	public static final String NLWEB_PAGE = "PAGE";
	public static final String NLWEB_REQUEST = "REQUEST";
	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##########");
	public static final String NLWEB_VERSION = "v1";
	
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
		description.append("New Relic Monitoring Action will retrieve all the counters measured by NewRelic Infrastructure\n").append(
				Arguments.getArgumentDescriptions(NewRelicOption.values()));
		CUSTOM_ACTION_DESCRIPTION = description.toString();
	}		
			
	/*** NeoLoad error codes ***/
	public static final String STATUS_CODE_INVALID_PARAMETER = "NL-NEW_RELIC_ACTION-01";
	public static final String STATUS_CODE_TECHNICAL_ERROR = "NL-NEW_RELIC_ACTION-02";
	public static final String STATUS_CODE_BAD_CONTEXT = "NL-NEW_RELIC_ACTION-03";
	public static final String STATUS_CODE_INSUFFICIENT_DELAY = "NL-NEW_RELIC_ACTION-04";	
	
	/*** NeoLoad context (Data Exchange API) ***/
	public static final String NEOLOAD_CONTEXT_HARDWARE = NEW_RELIC;
	public static final String NEOLOAD_CONTEXT_LOCATION = NEW_RELIC;
	public static final String NEOLOAD_CONTEXT_SOFTWARE = NEW_RELIC;
	
	/*** NeoLoad Current Virtual user context (Keep object in cache cross iterations) ***/
	public static final String NEW_RELIC_LAST_EXECUTION_TIME = "NewRelicLastExecutionTime";
	public static final String NEW_RELIC_REST_CLIENT = "NewRelicRestClient";
	public static final String NL_DATA_EXCHANGE_API_CLIENT = "NLDataExchangeAPIClient";
	public static final String NL_WEB_CLIENT = "NLWebClient";
		
	/*** HTTP ***/
	public static final String HTTP_APPLICATION_JSON = "application/json";
	public static final String HTTP_CONTENT_TYPE = "Content-Type";
	public static final String HTTP_ACCEPT = "Accept";
	
	/*** Retrieve Data from New Relic to NeoLoad ***/
	public static final String NEW_RELIC_DEFAULT_RELEVANT_METRIC_NAMES = "Datastore/statement,Datastore/instance,CPU,Memory,Error/,connects";
	public static final String NEW_RELIC_DEFAULT_RELEVANT_METRIC_VALUES = "min,max,average,used_mb,percent";	
}
