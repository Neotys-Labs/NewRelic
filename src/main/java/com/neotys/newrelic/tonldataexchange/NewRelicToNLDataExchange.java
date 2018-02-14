package com.neotys.newrelic.tonldataexchange;

import static com.neotys.newrelic.NewRelicUtils.getProxy;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import com.neotys.newrelic.NewRelicActionArguments;
import com.neotys.newrelic.NewRelicException;
import com.neotys.newrelic.rest.HTTPGenerator;
import com.neotys.newrelic.rest.NewRelicApplicationHost;
import com.neotys.newrelic.rest.NewRelicRestClient;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClient;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClientFactory;
import com.neotys.rest.dataexchange.model.ContextBuilder;
import com.neotys.rest.dataexchange.model.Entry;
import com.neotys.rest.dataexchange.model.EntryBuilder;
import com.neotys.rest.error.NeotysAPIException;

public class NewRelicToNLDataExchange {
	private DataExchangeAPIClient client;

	private final NewRelicActionArguments newRelicActionArguments;
	private final String newRelicApplicationId;
	private final Context context;
	private final long startTimestamp;
	private HashMap<String, String> header = null;
	
	public NewRelicToNLDataExchange(final String newRelicApplicationId, final Context context, final NewRelicActionArguments newRelicActionArguments, final long startTimestamp)
			throws NewRelicException, IOException {
		this.context = context;
		this.newRelicActionArguments = newRelicActionArguments;
		this.startTimestamp = startTimestamp;
		header = new HashMap<>();
		header.put("X-Api-Key", newRelicActionArguments.getNewRelicAPIKey());
		header.put("Content-Type", "application/json");
		final ContextBuilder contextBuilder = new ContextBuilder();
		contextBuilder.hardware(Constants.NEOLOAD_CONTEXT_HARDWARE).location(Constants.NEOLOAD_CONTEXT_LOCATION).software(
				Constants.NEOLOAD_CONTEXT_SOFTWARE).script("NewRelicInfrasfructureMonitoring" + System.currentTimeMillis());

		try {
			client = DataExchangeAPIClientFactory.newClient(newRelicActionArguments.getDataExchangeApiUrl(), contextBuilder.build(),
					newRelicActionArguments.getDataExchangeApiKey().orNull());
		} catch (GeneralSecurityException | IOException | ODataException | URISyntaxException | NeotysAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.newRelicApplicationId = newRelicApplicationId;
	}

	public void startMonitor(StringBuilder build)
			throws IOException, JSONException, GeneralSecurityException, URISyntaxException, NeotysAPIException, ParseException {

		for (final NewRelicApplicationHost newRelicApplicationHost : NewRelicRestClient.getApplicationHosts(newRelicActionArguments, newRelicApplicationId, context)) {
			final List<String> metrics = NewRelicRestClient.getMetricNamesForHost(newRelicActionArguments, newRelicApplicationId, context, newRelicApplicationHost.getHostId());
			for (final String metric: metrics) {
				build.append("Sending Metrics Data for Application " + newRelicActionArguments.getNewRelicApplicationName() + " on the host "
						+ newRelicApplicationHost.getHostName() + ", metrics name " + metric + "\n");
				sendMetricDataFromMetricsNameToNL(metric, newRelicApplicationHost.getHostId(), newRelicActionArguments.getNewRelicApplicationName(),
						newRelicApplicationHost.getHostName());
			}

		}

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

	

	private static boolean isMetricRelevant(final String name) {
		boolean result = false;
		for (String listItem : Constants.RELEVANT_METRIC_NAME_VALUES) {
			if (name.contains(listItem)) {
				return true;
			}
		}
		return result;
	}

	private static String getCurrentDateUTC() {
		return ZonedDateTime.now(ZoneOffset.UTC).toString().subSequence(0,19)+"+00:00";
	}

	public void sendMetricDataFromMetricsNameToNL(final String MetricName, final String HostID, final String Applicationname, final String Hostname)
			throws ClientProtocolException, IOException, JSONException, GeneralSecurityException, URISyntaxException, NeotysAPIException,
			ParseException {

		String metricDate;

		final HashMap<String, String> parameters = new HashMap<>();
		parameters.put("names[]", MetricName);
		parameters.put("from", getCurrentDateUTC());
		parameters.put("period", "60");// Not sure about that... on the NewRelic documentation they mention a number of second, but whatever I tried it seems to be minutes !
		parameters.put("summarize", "true");
		parameters.put("raw", "true");
		final String url = Constants.NEW_RELIC_API_APPLICATIONS_URL + newRelicApplicationId + "/hosts/" + HostID + Constants.DATA_JSON;
		final Optional<Proxy> proxy = getProxy(context, newRelicActionArguments.getProxyName(), url);
		HTTPGenerator http = null;
		final List<Entry> entries = new ArrayList<>();
		try {
			http = new HTTPGenerator(url, "GET", header, parameters, proxy);

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
												createEntry(Applicationname, Hostname, MetricName, metricValueName, values.getDouble(metricValueName),
														"",
														metricDate));
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
			if (!entries.isEmpty()) {
				client.addEntries(entries);
			}
		}

	}

	

}
