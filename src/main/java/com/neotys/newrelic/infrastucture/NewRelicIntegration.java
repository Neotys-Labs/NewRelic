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
import java.util.Map.Entry;
import java.util.SimpleTimeZone;

import org.apache.http.client.ClientProtocolException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Optional;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import com.neotys.newrelic.Constants;
import com.neotys.newrelic.http.HTTPGenerator;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClient;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClientFactory;
import com.neotys.rest.dataexchange.model.ContextBuilder;
import com.neotys.rest.dataexchange.model.EntryBuilder;
import com.neotys.rest.error.NeotysAPIException;

public class NewRelicIntegration {
	private DataExchangeAPIClient client;
	private HTTPGenerator http;
	
	private String newRelicApplication;
	private String newRelicApplicationId;
	private final Context context;
	private final com.google.common.base.Optional<String> proxyName;
	private long startingTS;
	private HashMap<String, String> header = null;
	private HashMap<String, String> Parameters = null;
	
	public NewRelicIntegration(final String newRelicApiKey, String NewRelicApplication, final String dataExchangeApiUrl,
			final com.google.common.base.Optional<String> dataExchangeApiKey, final com.neotys.extensions.action.engine.Context context,
			final com.google.common.base.Optional<String> proxyName, final long startTS) {
		this.context = context;
		this.proxyName = proxyName;
		startingTS = startTS;
		header = new HashMap<>();
		header.put("X-Api-Key", newRelicApiKey);
		header.put("Content-Type", "application/json");
		final ContextBuilder contextBuilder = new ContextBuilder();
		contextBuilder.hardware(Constants.NEOLOAD_CONTEXT_HARDWARE)
			.location(Constants.NEOLOAD_CONTEXT_LOCATION)
			.software(Constants.NEOLOAD_CONTEXT_SOFTWARE)
			.script("NewRelicInfrasfructureMonitoring" + System.currentTimeMillis());

		newRelicApplication = NewRelicApplication;

		try {
			client = DataExchangeAPIClientFactory.newClient(dataExchangeApiUrl, contextBuilder.build(), dataExchangeApiKey.orNull());
		} catch (GeneralSecurityException | IOException | ODataException | URISyntaxException | NeotysAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void startMonitor(StringBuilder build)
			throws NewRelicException, IOException, JSONException, GeneralSecurityException, URISyntaxException, NeotysAPIException, ParseException {

		newRelicApplicationId = getApplicationID();
		final Map<String, String> Hosts = getApplicationsHosts();
		for (Entry<String, String> entry : Hosts.entrySet()) {
			final List<String> metrics = getMetricNameByHost(entry.getKey());
			for (int i = 0 ; i < metrics.size() ; i++) {
				build.append("Sending Metrics Data for Application" + newRelicApplication + " on the host " + entry.getValue() + " metrics name "
						+ metrics.get(i) + "\n");
				sendMetricDataFromMetricsNameToNL(metrics.get(i), entry.getKey(), newRelicApplication, entry.getValue());
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
		Url = Constants.NEW_RELIC_URL + "applications/" + newRelicApplicationId + Constants.NEW_RELIC_HOST_API;

		final Optional<Proxy> proxy = getProxy(context, proxyName, Url);
		http = new HTTPGenerator(Url, "GET", header, Parameters, proxy);

		jsoobj = http.getJSONHTTPresponse();
		if (jsoobj != null) {
			Hostnames = new HashMap<>();
			array = jsoobj.getJSONArray("application_hosts");
			for (int j = 0 ; j < array.length() ; j++)
				Hostnames.put(String.valueOf(array.getJSONObject(j).getInt("id")), array.getJSONObject(j).getString("host"));
		}

		http.closeHttpClient();
		return Hostnames;
	}

	private static long getTimeMillisFromDate(final String date) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
		formatter.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
		Date d = formatter.parse(date);
		long timestamp = d.getTime();

		return timestamp;
	}

	private static com.neotys.rest.dataexchange.model.Entry createEntry(String ApplicationName, String HostName, String MetricName,
			String MetricValueName, double value, String unit,
			String ValueDate) {
		final EntryBuilder entryBuilder = new EntryBuilder(Arrays.asList("NewRelic", ApplicationName, HostName, MetricName, MetricValueName),
				Long.parseLong(ValueDate));
		entryBuilder.unit(unit);
		entryBuilder.value(value);
		return entryBuilder.build();
	}

	public List<String> getMetricNameByHost(final String hostId) throws ClientProtocolException, IOException {
		JSONObject jsoobj;
		JSONArray array;
		final List<String> metriNames = new ArrayList<>();
		String Url = Constants.NEW_RELIC_URL + "applications/" + newRelicApplicationId + "/hosts/" + hostId + Constants.NEW_RELIC_METRIC_NAME_API;
		Parameters = null;

		final Optional<Proxy> proxy = getProxy(context, proxyName, Url);
		http = new HTTPGenerator(Url, "GET", header, Parameters, proxy);

		jsoobj = http.getJSONHTTPresponse();
		if (jsoobj != null) {
			array = jsoobj.getJSONArray("metrics");
			for (int i = 0 ; i < array.length() ; i++) {
				if (isRelevantMetricNameForHost(array.getJSONObject(i).getString("name")))
					metriNames.add(array.getJSONObject(i).getString("name"));
			}

		}
		http.closeHttpClient();
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
		final String url = Constants.NEW_RELIC_URL + "applications/" + newRelicApplicationId + "/hosts/" + HostID + Constants.NEW_RELIC_METRIC_DATA_API;
		final Optional<Proxy> proxy = getProxy(context, proxyName, url);
		http = new HTTPGenerator(url, "GET", header, Parameters, proxy);

		final List<com.neotys.rest.dataexchange.model.Entry> entries = new ArrayList<>();
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
						if (metricdate >= startingTS) {
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
		client.addEntries(entries);
		http.closeHttpClient();
	}

	public String getApplicationID() throws NewRelicException, IOException {
		JSONObject jsoobj;
		String Url;
		JSONArray jsonApplication;

		Url = Constants.NEW_RELIC_URL + Constants.APPLICATIONS_JSON;
		Parameters = new HashMap<>();
		Parameters.put("filter[name]", newRelicApplication);

		final Optional<Proxy> proxy = getProxy(context, proxyName, Url);
		http = new HTTPGenerator(Url, "GET", header, Parameters, proxy);

		jsoobj = http.getJSONHTTPresponse();
		if (jsoobj != null) {
			if (jsoobj.has("applications")) {
				jsonApplication = jsoobj.getJSONArray("applications");
				newRelicApplicationId = String.valueOf(jsonApplication.getJSONObject(0).getInt("id"));
				if (newRelicApplicationId == null)
					throw new NewRelicException("No Application find in The NewRelic Account");
			} else {
				newRelicApplicationId = null;
			}
		} else {
			newRelicApplicationId = null;
		}
		http.closeHttpClient();
		return newRelicApplicationId;
	}

}
