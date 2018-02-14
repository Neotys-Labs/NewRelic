package com.neotys.newrelic.rest;

import static com.neotys.newrelic.NewRelicUtils.getProxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

public class NewRelicRestClient {
	
	/**
	 * This method retrieves the id of an appliaction given its name.
	 * REST URL is: https://api.newrelic.com/v2/applications.json
	 * More info on NewRelic documentation: https://rpm.newrelic.com/api/explore/applications/list
	 * @return
	 * @throws IOException
	 */
	public static String getApplicationId(final NewRelicActionArguments newRelicActionArguments, final Context context) throws NewRelicException, IOException {
		final String url = Constants.NEW_RELIC_API_APPLICATIONS_JSON_URL;
		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
		final HashMap<String, String> parameters = new HashMap<>();
		parameters.put("filter[name]", newRelicActionArguments.getNewRelicApplicationName());
		final  HashMap<String, String> header = new HashMap<>();
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
	public static List<NewRelicApplicationHost> getApplicationHosts(final NewRelicActionArguments newRelicActionArguments, final String newRelicApplicationId, final Context context) throws IOException {		
		final List<NewRelicApplicationHost> newRelicApplicationHosts = new ArrayList<>();
		final String url = Constants.NEW_RELIC_API_APPLICATIONS_URL + newRelicApplicationId + Constants.HOSTS_JSON;
		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
		final  HashMap<String, String> header = new HashMap<>();
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
	 * This method lists metrics available for a given host.
	 * REST URL is: https://api.newrelic.com/v2/applications/<applicationId>/hosts/<hostId>/metrics.json
	 * More info on NewRelic documentation: https://rpm.newrelic.com/api/explore/application_hosts/names
	 * @return
	 * @throws IOException
	 */
	public static List<String> getMetricNamesForHost(final NewRelicActionArguments newRelicActionArguments, final String newRelicApplicationId, final Context context, final String hostId) throws ClientProtocolException, IOException {		
		final List<String> metricNamesForHost = new ArrayList<>();
		final String url = Constants.NEW_RELIC_API_APPLICATIONS_URL + newRelicApplicationId + Constants.HOSTS + hostId + Constants.METRICS_JSON;
		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
		final  HashMap<String, String> header = new HashMap<>();
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
					if (isRelevantMetricName(metricName)){
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


}
