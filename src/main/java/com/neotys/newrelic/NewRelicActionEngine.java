package com.neotys.newrelic;

import java.util.List;
import java.util.stream.Collectors;

import com.neotys.action.result.ResultFactory;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.SampleResult;
import com.neotys.newrelic.fromnlweb.NLWebToNewRelic;
import com.neotys.newrelic.rest.NewRelicApplicationHost;
import com.neotys.newrelic.rest.NewRelicMetricData;
import com.neotys.newrelic.rest.NewRelicRestClient;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClient;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClientFactory;
import com.neotys.rest.dataexchange.model.ContextBuilder;

public final class NewRelicActionEngine implements ActionEngine {

	@Override
	public SampleResult execute(Context context, List<ActionParameter> parameters) {
		final SampleResult sampleResult = new SampleResult();

		// Parse arguments
		final NewRelicActionArguments newRelicActionArguments;
		try {
			newRelicActionArguments = new NewRelicActionArguments(context, parameters);
		} catch (final IllegalArgumentException iae) {
			return ResultFactory.newErrorResult(context, Constants.STATUS_CODE_INVALID_PARAMETER, "Issue while parsing advanced action arguments: ",
					iae);
		}

		// When option to sendNLWebDataToNewRelic is on, then 3 other parameters should be present as well
		if (newRelicActionArguments.isSendNLWebDataToNewRelic()) {
			if (!newRelicActionArguments.getNewRelicLicenseKey().isPresent()) {
				return ResultFactory.newErrorResult(context, Constants.STATUS_CODE_INVALID_PARAMETER,
						"The New Relic license key is required when argument 'sendNLWebDataToNewRelic' is true.");
			}
			if (!newRelicActionArguments.getNewRelicAccountId().isPresent()) {
				return ResultFactory.newErrorResult(context, Constants.STATUS_CODE_INVALID_PARAMETER,
						"The New Relic Account Id is required when argument 'sendNLWebDataToNewRelic' is true.");
			}
			if (!newRelicActionArguments.getNewRelicInsightsAPIKey().isPresent()) {
				return ResultFactory.newErrorResult(context, Constants.STATUS_CODE_INVALID_PARAMETER,
						"The New Relic Insights API yey Id is required when argument 'sendNLWebDataToNewRelic' is true.");
			}
		}

		// When option to sendNLWebDataToNewRelic is on, then a NeoLoad Web test must be running
		if (newRelicActionArguments.isSendNLWebDataToNewRelic()) {
			if (context.getWebPlatformRunningTestUrl() == null) {
				return ResultFactory.newErrorResult(context, Constants.STATUS_CODE_BAD_CONTEXT,
						"A NeoLoad Web test should be running when argument 'sendNLWebDataToNewRelic' is true.");
			}

			// Retrieve NewRelicRestClient from Context, or instantiate new one
			NewRelicRestClient newRelicRestClient = (NewRelicRestClient) context.getCurrentVirtualUser().get("NewRelicRestClient");
			if (newRelicRestClient == null) {
				try {
					newRelicRestClient = new NewRelicRestClient(newRelicActionArguments, context);
					context.getCurrentVirtualUser().put("NewRelicRestClient", newRelicRestClient);
				} catch (final Exception e) {
					return ResultFactory.newErrorResult(context, Constants.STATUS_CODE_TECHNICAL_ERROR,
							"Technical Error encouter while creating New Relic Rest Client:", e);
				}
			}

			// Retrieve DataExchangeAPIClient from Context, or instantiate new one
			DataExchangeAPIClient dataExchangeAPIClient = (DataExchangeAPIClient) context.getCurrentVirtualUser().get("DataExchangeAPIClient");
			if (dataExchangeAPIClient == null) {
				try {
					final ContextBuilder contextBuilder = new ContextBuilder();
					contextBuilder.hardware(Constants.NEOLOAD_CONTEXT_HARDWARE).location(Constants.NEOLOAD_CONTEXT_LOCATION).software(
							Constants.NEOLOAD_CONTEXT_SOFTWARE).script("NewRelicInfrasfructureMonitoring" + System.currentTimeMillis());
					dataExchangeAPIClient = DataExchangeAPIClientFactory.newClient(newRelicActionArguments.getDataExchangeApiUrl(),
							contextBuilder.build(),
							newRelicActionArguments.getDataExchangeApiKey().orElse(null));
					context.getCurrentVirtualUser().put("DataExchangeAPIClient", dataExchangeAPIClient);
				} catch (final Exception e) {
					return ResultFactory.newErrorResult(context, Constants.STATUS_CODE_TECHNICAL_ERROR,
							"Technical Error encouter while creating DataExchangeAPI Client:", e);
				}
			}

			final StringBuilder responseContentBuilder = new StringBuilder();
			try {
				sampleResult.setRequestContent("Retrieve New Relic Metric Data and inject them to NeoLoad through DataExchange API.\n");
				sampleResult.sampleStart();
				for (final NewRelicApplicationHost newRelicApplicationHost : newRelicRestClient.getApplicationHosts()) {
					final List<String> metricNames = newRelicRestClient.getMetricNamesForHost(newRelicApplicationHost.getHostId());
					responseContentBuilder.append("Sending New Relic Metrics Data for host " + newRelicApplicationHost.getHostName()
							+ "and metric names :\n" + metricNames);					
					final List<NewRelicMetricData> newRelicMetricData = newRelicRestClient.getNewRelicMetricData(metricNames,
							newRelicApplicationHost.getHostId(), newRelicApplicationHost.getHostName());
					if (!newRelicMetricData.isEmpty()) {
						dataExchangeAPIClient.addEntries(newRelicMetricData.stream().map(n -> n.buildEntry()).collect(Collectors.toList()));
					}
				}
			} catch (final Exception e) {
				return ResultFactory.newErrorResult(context, Constants.STATUS_CODE_TECHNICAL_ERROR,
						"Technical Error encouter while sending New Relic Metric Data and inject them to NeoLoad through DataExchange API:", e);
			} finally {
				sampleResult.sampleEnd();
				sampleResult.setResponseContent(responseContentBuilder.toString());
			}

			if (newRelicActionArguments.isSendNLWebDataToNewRelic()) {
				// Retrieve NLWebToNewRelic from Context, or instantiate new one
				NLWebToNewRelic nlWebToNewRelic = (NLWebToNewRelic) context.getCurrentVirtualUser().get("NLWebToNewRelic");
				if (nlWebToNewRelic == null) {
					try {
						nlWebToNewRelic = new NLWebToNewRelic(newRelicRestClient, context, newRelicActionArguments);
						context.getCurrentVirtualUser().put("NLWebToNewRelic", nlWebToNewRelic);
					} catch (final Exception e) {
						return ResultFactory.newErrorResult(context, Constants.STATUS_CODE_TECHNICAL_ERROR,
								"Technical Error encouter while sending New Relic Metric Data and inject them to NeoLoad through DataExchange API:",
								e);
					}
				}
			}

		}
		return sampleResult;

	}

	@Override
	public void stopExecute() {
		// nothing to do
	}

}
