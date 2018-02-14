package com.neotys.newrelic.rest;

import static com.neotys.newrelic.NewRelicUtils.getProxy;

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
import java.util.SimpleTimeZone;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import com.neotys.newrelic.Constants;
import com.neotys.newrelic.NewRelicActionArguments;
import com.neotys.newrelic.NewRelicException;

/**
 * 
 * @author srichert
 * @date 14 févr. 2018
 */
public class NewRelicRestClient {

	/**
	 * This method retrieves the id of an appliaction given its name.
	 * REST URL is: https://api.newrelic.com/v2/applications.json
	 * More info on NewRelic documentation: https://rpm.newrelic.com/api/explore/applications/list
	 * @return
	 * @throws IOException
	 */
	public static String getApplicationId(final NewRelicActionArguments newRelicActionArguments, final Context context)
			throws NewRelicException, IOException {
		final String url = Constants.NEW_RELIC_API_APPLICATIONS_JSON_URL;
		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
		final HashMap<String, String> parameters = new HashMap<>();
		parameters.put("filter[name]", newRelicActionArguments.getNewRelicApplicationName());
		final HashMap<String, String> header = new HashMap<>();
		header.put("X-Api-Key", newRelicActionArguments.getNewRelicAPIKey());
		header.put("Content-Type", "application/json");
		HTTPGenerator http = null;
		try {
			http = new HTTPGenerator(url, HttpGet.METHOD_NAME, header, parameters, proxy);
			final JSONObject jsoobj = http.getJSONHTTPresponse();
			if (jsoobj != null) {
				if (jsoobj.has("applications")) {
					final JSONArray jsonArray = jsoobj.getJSONArray("applications");
					final String id = String.valueOf(jsonArray.getJSONObject(0).getInt("id"));
					if (!Strings.isNullOrEmpty(id)) {
						return id;
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
	 * @throws IOException
	 */
	public static List<NewRelicApplicationHost> getApplicationHosts(final NewRelicActionArguments newRelicActionArguments,
			final String newRelicApplicationId, final Context context) throws IOException {
		final List<NewRelicApplicationHost> newRelicApplicationHosts = new ArrayList<>();
		final String url = Constants.NEW_RELIC_API_APPLICATIONS_URL + newRelicApplicationId + Constants.HOSTS_JSON;
		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
		final HashMap<String, String> header = new HashMap<>();
		header.put("X-Api-Key", newRelicActionArguments.getNewRelicAPIKey());
		header.put("Content-Type", "application/json");
		HTTPGenerator http = null;
		try {
			http = new HTTPGenerator(url, HttpGet.METHOD_NAME, header, new HashMap<>(), proxy);
			final JSONObject jsoobj = http.getJSONHTTPresponse();
			if (jsoobj != null) {
				final JSONArray array = jsoobj.getJSONArray(Constants.APPLICATION_HOSTS);
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
	public static String getMetricNamesForHost(final NewRelicActionArguments newRelicActionArguments, final String newRelicApplicationId,
			final Context context, final String hostId) throws ClientProtocolException, IOException {
		final StringBuilder metricNamesForHost = new StringBuilder();
		final String url = Constants.NEW_RELIC_API_APPLICATIONS_URL + newRelicApplicationId + Constants.HOSTS + hostId + Constants.METRICS_JSON;
		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
		final HashMap<String, String> header = new HashMap<>();
		header.put("X-Api-Key", newRelicActionArguments.getNewRelicAPIKey());
		header.put("Content-Type", "application/json");
		HTTPGenerator http = null;
		try {
			http = new HTTPGenerator(url, HttpGet.METHOD_NAME, header, new HashMap<>(), proxy);
			final JSONObject jsoobj = http.getJSONHTTPresponse();
			if (jsoobj != null) {
				final JSONArray array = jsoobj.getJSONArray(Constants.METRICS);
				for (int i = 0 ; i < array.length() ; i++) {
					final String metricName = array.getJSONObject(i).getString("name");
					if (isRelevantMetricName(metricName)) {
						metricNamesForHost.append(metricName).append("\r\n");
					}
				}

			}
		} finally {
			if (http != null) {
				http.closeHttpClient();
			}
		}
		return metricNamesForHost.toString();
	}

	/**
	 * This method retrieves the data for the given metrics.
	 * REST URL is: https://api.newrelic.com/v2/applications/<applicationId>/hosts/<hostId>/metrics/data.json
	 * More info on NewRelic documentation: https://rpm.newrelic.com/api/explore/application_hosts/data
	 * @return
	 * @throws IOException
	 * @throws ParseException 
	 */
	public static List<NewRelicMetricData> getNewRelicMetricData(final String metricNames, final NewRelicActionArguments newRelicActionArguments,
			final String newRelicApplicationId, final Context context, final String hostId, final long startTimestamp, final String hostName) throws IOException, ParseException {
		final List<NewRelicMetricData> newRelicMetricData = new ArrayList<>();
		final String url = Constants.NEW_RELIC_API_APPLICATIONS_URL + newRelicApplicationId + "/hosts/" + hostId + Constants.DATA_JSON;
		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
		final HashMap<String, String> header = new HashMap<>();
		header.put("X-Api-Key", newRelicActionArguments.getNewRelicAPIKey());
		header.put("Content-Type", "application/json");
		final HashMap<String, String> parameters = new HashMap<>();
		parameters.put("names[]", metricNames);
		parameters.put("from", ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(60).format(DateTimeFormatter.ofPattern("yyyy-MM-dd+HH:mm:ss")));
		parameters.put("period", "1");// Not sure about that... on the NewRelic documentation they mention a number of second, but whatever I tried it seems to be minutes !
		parameters.put("summarize", "true");
		parameters.put("raw", "true");
		HTTPGenerator http = null;		
		try {
			http = new HTTPGenerator(url, HttpGet.METHOD_NAME, header, parameters, proxy);
			final JSONObject jsoobj = http.getJSONHTTPresponse();
			if (jsoobj != null) {
				if (jsoobj.has("metric_data")) {
					final JSONObject metric_data = jsoobj.getJSONObject("metric_data");
					final JSONArray array = metric_data.getJSONArray("metrics");
					for (int i = 0 ; i < array.length() ; i++) {
						final JSONArray timeslices = array.getJSONObject(i).getJSONArray("timeslices");
						for (int j = 0 ; j < timeslices.length() ; j++) {
							String metricDate = timeslices.getJSONObject(j).getString("from");
							final long metricdate = getTimeMillisFromDate(metricDate);
							if (metricdate >= startTimestamp) {
								metricDate = String.valueOf(metricdate);
								final JSONObject values = timeslices.getJSONObject(j).getJSONObject("values");
								final Iterator<String> it = values.keys();
								while (it.hasNext()) {
									final String metricValue = it.next();
									if (isRelevantMetricValue(metricValue))
										// TODO: seb parse
										newRelicMetricData.add(new NewRelicMetricData(newRelicActionArguments.getNewRelicApplicationName(), hostName, metricNames, metricValue,
												values.getDouble(metricValue), "", metricDate));
								}
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
	
	private static long getTimeMillisFromDate(final String date) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
		formatter.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
		Date d = formatter.parse(date);
		long timestamp = d.getTime();

		return timestamp;
	}
	
	private static boolean isRelevantMetricName(final String metricName) {	
		if(Strings.isNullOrEmpty(metricName)){
			return false;
		}
		for (final String relevantMetricName : Constants.RELEVANT_METRIC_NAMES) {
			if (metricName.contains(relevantMetricName)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean isRelevantMetricValue(final String metricValue) {
		if(Strings.isNullOrEmpty(metricValue)){
			return false;
		}
		for (final String relevantMetricValue : Constants.RELEVANT_METRIC_VALUES) {
			if (metricValue.contains(relevantMetricValue)) {
				return true;
			}
		}
		return false;
	}
}
