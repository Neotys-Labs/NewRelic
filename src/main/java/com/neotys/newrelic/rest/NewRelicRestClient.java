package com.neotys.newrelic.rest;

import static com.neotys.newrelic.NewRelicUtils.getProxy;
import static com.neotys.newrelic.rest.HTTPGenerator.HTTP_GET_METHOD;
import static com.neotys.newrelic.rest.HttpResponseUtils.getJsonResponse;

import java.io.IOException;
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Strings;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import com.neotys.newrelic.Constants;
import com.neotys.newrelic.NewRelicActionArguments;
import com.neotys.newrelic.NewRelicException;
import com.neotys.newrelic.fromnlweb.NLWebElementValue;
import com.neotys.newrelic.fromnlweb.NLWebMainStatistics;

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

	public NewRelicRestClient(final NewRelicActionArguments newRelicActionArguments, final Context context) throws Exception {
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
	private String getApplicationId() throws Exception {
		final String url = Constants.NEW_RELIC_API_APPLICATIONS_JSON_URL;

		final Multimap<String, String> parameters = ArrayListMultimap.create();
		parameters.put("filter[name]", newRelicActionArguments.getNewRelicApplicationName());
		HTTPGenerator http = null;
		try {
			final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
			http = new HTTPGenerator(HTTP_GET_METHOD, url, headers, parameters, proxy);
			final HttpResponse httpResponse = http.execute();
			if (HttpResponseUtils.isSuccessHttpCode(httpResponse.getStatusLine().getStatusCode())) {
				final JSONObject jsonObject = getJsonResponse(httpResponse);
				if (jsonObject.has("applications")) {
					final JSONArray jsonArray = jsonObject.getJSONArray("applications");
					if (jsonArray.length() == 1) {
						final String id = String.valueOf(jsonArray.getJSONObject(0).getInt("id"));
						if (!Strings.isNullOrEmpty(id)) {
							return id;
						}
					}
				}
			}
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
	 * @throws Exception
	 */
	public List<NewRelicApplicationHost> getApplicationHosts() throws Exception {
		final List<NewRelicApplicationHost> newRelicApplicationHosts = new ArrayList<>();
		final String url = Constants.NEW_RELIC_API_APPLICATIONS_URL + applicationId + Constants.NEW_RELIC_HOSTS_JSON;
		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
		HTTPGenerator http = null;
		try {
			http = new HTTPGenerator(HTTP_GET_METHOD, url, headers, ArrayListMultimap.create(), proxy);
			final HttpResponse httpResponse = http.execute();
			if (HttpResponseUtils.isSuccessHttpCode(httpResponse.getStatusLine().getStatusCode())) {
				final JSONObject jsonObject = getJsonResponse(httpResponse);
				if (jsonObject.has(Constants.NEW_RELIC_APPLICATION_HOSTS)) {
					final JSONArray jsonArray = jsonObject.getJSONArray(Constants.NEW_RELIC_APPLICATION_HOSTS);
					for (int j = 0 ; j < jsonArray.length() ; j++) {
						newRelicApplicationHosts.add(new NewRelicApplicationHost(jsonArray.getJSONObject(j)));
					}
				}
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
	public List<String> getMetricNamesForHost(final String hostId) throws Exception {
		final List<String> metricNamesForHost = new ArrayList<>();
		final String url = Constants.NEW_RELIC_API_APPLICATIONS_URL + applicationId + Constants.NEW_RELIC_HOSTS + hostId
				+ Constants.NEW_RELIC_METRICS_JSON;
		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);

		Multimap<String, String> params = ArrayListMultimap.create();
		boolean hasNextPage;
		do {
			HTTPGenerator http = null;
			hasNextPage = false;
			try {
				http = new HTTPGenerator(HTTP_GET_METHOD, url, headers, params, proxy);
				final HttpResponse httpResponse = http.execute();
				if (HttpResponseUtils.isSuccessHttpCode(httpResponse.getStatusLine().getStatusCode())) {

					retrieveMetricNamesFromJson(metricNamesForHost, httpResponse);

					params.clear();
					hasNextPage = HttpResponseUtils.getNextPageParams(httpResponse, params);
				}
			} finally {
				if (http != null) {
					http.closeHttpClient();
				}
			}
		} while (hasNextPage);

		return metricNamesForHost;
	}

	private void retrieveMetricNamesFromJson(List<String> metricNamesForHost, HttpResponse response) throws IOException {
		final JSONObject jsonObject = getJsonResponse(response);
		if (jsonObject.has(Constants.NEW_RELIC_METRICS)) {
			final JSONArray jsonArray = jsonObject.getJSONArray(Constants.NEW_RELIC_METRICS);
			for (int i = 0; i < jsonArray.length(); i++) {
				final String metricName = jsonArray.getJSONObject(i).getString("name");
				if (isRelevantMetricName(metricName)) {
					metricNamesForHost.add(metricName);
				}
			}
		}
	}

	/**
	 * This method retrieves the data for the given metrics.
	 * REST URL is: https://api.newrelic.com/v2/applications/<applicationId>/hosts/<hostId>/metrics/data.json
	 * More info on NewRelic documentation: https://rpm.newrelic.com/api/explore/application_hosts/data
	 * @return
	 * @throws Exception 
	 */
	public List<NewRelicMetricData> getNewRelicMetricData(final List<String> metricNames, final String hostId, final String hostName)
			throws Exception {
		final List<NewRelicMetricData> newRelicMetricData = new ArrayList<>();
		final String url = Constants.NEW_RELIC_API_APPLICATIONS_URL + applicationId + "/hosts/" + hostId + Constants.NEW_RELIC_DATA_JSON;
		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);

		// The number of metric names could be very important, so here we split them into several requests
		// containing a subset of 30 names to avoid an issue on request length.
		final int STEP = 30;
		int offset = 0;

		while (offset < metricNames.size()) {
			List<String> metricNamesToRequest;
			if(metricNames.size() - offset > STEP) {
				metricNamesToRequest = metricNames.subList(offset, offset + STEP);
				offset += STEP;
			}
			else {
				metricNamesToRequest = metricNames.subList(offset, metricNames.size());
				offset = metricNames.size();
			}

			final Multimap<String, String> parameters = ArrayListMultimap.create();
			parameters.putAll("names[]", metricNamesToRequest);

			final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
			parameters.put("from", now.minusSeconds(120).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
			parameters.put("to", now.minusSeconds(60).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
			parameters.put("summarize", "true");
			parameters.put("raw", "false");
			HTTPGenerator http = null;
			try {
				// There is no need to use pagination the get the whole response here, because we are requesting the last
				// data for each metric name (at least 30 names) for the last minute, then we summarize the data, so at
				// least we will have 30 objects, where the limit for a page is 200 objects.
				http = new HTTPGenerator(HTTP_GET_METHOD, url, headers, parameters, proxy);
				final HttpResponse httpResponse = http.execute();
				if (HttpResponseUtils.isSuccessHttpCode(httpResponse.getStatusLine().getStatusCode())) {
					retrieveMetricDataFromJson(hostName, newRelicMetricData, httpResponse);
				}
			} finally {
				if (http != null) {
					http.closeHttpClient();
				}
			}
		}
		return newRelicMetricData;
	}

	private void retrieveMetricDataFromJson(String hostName, List<NewRelicMetricData> newRelicMetricData, HttpResponse httpResponse) throws IOException, ParseException {
		final JSONObject jsonObject = getJsonResponse(httpResponse);
		if (jsonObject.has("metric_data")) {
			final JSONObject metricData = jsonObject.getJSONObject("metric_data");
			final JSONArray metrics = metricData.getJSONArray("metrics");
			for (int i = 0; i < metrics.length(); i++) {
				final JSONObject metric = metrics.getJSONObject(i);
				final String metricName = metric.get("name").toString();
				final JSONArray timeslices = metric.getJSONArray("timeslices");
				if (timeslices.length() != 1) {
					continue;
				}
				final JSONObject timeslice = timeslices.getJSONObject(0);
				final String metricDate = getTimeMillisFromDate(timeslice.getString("to"));
				final JSONObject values = timeslice.getJSONObject("values");
				final Iterator<String> it = values.keys();
				while (it.hasNext()) {
					final String metricValue = it.next();
					if (isRelevantMetricValue(metricValue)) {
						newRelicMetricData.add(new NewRelicMetricData(newRelicActionArguments.getNewRelicApplicationName(), hostName,
								metricName, metricValue, values.getDouble(metricValue), "", metricDate));
					}
				}
			}
		}
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
	 * @return Optional<String>: error message if any
	 */
	public Optional<String> sendNLWebElementValuesToInsightsAPI(final List<NLWebElementValue> nlWebElementValues) {
		HTTPGenerator http = null;
		try {
			final String accountId = newRelicActionArguments.getNewRelicAccountId().orElse("");
			if (Strings.isNullOrEmpty(accountId)) return Optional.of("Cannot find the account id");

			final String url = Constants.NEW_RELIC_INSIGHT_URL + accountId + "/events";
			final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
			for (final NLWebElementValue nlWebElementValue : nlWebElementValues) {

				final String jsonString = "[{\"eventType\":\"NeoLoadValues\","
						+ "\"account\" : \"" + accountId + "\","
						+ "\"appId\" : \"" + applicationId + "\","
						+ "\"testName\" : \"" + context.getTestName() + "\","
						+ "\"scenarioName\" : \"" + context.getScenarioName() + "\","
						+ "\"applicationName\" : \"" + newRelicActionArguments.getNewRelicApplicationName() + "\","
						+ "\"trendfield\": \"" + newRelicActionArguments.getNewRelicApplicationName() + context.getScenarioName()
						+ context.getTestName()
						+ "\","
						+ "\"userPathName\" :\"" + nlWebElementValue.getUserPath() + "\","
						+ "\"type\" :\"TRANSACTION\","
						+ "\"transactionName\" :\"" + nlWebElementValue.getName() + "\","
						+ "\"path\" :\"" + nlWebElementValue.getPath() + "\","
						+ "\"responseTime\":" + Constants.DECIMAL_FORMAT.format(nlWebElementValue.getResponsetime()) + ","
						+ "\"elementPerSecond\":" + Constants.DECIMAL_FORMAT.format(nlWebElementValue.getHitPerSecond()) + ","
						+ "\"downloadedBytesPerSecond\":" + Constants.DECIMAL_FORMAT.format(nlWebElementValue.getThroughput()) + ","
						+ "\"timestamp\" : " + System.currentTimeMillis() + "}]";

				http = HTTPGenerator.newJsonHttpGenerator(HTTP_GET_METHOD, url, headers, ArrayListMultimap.create(), proxy, jsonString);
				final HttpResponse httpResponse = http.execute();
				final String exceptionMessage = HttpResponseUtils.getStringResponse(httpResponse);
				if (exceptionMessage != null) {
					return Optional.of(exceptionMessage);
				}
			}
		} catch (final Exception e) {
			return Optional.of(e.getMessage());
		} finally {
			if (http != null) {
				http.closeHttpClient();
			}
		}
		return Optional.empty();
	}

	/**
	 * This method send NL Web global data values to New Relic Insights API.
	 * REST URL is: https://insights-collector.newrelic.com/v1/accounts/<accountId>/events
	 * More info on NewRelic documentation: https://docs.newrelic.com/docs/insights/insights-data-sources/custom-data/insert-custom-events-insights-api
	 * @return Optional<String>: error message if any
	 */
	public Optional<String> sendNLWebMainStatisticsToInsightsAPI(final NLWebMainStatistics nlWebMainStatistics) {
		final String accountId = newRelicActionArguments.getNewRelicAccountId().orElse("");
		if (Strings.isNullOrEmpty(accountId)) return Optional.of("Cannot find the account id");

		final String url = Constants.NEW_RELIC_INSIGHT_URL + accountId + "/events";
		final StringBuilder jsonString = new StringBuilder();
		jsonString.append("[{\"eventType\":\"NeoLoadData\","
				+ "\"account\" : \"" + accountId + "\","
				+ "\"appId\" : \"" + applicationId + "\","
				+ "\"testName\" : \"" + context.getTestName() + "\","
				+ "\"scenarioName\" : \"" + context.getScenarioName() + "\","
				+ "\"applicationName\" : \"" + newRelicActionArguments.getNewRelicApplicationName() + "\","
				+ "\"trendfield\": \"" + newRelicActionArguments.getNewRelicApplicationName() + context.getScenarioName()
				+ context.getTestName() + "\",");
		for (final String[] metric : nlWebMainStatistics.getNlData()) {
			jsonString.append("\"").append(metric[1]).append("\" : ").append(metric[3]).append(",");
		}
		jsonString.append("\"MetricUnit\" : \"\",\"timestamp\" : ").append(System.currentTimeMillis()).append("}]");

		HTTPGenerator http = null;
		try {
			final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
			http = HTTPGenerator.newJsonHttpGenerator(HTTP_GET_METHOD, url, headers, ArrayListMultimap.create(), proxy, jsonString.toString());
			final HttpResponse httpResponse = http.execute();
			final String exceptionMessage = HttpResponseUtils.getStringResponse(httpResponse);
			if (exceptionMessage != null) {
				return Optional.of(exceptionMessage);
			}
		} catch (final Exception e) {
			return Optional.of(e.getMessage());
		} finally {
			if (http != null) {
				http.closeHttpClient();
			}
		}
		return Optional.empty();
	}

	/**
	 * This method send NL Web global data values to New Relic Plateform API.
	 * REST URL is: https://platform-api.newrelic.com/platform/v1/metrics
	 * More info on NewRelic documentation: https://docs.newrelic.com/docs/plugins/plugin-developer-resources/developer-reference/work-directly-plugin-api
	 * @return Optional<String>: error message if any
	 */
	public Optional<String> sendNLWebMainStatisticsToPlateformAPI(final NLWebMainStatistics nlWebMainStatistics) {
		HTTPGenerator http = null;
		try {
			final String url = Constants.NEW_RELIC_PLATFORM_API_URL;
			final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);

			for (final String[] metric : nlWebMainStatistics.getNlData()) {
				final String metricPath = metric[1];
				final String unit = metric[2];
				final String value = metric[3];
				final String jsonString = "{\"agent\":{"
						+ "\"host\" : \"" + Constants.CUSTOM_ACTION_HOST + "\","
						+ "\"version\" : \"" + Constants.CUSTOM_ACTION_VERSION + "\""
						+ "},"
						+ "\"components\": ["
						+ "{"
						+ "\"name\": \"NeoLoad\","
						+ "\"guid\": \"" + Constants.CUSTOM_ACTION_HOST + "\","
						+ " \"duration\" : " + nlWebMainStatistics.getDuration() + ","
						+ " \"metrics\" : {"
						+ " \"Component/NeoLoad/Statistics/" + metricPath + "[" + unit + "]\": " + value + ""
						+ "}"
						+ "}"
						+ "]"
						+ "}";

				http = HTTPGenerator.newJsonHttpGenerator(HTTP_GET_METHOD, url, headers, ArrayListMultimap.create(), proxy, jsonString);
				final HttpResponse httpResponse = http.execute();
				final String exceptionMessage = HttpResponseUtils.getStringResponse(httpResponse);
				if (exceptionMessage != null) {
					return Optional.of(exceptionMessage);
				}
			}
		} catch (final Exception e) {
			return Optional.of(e.getMessage());
		} finally {
			if (http != null) {
				http.closeHttpClient();
			}
		}
		return Optional.empty();
	}
}
