package com.neotys.newrelic.infrastucture;

import static com.neotys.newrelic.NewRelicUtils.getProxy;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.neotys.rest.dataexchange.model.Entry;
import java.util.SimpleTimeZone;

import org.apache.http.client.ClientProtocolException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import com.neotys.newrelic.Constants;
import com.neotys.newrelic.HTTPGenerator;
import com.neotys.newrelic.NewRelicActionArguments;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClient;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClientFactory;
import com.neotys.rest.dataexchange.model.ContextBuilder;
import com.neotys.rest.dataexchange.model.EntryBuilder;
import com.neotys.rest.error.NeotysAPIException;

public class NewRelicToNLDataExchange {
	private DataExchangeAPIClient client;
		
	private final NewRelicActionArguments newRelicActionArguments;
	private final String newRelicApplicationId;
	private final Context context;	
	private final long startTimestamp;
	private HashMap<String, String> header = null;
	private HashMap<String, String> Parameters = null;
	
	public NewRelicToNLDataExchange(final Context context, final NewRelicActionArguments newRelicActionArguments, final long startTimestamp) throws NewRelicException, IOException {
		this.context = context;	
		this.newRelicActionArguments = newRelicActionArguments;
		this.startTimestamp = startTimestamp;
		header = new HashMap<>();
		header.put("X-Api-Key", newRelicActionArguments.getNewRelicAPIKey());
		header.put("Content-Type", "application/json");
		final ContextBuilder contextBuilder = new ContextBuilder();
		contextBuilder.hardware(Constants.NEOLOAD_CONTEXT_HARDWARE)
			.location(Constants.NEOLOAD_CONTEXT_LOCATION)
			.software(Constants.NEOLOAD_CONTEXT_SOFTWARE)
			.script("NewRelicInfrasfructureMonitoring" + System.currentTimeMillis());

		try {
			client = DataExchangeAPIClientFactory.newClient(newRelicActionArguments.getDataExchangeApiUrl(), contextBuilder.build(), newRelicActionArguments.getDataExchangeApiKey().orNull());
		} catch (GeneralSecurityException | IOException | ODataException | URISyntaxException | NeotysAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.newRelicApplicationId = getApplicationID();
	}

	public void startMonitor(StringBuilder build)
			throws IOException, JSONException, GeneralSecurityException, URISyntaxException, NeotysAPIException, ParseException {

		for (java.util.Map.Entry<String, String> entry : getApplicationsHosts().entrySet()) {
			final List<String> metrics = getMetricNameByHost(entry.getKey());
			for (int i = 0 ; i < metrics.size() ; i++) {
				build.append("Sending Metrics Data for Application" + newRelicActionArguments.getNewRelicApplicationName() + " on the host " + entry.getValue() + " metrics name "
						+ metrics.get(i) + "\n");
				sendMetricDataFromMetricsNameToNL(metrics.get(i), entry.getKey(), newRelicActionArguments.getNewRelicApplicationName(), entry.getValue());
			}

		}

	}

	private static boolean isRelevantMetricNameForHost(final String metricname) {
		boolean result = false;
		for (String listItem : Constants.RELEVANT_METRIC_NAMES_FOR_HOST) {
			if (metricname.contains(listItem)) {
				return true;
			}
		}
		return result;
	}

	public Map<String, String> getApplicationsHosts() throws IOException {
		JSONObject jsoobj;
		String Url;
		JSONArray array;
		HashMap<String, String> Hostnames = null;
		Parameters = null;
		Url = Constants.NEW_RELIC_API_URL + "applications/" + newRelicApplicationId + Constants.HOSTS_JSON;

		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), Url);
		HTTPGenerator http = null;
		try{
			http = new HTTPGenerator(Url, "GET", header, Parameters, proxy);
			jsoobj = http.getJSONHTTPresponse();
			if (jsoobj != null) {
				Hostnames = new HashMap<>();
				array = jsoobj.getJSONArray("application_hosts");
				for (int j = 0 ; j < array.length() ; j++)
					Hostnames.put(String.valueOf(array.getJSONObject(j).getInt("id")), array.getJSONObject(j).getString("host"));
			}
		} finally {
			if(http != null){
				http.closeHttpClient();
			}
		}
		return Hostnames;
	}

	private static long getTimeMillisFromDate(final String date) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
		formatter.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
		Date d = formatter.parse(date);
		long timestamp = d.getTime();

		return timestamp;
	}

	private static Entry createEntry(final String applicationName, final String hostName, final String metricPath,
			final String metricName, final double metricValue, final String unit, final String timestamp) {
		final List<String> path = Arrays.asList(Constants.NEW_RELIC, applicationName, hostName);
		path.addAll(Arrays.asList(metricPath.split("/")));
		path.add(metricName);
		final EntryBuilder entryBuilder = new EntryBuilder(path, Long.parseLong(timestamp));
		entryBuilder.unit(unit);
		entryBuilder.value(metricValue);
		return entryBuilder.build();
	}

	public List<String> getMetricNameByHost(final String hostId) throws ClientProtocolException, IOException {
		JSONObject jsoobj;
		JSONArray array;
		final List<String> metriNames = new ArrayList<>();
		final String url = Constants.NEW_RELIC_API_URL + "applications/" + newRelicApplicationId + "/hosts/" + hostId + Constants.METRICS_JSON;
		Parameters = null;

		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
		HTTPGenerator http = null;
		try{
			http = new HTTPGenerator(url, "GET", header, Parameters, proxy);
	
			jsoobj = http.getJSONHTTPresponse();
			if (jsoobj != null) {
				array = jsoobj.getJSONArray("metrics");
				for (int i = 0 ; i < array.length() ; i++) {
					if (isRelevantMetricNameForHost(array.getJSONObject(i).getString("name")))
						metriNames.add(array.getJSONObject(i).getString("name"));
				}
	
			}
		} finally {
			if(http != null){
				http.closeHttpClient();
			}
		}
		return metriNames;
	}

	private static boolean isMetricRelevant(final String name) {
		boolean result = false;
		for (String listItem : Constants.RELEVANT_METRIC_NAMES) {
			if (name.contains(listItem)) {
				return true;
			}
		}
		return result;
	}

	public void sendMetricDataFromMetricsNameToNL(final String MetricName, final String HostID, final String Applicationname, final String Hostname)
			throws ClientProtocolException, IOException, JSONException, GeneralSecurityException, URISyntaxException, NeotysAPIException,
			ParseException {

		String metricDate;

		Parameters = new HashMap<>();
		Parameters.put("names[]", MetricName);
		Parameters.put("period", "1");
		Parameters.put("summarize", "false");
		Parameters.put("raw", "true");
		final String url = Constants.NEW_RELIC_API_URL + "applications/" + newRelicApplicationId + "/hosts/" + HostID + Constants.DATA_JSON;
		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
		HTTPGenerator http = null;
		final List<Entry> entries = new ArrayList<>();
		try{
			http = new HTTPGenerator(url, "GET", header, Parameters, proxy);

			
			final JSONObject jsoobj = http.getJSONHTTPresponse();
			if (jsoobj != null) {
				if (jsoobj.has("metric_data")) {
					final JSONObject metric_data = jsoobj.getJSONObject("metric_data");
	
					final JSONArray array = metric_data.getJSONArray("metrics");
					for (int i = 0 ; i < array.length() ; i++) {
						final JSONArray timeslices = array.getJSONObject(i).getJSONArray("timeslices");
						for (int j = 0 ; j < timeslices.length() ; j++) {
							metricDate = timeslices.getJSONObject(j).getString("from");
							final long metricdate = getTimeMillisFromDate(metricDate);
							if (metricdate >= startTimestamp) {
								metricDate = String.valueOf(metricdate);
								final JSONObject values = timeslices.getJSONObject(j).getJSONObject("values");
								final Iterator<String> it = values.keys();
								while (it.hasNext()) {
									final String metricValueName = it.next();
									if (isMetricRelevant(metricValueName))
										entries.add(
												createEntry(Applicationname, Hostname, MetricName, metricValueName, values.getDouble(metricValueName), "",
														metricDate));
								}
							}
						}
					}
				}
			}			
		} finally {			
			if(http != null){
				http.closeHttpClient();
			}
			if(!entries.isEmpty()){
				client.addEntries(entries);
			}
		}
		
	}

	public String getApplicationID() throws NewRelicException, IOException {
		JSONObject jsoobj;		
		JSONArray jsonApplication;

		final String url = Constants.NEW_RELIC_API_URL + Constants.APPLICATIONS_JSON;
		Parameters = new HashMap<>();
		Parameters.put("filter[name]", newRelicActionArguments.getNewRelicApplicationName());

		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
		HTTPGenerator http = null;
		try{
			http = new HTTPGenerator(url, "GET", header, Parameters, proxy);
			jsoobj = http.getJSONHTTPresponse();
			if (jsoobj != null) {
				if (jsoobj.has("applications")) {
					jsonApplication = jsoobj.getJSONArray("applications");
					final String id = String.valueOf(jsonApplication.getJSONObject(0).getInt("id"));
					if(!Strings.isNullOrEmpty(id)){
						return id;
					} 
				}
			}
		} finally {
			if(http != null){
				http.closeHttpClient();
			}
		}
		throw new NewRelicException("No Application found for name '" + newRelicActionArguments.getNewRelicApplicationName() + "'.");
	}

}
