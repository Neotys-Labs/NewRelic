package com.neotys.newrelic.fromnlweb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import com.neotys.newrelic.Constants;
import com.neotys.newrelic.NewRelicException;
import com.neotys.newrelic.rest.NewRelicRestClient;

import io.swagger.client.ApiException;
import io.swagger.client.api.ResultsApi;
import io.swagger.client.model.ArrayOfElementDefinition;
import io.swagger.client.model.ElementDefinition;
import io.swagger.client.model.ElementValues;
import io.swagger.client.model.TestStatistics;

public class NLWebToNewRelicTask extends TimerTask {
	
	private final NewRelicRestClient newRelicRestClient;
	private final String testId;			
	private final ResultsApi resultsApi;		
	private NLWebStats nlWebStats;	

	public NLWebToNewRelicTask(final NewRelicRestClient newRelicRestClient, final String testId, final ResultsApi resultsApi, final NLWebStats nlWebStats) {
		this.newRelicRestClient = newRelicRestClient;
		this.testId = testId;
		this.resultsApi = resultsApi;	
		this.nlWebStats = nlWebStats;			
	}

	private void sendStatsToNewRelic() throws ApiException, IOException {
		long lastduration;
		final long utc = System.currentTimeMillis() / 1000;
		lastduration = nlWebStats.getLasduration();
		if (lastduration == 0 || (utc - lastduration) >= Constants.MIN_NEW_RELIC_DURATION) {
			final TestStatistics qtatsResult = resultsApi.getTestStatistics(testId);
			lastduration = sendMetricsToNewRelic(qtatsResult, lastduration);
			nlWebStats.setLasduration(lastduration);
			sendNLWebMetricsToInsightsAPI();
		}
	}

	private long sendMetricsToNewRelic(final TestStatistics testStatistics, final long lastDuration) throws IOException {
		int time = 0;
		List<String[]> data;
		final long utc = System.currentTimeMillis() / 1000;
		if (nlWebStats == null)
			nlWebStats = new NLWebStats(testStatistics);
		else {
			nlWebStats.UpdateStat(testStatistics);
		}
		data = nlWebStats.GetNLData();
		if (lastDuration == 0)
			time = 0;
		else {
			time = (int) (utc - lastDuration);
		}
		for (String[] metric : data) {
			try {
				newRelicRestClient.sendDataMetricToPlateformAPI(metric[0], metric[1], time, metric[2], metric[3]);
			} catch (NewRelicException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			newRelicRestClient.sendDataMetricToInsightsAPI(data);
		} catch (NewRelicException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return utc;
	}

	
	private void sendNLWebMetricsToInsightsAPI() {		
		final List<NLWebElementValue> nlWebElementValues = new ArrayList<>();
		try {
			final ArrayOfElementDefinition NLElement = resultsApi.getTestElements(testId, Constants.NLWEB_TRANSACTION);
			for (ElementDefinition element : NLElement) {
				if (element.getType().equalsIgnoreCase("TRANSACTION")) {
					final ElementValues Values = resultsApi.getTestElementsValues(testId, element.getId());
					nlWebElementValues.add(new NLWebElementValue(element, Values));
				}
			}
			for (final NLWebElementValue val : nlWebElementValues){
				newRelicRestClient.sendValuesMetricToInsightsAPI(val);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			sendStatsToNewRelic();
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
