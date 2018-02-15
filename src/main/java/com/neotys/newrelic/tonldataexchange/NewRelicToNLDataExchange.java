package com.neotys.newrelic.tonldataexchange;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.client.ClientProtocolException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.json.JSONException;

import com.neotys.newrelic.Constants;
import com.neotys.newrelic.NewRelicActionArguments;
import com.neotys.newrelic.rest.NewRelicApplicationHost;
import com.neotys.newrelic.rest.NewRelicMetricData;
import com.neotys.newrelic.rest.NewRelicRestClient;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClient;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClientFactory;
import com.neotys.rest.dataexchange.model.ContextBuilder;
import com.neotys.rest.error.NeotysAPIException;

public class NewRelicToNLDataExchange {
	private DataExchangeAPIClient dataExchangeAPIClient;
	private final NewRelicRestClient newRelicRestClient;
	private final long startTimestamp;
	
	public NewRelicToNLDataExchange(final NewRelicActionArguments newRelicActionArguments, final NewRelicRestClient newRelicRestClient, final long startTimestamp) {
		this.newRelicRestClient = newRelicRestClient;		
		this.startTimestamp = startTimestamp;
		
		final ContextBuilder contextBuilder = new ContextBuilder();
		contextBuilder.hardware(Constants.NEOLOAD_CONTEXT_HARDWARE).location(Constants.NEOLOAD_CONTEXT_LOCATION).software(
				Constants.NEOLOAD_CONTEXT_SOFTWARE).script("NewRelicInfrasfructureMonitoring" + System.currentTimeMillis());

		try {
			dataExchangeAPIClient = DataExchangeAPIClientFactory.newClient(newRelicActionArguments.getDataExchangeApiUrl(), contextBuilder.build(),
					newRelicActionArguments.getDataExchangeApiKey().orElse(null));
		} catch (GeneralSecurityException | IOException | ODataException | URISyntaxException | NeotysAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public void startMonitor(StringBuilder build) throws IOException, JSONException, GeneralSecurityException, URISyntaxException, NeotysAPIException, ParseException {

		for (final NewRelicApplicationHost newRelicApplicationHost : newRelicRestClient.getApplicationHosts()) {
			final String metricNames = newRelicRestClient.getMetricNamesForHost(newRelicApplicationHost.getHostId());
			build.append("Sending Metrics Data on the host "
					+ newRelicApplicationHost.getHostName() + ", metric names " + metricNames + "\n");
			sendMetricDataFromMetricsNameToNL(metricNames, newRelicApplicationHost.getHostId(), newRelicApplicationHost.getHostName());
		}
	}

	public void sendMetricDataFromMetricsNameToNL(final String metricNames, final String hostID, final String hostName)
			throws ClientProtocolException, IOException, JSONException, GeneralSecurityException, URISyntaxException, NeotysAPIException, ParseException {
		final List<NewRelicMetricData> newRelicMetricData = newRelicRestClient.getNewRelicMetricData(metricNames, hostID, startTimestamp, hostName);

		if (!newRelicMetricData.isEmpty()) {
			dataExchangeAPIClient.addEntries(newRelicMetricData.stream().map(n -> n.buildEntry()).collect(Collectors.toList()));
		}
	}
}
