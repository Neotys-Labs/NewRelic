package com.neotys.newrelic.tonldataexchange;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.ClientProtocolException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.json.JSONException;

import com.neotys.extensions.action.engine.Context;
import com.neotys.newrelic.Constants;
import com.neotys.newrelic.NewRelicActionArguments;
import com.neotys.newrelic.NewRelicException;
import com.neotys.newrelic.rest.NewRelicApplicationHost;
import com.neotys.newrelic.rest.NewRelicMetricData;
import com.neotys.newrelic.rest.NewRelicRestClient;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClient;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClientFactory;
import com.neotys.rest.dataexchange.model.ContextBuilder;
import com.neotys.rest.error.NeotysAPIException;

public class NewRelicToNLDataExchange {
	private DataExchangeAPIClient client;

	private final NewRelicActionArguments newRelicActionArguments;
	private final String newRelicApplicationId;
	private final Context context;
	private final long startTimestamp;
	private HashMap<String, String> header = null;

	public NewRelicToNLDataExchange(final String newRelicApplicationId, final Context context, final NewRelicActionArguments newRelicActionArguments,
			final long startTimestamp)
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

	public void startMonitor(StringBuilder build) throws IOException, JSONException, GeneralSecurityException, URISyntaxException, NeotysAPIException, ParseException {

		for (final NewRelicApplicationHost newRelicApplicationHost : NewRelicRestClient.getApplicationHosts(newRelicActionArguments,
				newRelicApplicationId, context)) {
			final String metricNames = NewRelicRestClient.getMetricNamesForHost(newRelicActionArguments, newRelicApplicationId, context,
					newRelicApplicationHost.getHostId());
			build.append("Sending Metrics Data for Application " + newRelicActionArguments.getNewRelicApplicationName() + " on the host "
					+ newRelicApplicationHost.getHostName() + ", metric names " + metricNames + "\n");
			sendMetricDataFromMetricsNameToNL(metricNames, newRelicApplicationHost.getHostId(), newRelicActionArguments.getNewRelicApplicationName(),
					newRelicApplicationHost.getHostName());
		}
	}

	public void sendMetricDataFromMetricsNameToNL(final String metricNames, final String hostID, final String applicationName, final String hostName)
			throws ClientProtocolException, IOException, JSONException, GeneralSecurityException, URISyntaxException, NeotysAPIException, ParseException {
		final List<NewRelicMetricData> newRelicMetricData = NewRelicRestClient.getNewRelicMetricData(metricNames, newRelicActionArguments,
				newRelicApplicationId, context, hostID, startTimestamp, hostName);

		if (!newRelicMetricData.isEmpty()) {
			client.addEntries(newRelicMetricData.stream().map(n -> n.buildEntry()).collect(Collectors.toList()));
		}
	}
}
