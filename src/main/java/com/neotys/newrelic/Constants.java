package com.neotys.newrelic;

import java.util.Arrays;
import java.util.List;

public final class Constants {

	public static final int MIN_NEW_RELIC_DURATION = 30;
	public static final String NEW_RELIC_URL = "https://api.newrelic.com/v2/";
	public static final String APPLICATIONS_JSON = "applications.json";
	public static final String NEW_RELIC_PLATFORM_API_URL = "https://platform-api.newrelic.com/platform/v1/metrics";
	public static final String NEW_RELIC_INSIGHT_URL = "https://insights-collector.newrelic.com/v1/accounts/";
	public static final String NLGUID = "com.neotys.NeoLoad.plugin";
	public static final String NLWEB_TRANSACTION = "TRANSACTION";
	public static final String NLWEB_PAGE = "PAGE";
	public static final String NLWEB_REQUEST = "REQUEST";
	public static final int BAD_REQUEST = 400;
	public static final int UNAUTHORIZED = 403;
	public static final int NOT_FOUND = 404;
	public static final int METHOD_NOT_ALLOWED = 405;
	public static final int REQUEST_ENTITY_TOO_LARGE = 413;
	public static final int INTERNAL_SERVER_ERROR = 500;
	public static final int BAD_GATEWAY = 502;
	public static final int SERVICE_UNAVAIBLE = 503;
	public static final int GATEWAY_TIMEOUT = 504;	
	public static final String VERSION = "1.0.0";
	public static final String CUSTOM_ACTION_DISPLAY_NAME = "New Relic Monitoring";
	public static final String CUSTOM_ACTION_DISPLAY_PATH = "APM/New Relic";
	public static final String CUSTOM_ACTION_ICON = "newrelic.png";
	public static final String CUSTOM_ACTION_TYPE = "NewRelicMonitoringAction";
	public static final String STATUS_CODE_INVALID_PARAMETER = "NL-NEW_RELIC_MONITORING_ACTION-01";	
	public static final String STATUS_CODE_BAD_CONTEXT = "NL-NEW_RELIC_MONITORING_ACTION-03";	
	public static final String NEW_RELIC_METRIC_NAME_API = "/metrics.json";
	public static final String NEW_RELIC_HOST_API = "/hosts.json";
	public static final String NEW_RELIC_METRIC_DATA_API = "/metrics/data.json";
	public static final String NEW_RELIC = "NewRelic";
	public static final String NEOLOAD_CONTEXT_HARDWARE = NEW_RELIC;
	public static final String NEOLOAD_CONTEXT_LOCATION = NEW_RELIC;
	public static final String NEOLOAD_CONTEXT_SOFTWARE = NEW_RELIC;
	public static final List<String> RELEVANT_METRIC_NAMES = Arrays.asList("min", "max", "average", "used_mb", "percent");
	public static final List<String> RELEVANT_METRIC_NAMES_FOR_HOST = Arrays.asList("Datastore/statement", "Datastore/instance", "CPU", "Memory", "Error/",
			"connects");
}
