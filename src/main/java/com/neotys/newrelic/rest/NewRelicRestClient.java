package com.neotys.newrelic.rest;

import static com.neotys.newrelic.NewRelicUtils.getProxy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.SimpleTimeZone;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Strings;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import com.neotys.newrelic.Constants;
import com.neotys.newrelic.NewRelicActionArguments;
import com.neotys.newrelic.NewRelicException;
import com.neotys.newrelic.NewRelicUtils;
import com.neotys.newrelic.fromnlweb.NLWebElementValue;

/**
 * 
 * @author srichert
 * @date 14 févr. 2018
 */
public class NewRelicRestClient {

	private final NewRelicActionArguments newRelicActionArguments;
	private final Context context;
	private final String applicationId;
	private final HashMap<String, String> headers = new HashMap<>();	

	public NewRelicRestClient(final NewRelicActionArguments newRelicActionArguments, final Context context) throws NewRelicException {
		this.newRelicActionArguments = newRelicActionArguments;
		this.context = context;
		this.headers.put(Constants.NEW_RELIC_X_API_KEY, newRelicActionArguments.getNewRelicAPIKey());
		this.headers.put(Constants.HTTP_CONTENT_TYPE, Constants.HTTP_APPLICATION_JSON);
		this.headers.put(Constants.HTTP_ACCEPT, Constants.HTTP_APPLICATION_JSON);
		newRelicActionArguments.getNewRelicInsightsAPIKey().map(k -> this.headers.put(Constants.NEW_RELIC_X_INSERT_KEY, k));
		newRelicActionArguments.getNewRelicLicenseKey().map(k -> this.headers.put(Constants.NEW_RELIC_X_LICENSE_KEY, k));
		this.applicationId = getApplicationId();
	}

	/**
	 * This method retrieves the id of an appliaction given its name.
	 * REST URL is: https://api.newrelic.com/v2/applications.json
	 * More info on NewRelic documentation: https://rpm.newrelic.com/api/explore/applications/list
	 * @return
	 * @throws NewRelicException
	 */
	private String getApplicationId() throws NewRelicException {
		final String url = Constants.NEW_RELIC_API_APPLICATIONS_JSON_URL;
		
		final List<Pair<String, String>> parameters = new ArrayList<>();
		parameters.add(new ImmutablePair<>("filter[name]", newRelicActionArguments.getNewRelicApplicationName()));
		HTTPGenerator http = null;
		try {
			final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
			http = new HTTPGenerator(url, HttpGet.METHOD_NAME, headers, parameters, proxy);
			final JSONObject jsoobj = http.getJSONHTTPresponse();
			if (jsoobj != null) {
				if (jsoobj.has("applications")) {
					final JSONArray jsonArray = jsoobj.getJSONArray("applications");
					if(jsonArray.length()==1){
						final String id = String.valueOf(jsonArray.getJSONObject(0).getInt("id"));
						if (!Strings.isNullOrEmpty(id)) {
							return id;
						}
					}
				}
			}
		} catch (final Exception exception){
			throw new NewRelicException("Cannot get applicationId for '" + newRelicActionArguments.getNewRelicApplicationName() + "'.", exception);
		} finally {
			if (http != null) {
				http.closeHttpClient();
			}
		}
		throw new NewRelicException("No Application found for name '" + newRelicActionArguments.getNewRelicApplicationName() + "'.");
	}

	/**
	 * This method lists the hosts available for a given application.
	 * REST URL is: https://api.newrelic.com/v2/applications/<applicationId>/hosts.json
	 * More info on NewRelic documentation: https://rpm.newrelic.com/api/explore/application_hosts/list
	 * @return
	 * @throws IOException
	 */
	public List<NewRelicApplicationHost> getApplicationHosts() throws IOException {
		final List<NewRelicApplicationHost> newRelicApplicationHosts = new ArrayList<>();
		final String url = Constants.NEW_RELIC_API_APPLICATIONS_URL + applicationId + Constants.NEW_RELIC_HOSTS_JSON;
		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
		HTTPGenerator http = null;
		try {
			http = new HTTPGenerator(url, HttpGet.METHOD_NAME, headers, new ArrayList<>(), proxy);
			final JSONObject jsoobj = http.getJSONHTTPresponse();
			if (jsoobj != null) {
				final JSONArray array = jsoobj.getJSONArray(Constants.NEW_RELIC_APPLICATION_HOSTS);
				for (int j = 0 ; j < array.length() ; j++)
					newRelicApplicationHosts.add(new NewRelicApplicationHost(array.getJSONObject(j)));
			}
		} finally {
			if (http != null) {
				http.closeHttpClient();
			}
		}
		return newRelicApplicationHosts;
	}

	/**
	 * This method lists metrics available for a given host. Metrics are separated by a newline.
	 * REST URL is: https://api.newrelic.com/v2/applications/<applicationId>/hosts/<hostId>/metrics.json
	 * More info on NewRelic documentation: https://rpm.newrelic.com/api/explore/application_hosts/names
	 * @return
	 * @throws IOException
	 */
	public List<String> getMetricNamesForHost(final String hostId) throws ClientProtocolException, IOException {
		final List<String> metricNamesForHost = new ArrayList<>();
		final String url = Constants.NEW_RELIC_API_APPLICATIONS_URL + applicationId + Constants.NEW_RELIC_HOSTS + hostId
				+ Constants.NEW_RELIC_METRICS_JSON;
		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
		HTTPGenerator http = null;
		try {
			http = new HTTPGenerator(url, HttpGet.METHOD_NAME, headers, new ArrayList<>(), proxy);
			final JSONObject jsoobj = http.getJSONHTTPresponse();
			if (jsoobj != null) {
				final JSONArray array = jsoobj.getJSONArray(Constants.NEW_RELIC_METRICS);
				for (int i = 0 ; i < array.length() ; i++) {
					final String metricName = array.getJSONObject(i).getString("name");
					if (isRelevantMetricName(metricName)) {
						metricNamesForHost.add(metricName);
					}
				}

			}
		} finally {
			if (http != null) {
				http.closeHttpClient();
			}
		}
		return metricNamesForHost;
	}

	/**
	 * This method retrieves the data for the given metrics.
	 * REST URL is: https://api.newrelic.com/v2/applications/<applicationId>/hosts/<hostId>/metrics/data.json
	 * More info on NewRelic documentation: https://rpm.newrelic.com/api/explore/application_hosts/data
	 * @return
	 * @throws IOException
	 * @throws ParseException 
	 */
	public List<NewRelicMetricData> getNewRelicMetricData(final List<String> metricNames, final String hostId, final String hostName) throws IOException, ParseException {
		final List<NewRelicMetricData> newRelicMetricData = new ArrayList<>();
		final String url = Constants.NEW_RELIC_API_APPLICATIONS_URL + applicationId + "/hosts/" + hostId + Constants.NEW_RELIC_DATA_JSON;
		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
		final List<Pair<String, String>> parameters = new ArrayList<>();
		for(final String metricName: metricNames){
			parameters.add(new ImmutablePair<>("names[]", metricName));
		}		
		final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);		
		parameters.add(new ImmutablePair<>("from", now.minusSeconds(120).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));		
		parameters.add(new ImmutablePair<>("to", now.minusSeconds(60).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
		parameters.add(new ImmutablePair<>("summarize", "true"));
		parameters.add(new ImmutablePair<>("raw", "false"));
		HTTPGenerator http = null;
		try {
			http = new HTTPGenerator(url, HttpGet.METHOD_NAME, headers, parameters, proxy);
			final JSONObject jsoobj = http.getJSONHTTPresponse();
			if (jsoobj != null) {
				if (jsoobj.has("metric_data")) {
					final JSONObject metric_data = jsoobj.getJSONObject("metric_data");
					final JSONArray metrics = metric_data.getJSONArray("metrics");
					for (int i = 0 ; i < metrics.length() ; i++) {
						final JSONObject metric = metrics.getJSONObject(i);
						final String metricName = metric.get("name").toString();
						final JSONArray timeslices = metric.getJSONArray("timeslices");
						if(timeslices.length()!=1){
							continue;
						}
						final JSONObject timeslice = timeslices.getJSONObject(0);						
						final String metricDate = getTimeMillisFromDate(timeslice.getString("to"));						
						final JSONObject values = timeslice.getJSONObject("values");
						final Iterator<String> it = values.keys();
						while (it.hasNext()) {
							final String metricValue = it.next();
							if (isRelevantMetricValue(metricValue)){
								newRelicMetricData.add(new NewRelicMetricData(newRelicActionArguments.getNewRelicApplicationName(), hostName,
										metricName, metricValue, values.getDouble(metricValue), "", metricDate));
							}						
						}
					}
				}
			}
		} finally {
			if (http != null) {
				http.closeHttpClient();
			}
		}
		return newRelicMetricData;
	}

	private static String getTimeMillisFromDate(final String date) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
		formatter.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
		Date d = formatter.parse(date);
		return String.valueOf(d.getTime());
	}

	private boolean isRelevantMetricName(final String metricName) {
		if (Strings.isNullOrEmpty(metricName)) {
			return false;
		}
		for (final String relevantMetricName : newRelicActionArguments.getNewRelicRelevantMetricNames()) {
			if (metricName.contains(relevantMetricName)) {
				return true;
			}
		}
		return false;
	}

	private boolean isRelevantMetricValue(final String metricValue) {
		if (Strings.isNullOrEmpty(metricValue)) {
			return false;
		}
		for (final String relevantMetricValue : newRelicActionArguments.getNewRelicRelevantMetricValues()) {
			if (metricValue.contains(relevantMetricValue)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method send NL Web element values to New Relic Insights API.
	 * REST URL is: https://insights-collector.newrelic.com/v1/accounts/<accountId>/events
	 * More info on NewRelic documentation: https://docs.newrelic.com/docs/insights/insights-data-sources/custom-data/insert-custom-events-insights-api
	 * @return
	 * @throws MalformedURLException 
	 * @throws IOException
	 * @throws ParseException 
	 */
	public void sendValuesMetricToInsightsAPI(final NLWebElementValue nlWebElementValue) throws NewRelicException, IOException {
		final String url = Constants.NEW_RELIC_INSIGHT_URL + newRelicActionArguments.getNewRelicAccountId().get() + "/events";
		final String jsonString = "[{\"eventType\":\"NeoLoadValues\","
				+ "\"account\" : \"" + newRelicActionArguments.getNewRelicAccountId().get() + "\","
				+ "\"appId\" : \"" + applicationId + "\","
				+ "\"testName\" : \"" + context.getTestName() + "\","
				+ "\"scenarioName\" : \"" + context.getScenarioName() + "\","
				+ "\"applicationName\" : \"" + newRelicActionArguments.getNewRelicApplicationName() + "\","
				+ "\"trendfield\": \"" + newRelicActionArguments.getNewRelicApplicationName() + context.getScenarioName() + context.getTestName()
				+ "\","
				+ "\"userPathName\" :\"" + nlWebElementValue.getUserPath() + "\","
				+ "\"type\" :\"TRANSACTION\","
				+ "\"transactionName\" :\"" + nlWebElementValue.getName() + "\","
				+ "\"path\" :\"" + nlWebElementValue.getPath() + "\","
				+ "\"responseTime\":" + Constants.DECIMAL_FORMAT.format(nlWebElementValue.getResponsetime()) + ","
				+ "\"elementPerSecond\":" + Constants.DECIMAL_FORMAT.format(nlWebElementValue.getHitPerSecond()) + ","
				+ "\"downloadedBytesPerSecond\":" + Constants.DECIMAL_FORMAT.format(nlWebElementValue.getThroughput()) + ","
				+ "\"timestamp\" : " + System.currentTimeMillis() + "}]";
		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
		HTTPGenerator http = null;
		try {
			http = new HTTPGenerator(url, headers, jsonString, proxy);
			final String exceptionMessage = NewRelicUtils.getExceptionmessage(http.getHttpResponseCodeFromResponse());
			if (exceptionMessage != null) {
				throw new NewRelicException(exceptionMessage);
			}
		} finally {
			if (http != null) {
				http.closeHttpClient();
			}
		}
	}

	/**
	 * This method send NL Web global data values to New Relic Insights API.
	 * REST URL is: https://insights-collector.newrelic.com/v1/accounts/<accountId>/events
	 * More info on NewRelic documentation: https://docs.newrelic.com/docs/insights/insights-data-sources/custom-data/insert-custom-events-insights-api
	 * @return
	 * @throws MalformedURLException 
	 * @throws IOException
	 * @throws ParseException 
	 */
	public void sendNLWebMainStatisticsToInsightsAPI(final List<String[]> data) throws IOException, NewRelicException {
		final String url = Constants.NEW_RELIC_INSIGHT_URL + newRelicActionArguments.getNewRelicAccountId().get() + "/events";
		final StringBuilder jsonString = new StringBuilder();
		jsonString.append("[{\"eventType\":\"NeoLoadData\","
				+ "\"account\" : \"" + newRelicActionArguments.getNewRelicAccountId().get() + "\","
				+ "\"appId\" : \"" + applicationId + "\","
				+ "\"testName\" : \"" + context.getTestName() + "\","
				+ "\"scenarioName\" : \"" + context.getScenarioName() + "\","
				+ "\"applicationName\" : \"" + newRelicActionArguments.getNewRelicApplicationName() + "\","
				+ "\"trendfield\": \"" + newRelicActionArguments.getNewRelicApplicationName() + context.getScenarioName()
				+ context.getTestName() + "\",");
		for (final String[] metric : data) {
			jsonString.append("\"").append(metric[1]).append("\" : ").append(metric[3]).append(",");
		}
		jsonString.append("\"MetricUnit\" : \"\",\"timestamp\" : ").append(System.currentTimeMillis()).append("}]");
		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
		HTTPGenerator http = null;
		try {			
			http = new HTTPGenerator(url, headers, jsonString.toString(), proxy);
			final String exceptionMessage = NewRelicUtils.getExceptionmessage(http.getHttpResponseCodeFromResponse());
			if (exceptionMessage != null) {
				throw new NewRelicException(exceptionMessage);
			}
		} finally {
			if (http != null) {
				http.closeHttpClient();
			}
		}
	}
	
	/**
	 * This method send NL Web global data values to New Relic Plateform API.
	 * REST URL is: https://platform-api.newrelic.com/platform/v1/metrics
	 * More info on NewRelic documentation: https://docs.newrelic.com/docs/plugins/plugin-developer-resources/developer-reference/work-directly-plugin-api
	 * @return
	 * @throws IOException
	 * @throws ParseException 
	 */
	public void sendNLWebMainStatisticsToPlateformAPI(final String metricName, final String metricPath, final int duration, final String unit, final String value) throws NewRelicException, IOException {
		final String url = Constants.NEW_RELIC_PLATFORM_API_URL;
		final String jsonString = "{\"agent\":{"
				+ "\"host\" : \"" + Constants.CUSTOM_ACTION_HOST + "\","
				+ "\"version\" : \"" + Constants.CUSTOM_ACTION_VERSION + "\""
				+ "},"
				+ "\"components\": ["
				+ "{"
				+ "\"name\": \"NeoLoad\","
				+ "\"guid\": \"" + Constants.CUSTOM_ACTION_HOST + "\","
				+ " \"duration\" : " + String.valueOf(duration) + ","
				+ " \"metrics\" : {"
				+ " \"Component/NeoLoad/Statistics/" + metricPath + "[" + unit + "]\": " + value + ""
				+ "}"
				+ "}"
				+ "]"
				+ "}";
		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
		HTTPGenerator http = null;
		try {
			http = new HTTPGenerator(url, headers, jsonString, proxy);
			final String exceptionMessage = NewRelicUtils.getExceptionmessage(http.getHttpResponseCodeFromResponse());
			if (exceptionMessage != null) {
				throw new NewRelicException(exceptionMessage);
			}
		} finally {
			if (http != null) {
				http.closeHttpClient();
			}
		}	
	}
}
