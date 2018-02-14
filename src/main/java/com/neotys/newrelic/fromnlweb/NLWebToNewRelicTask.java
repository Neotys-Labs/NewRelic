package com.neotys.newrelic.fromnlweb;

import static com.neotys.newrelic.NewRelicUtils.getProxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import com.neotys.newrelic.Constants;
import com.neotys.newrelic.NewRelicActionArguments;
import com.neotys.newrelic.NewRelicException;
import com.neotys.newrelic.NewRelicUtils;
import com.neotys.newrelic.rest.HTTPGenerator;

import io.swagger.client.ApiException;
import io.swagger.client.api.ResultsApi;
import io.swagger.client.model.ArrayOfElementDefinition;
import io.swagger.client.model.ElementDefinition;
import io.swagger.client.model.ElementValues;
import io.swagger.client.model.TestStatistics;

public class NLWebToNewRelicTask extends TimerTask {
	
	private final Context neoloadContext;	
	private final NewRelicActionArguments newRelicActionArguments;
	private final String componentsName;	
	private final ResultsApi nlWebResult;		
	private final Map<String, String> headers = new HashMap<>();
	private NLWebStats nlStat;	
	private final String newRelicApplicationId;
	

	public NLWebToNewRelicTask(final String newRelicApplicationId, final Context neoloadContext, final NewRelicActionArguments newRelicActionArguments, final ResultsApi nlWebResult, final NLWebStats nlStat) throws NewRelicException, IOException {
		this.neoloadContext = neoloadContext;
		this.newRelicActionArguments = newRelicActionArguments;
		this.componentsName = "Statistics";		
		this.nlStat = nlStat;		
		this.nlWebResult = nlWebResult;
		this.newRelicApplicationId = newRelicApplicationId;		 
		headers.put("X-License-Key", newRelicActionArguments.getNewRelicLicenseKey().get());
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json");
	}

	private void sendStatsToNewRelic() throws ApiException {
		TestStatistics StatsResult;
		long utc;
		long lasduration;

		utc = System.currentTimeMillis() / 1000;

		lasduration = nlStat.getLasduration();

		if (lasduration == 0 || (utc - lasduration) >= Constants.MIN_NEW_RELIC_DURATION) {
			StatsResult = nlWebResult.getTestStatistics(neoloadContext.getTestId());
			lasduration = sendMetricsToNewRelic(StatsResult, lasduration);
			nlStat.setLasduration(lasduration);
			sendNLWebMetricsToInsightsAPI();
		}
	}

	private long sendMetricsToNewRelic(final TestStatistics stat, final long LasDuration) {
		int time = 0;
		List<String[]> data;
		long utc;
		utc = System.currentTimeMillis() / 1000;

		if (nlStat == null)
			nlStat = new NLWebStats(stat);
		else {
			nlStat.UpdateStat(stat);
		}
		data = nlStat.GetNLData();
		if (LasDuration == 0)
			time = 0;
		else {
			time = (int) (utc - LasDuration);
		}
		for (String[] metric : data) {
			try {
				sendMetricToPluginAPI(metric[0], metric[1], time, metric[2], metric[3]);

			} catch (NewRelicException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			sendMetricsToInsightAPI(data, time);
		} catch (NewRelicException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return utc;
	}

	
	private void sendNLWebMetricsToInsightsAPI() {		
		final List<NLWebElementValue> nlWebElementValues = new ArrayList<>();
		try {
			final ArrayOfElementDefinition NLElement = nlWebResult.getTestElements(neoloadContext.getTestId(), Constants.NLWEB_TRANSACTION);
			for (ElementDefinition element : NLElement) {
				if (element.getType().equalsIgnoreCase("TRANSACTION")) {
					final ElementValues Values = nlWebResult.getTestElementsValues(neoloadContext.getTestId(), element.getId());
					nlWebElementValues.add(new NLWebElementValue(element, Values));
				}
			}
			for (final NLWebElementValue val : nlWebElementValues){
				sendValueMetricToInsightsAPI(val);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendValueMetricToInsightsAPI(final NLWebElementValue nlWebElementValue) throws NewRelicException {
		final Map<String, String> head = new HashMap<>();
		head.put("X-Insert-Key", newRelicActionArguments.getNewRelicInsightsAPIKey().get());
		head.put("Content-Type", "application/json");		
		final String url = Constants.NEW_RELIC_INSIGHT_URL + newRelicActionArguments.getNewRelicAccountId() + "/events";	
		final String jsonString = "[{\"eventType\":\"NeoLoadValues\","
				+ "\"account\" : \"" + newRelicActionArguments.getNewRelicAccountId() + "\","
				+ "\"appId\" : \"" + newRelicApplicationId + "\","
				+ "\"testName\" : \"" + neoloadContext.getTestName() + "\","
				+ "\"scenarioName\" : \"" + neoloadContext.getScenarioName() + "\","
				+ "\"applicationName\" : \"" + newRelicActionArguments.getNewRelicApplicationName() + "\","
				+ "\"trendfield\": \"" + newRelicActionArguments.getNewRelicApplicationName() + neoloadContext.getScenarioName() + neoloadContext.getTestName() + "\","
				+ "\"userPathName\" :\"" + nlWebElementValue.getUserPath() + "\","
				+ "\"type\" :\"TRANSACTION\","
				+ "\"transactionName\" :\"" + nlWebElementValue.getName() + "\","
				+ "\"path\" :\"" + nlWebElementValue.getPath() + "\","
				+ "\"responseTime\":" + Constants.DECIMAL_FORMAT.format(nlWebElementValue.getResponsetime())+ ","
				+ "\"elementPerSecond\":" + Constants.DECIMAL_FORMAT.format(nlWebElementValue.getHitPerSecond()) + ","
				+ "\"downloadedBytesPerSecond\":" + Constants.DECIMAL_FORMAT.format(nlWebElementValue.getThroughput()) + ","
				+ "\"timestamp\" : " + System.currentTimeMillis() + "}]";
		HTTPGenerator http = null;
		try {
			final com.google.common.base.Optional<Proxy> proxy = getProxy(neoloadContext, newRelicActionArguments.getProxyName(), url);
			http = new HTTPGenerator(url, head, jsonString, proxy);
			final String exceptionMessage = NewRelicUtils.getExceptionmessage(http.getHttpResponseCodeFromResponse());			
			if (exceptionMessage != null){throw new NewRelicException(exceptionMessage);}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (http != null) {
				http.closeHttpClient();
			}
		}

	}

	private void sendMetricsToInsightAPI(List<String[]> data, int time) throws NewRelicException {
		
		final Map<String, String> head = new HashMap<>();
		head.put("X-Insert-Key", newRelicActionArguments.getNewRelicInsightsAPIKey().get());
		head.put("Content-Type", "application/json");
		final String url = Constants.NEW_RELIC_INSIGHT_URL + newRelicActionArguments.getNewRelicAccountId() + "/events";		 
		String jsonString = "[{\"eventType\":\"NeoLoadData\","
				+ "\"account\" : \"" + newRelicActionArguments.getNewRelicAccountId() + "\","
				+ "\"appId\" : \"" + newRelicApplicationId + "\","
				+ "\"testName\" : \"" + neoloadContext.getTestName() + "\","
				+ "\"scenarioName\" : \"" + neoloadContext.getScenarioName() + "\","
				+ "\"applicationName\" : \"" + newRelicActionArguments.getNewRelicApplicationName() + "\","
				+ "\"trendfield\": \"" + newRelicActionArguments.getNewRelicApplicationName() + neoloadContext.getScenarioName() + neoloadContext.getTestName() + "\",";
		for (String[] metric : data) {
			jsonString += "\"" + metric[1] + "\" : " + metric[3] + ",";

		}
		jsonString += "\"MetricUnit\" : \"\","+ "\"timestamp\" : " + System.currentTimeMillis() + "}]";
		HTTPGenerator http = null;
		try {
			final com.google.common.base.Optional<Proxy> proxy = getProxy(neoloadContext, newRelicActionArguments.getProxyName(), url);
			http = new HTTPGenerator(url, head, jsonString, proxy);			
			final String exceptionMessage = NewRelicUtils.getExceptionmessage(http.getHttpResponseCodeFromResponse());			
			if (exceptionMessage != null){throw new NewRelicException(exceptionMessage);}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(http!=null){
				http.closeHttpClient();
			}
		}
	}

	private void sendMetricToPluginAPI(final String MetricName, final String MetricPath, final int Duration, final String unit, final String value) throws NewRelicException {
		final String jsonString = "{\"agent\":{"
				+ "\"host\" : \"" + Constants.CUSTOM_ACTION_HOST + "\","
				+ "\"version\" : \"" + Constants.CUSTOM_ACTION_VERSION + "\""
				+ "},"
				+ "\"components\": ["
				+ "{"
				+ "\"name\": \"NeoLoad\","
				+ "\"guid\": \"" + Constants.CUSTOM_ACTION_HOST + "\","
				+ " \"duration\" : " + String.valueOf(Duration) + ","
				+ " \"metrics\" : {"
				+ " \"Component/NeoLoad/" + componentsName + "/" + MetricPath + "[" + unit + "]\": " + value + ""
				+ "}"
				+ "}"
				+ "]"
				+ "}";

		HTTPGenerator http = null;
		try {
			final com.google.common.base.Optional<Proxy> proxy = getProxy(neoloadContext, newRelicActionArguments.getProxyName(), Constants.NEW_RELIC_PLATFORM_API_URL);
			http = new HTTPGenerator(Constants.NEW_RELIC_PLATFORM_API_URL, headers, jsonString, proxy);
			
			final String exceptionMessage = NewRelicUtils.getExceptionmessage(http.getHttpResponseCodeFromResponse());			
			if (exceptionMessage != null){throw new NewRelicException(exceptionMessage);}			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(http!=null){
				http.closeHttpClient();
			}
		}
	}

	@Override
	public void run() {
		try {
			sendStatsToNewRelic();
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
