package com.neotys.newrelic.fromnlweb;

import static com.neotys.newrelic.NewRelicUtils.getProxy;
import static com.neotys.newrelic.NewRelicUtils.initProxyForNeoloadWebApiClient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import com.neotys.newrelic.Constants;
import com.neotys.newrelic.NewRelicActionArguments;
import com.neotys.newrelic.NewRelicException;
import com.neotys.newrelic.rest.NewRelicRestClient;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.ResultsApi;
import io.swagger.client.model.ArrayOfElementDefinition;
import io.swagger.client.model.ElementDefinition;
import io.swagger.client.model.ElementValues;
import io.swagger.client.model.TestStatistics;

public class NLWebToNewRelicTask {
	
	private final NewRelicRestClient newRelicRestClient;
	private final String testId;			
	private final ApiClient neoloadWebApiClient;	
	private final ResultsApi resultsApi;		
	private final NLWebStats nlWebStats;	
	
	public NLWebToNewRelicTask(final NewRelicRestClient newRelicRestClient, final Context context, final NewRelicActionArguments newRelicActionArguments) throws MalformedURLException, KeyManagementException, NoSuchAlgorithmException {
		this.newRelicRestClient = newRelicRestClient;
		this.testId = context.getTestId();
		this.neoloadWebApiClient = new ApiClient();
		this.neoloadWebApiClient.setApiKey(context.getAccountToken());
		final String webPlatformApiUrl = context.getWebPlatformApiUrl();
		final StringBuilder basePathBuilder = new StringBuilder(webPlatformApiUrl);
		if(!webPlatformApiUrl.endsWith("/")) {
			basePathBuilder.append("/");
		}
		basePathBuilder.append(Constants.NLWEB_VERSION + "/");
		final String basePath = basePathBuilder.toString();		
		this.neoloadWebApiClient.setBasePath(basePath);
		final Optional<Proxy> proxyOptional = getProxy(context, newRelicActionArguments.getProxyName(), basePath);
		if(proxyOptional.isPresent()) {
			initProxyForNeoloadWebApiClient(neoloadWebApiClient, proxyOptional.get());
		}					
		this.resultsApi = new ResultsApi(neoloadWebApiClient);	
		this.nlWebStats = new NLWebStats();		
	}

	public void sendNLWebMainStatisticsToNewRelic() throws IOException, NewRelicException, ApiException {
		long lastduration;
		final long utc = System.currentTimeMillis() / 1000;
		lastduration = nlWebStats.getLasduration();			
	
		final TestStatistics testStatistics = resultsApi.getTestStatistics(testId);
		if(testStatistics == null){
			throw new ApiException("Cannot find any statistics on NeoLoad Web for testId " + testId);
		}
		nlWebStats.updateTestStatistics(testStatistics);
		final List<String[]> data = nlWebStats.getNLData();
		final int time;	
		if (lastduration == 0){
			time = 0;
		} else {
			time = (int) (utc - lastduration);
		}
		for (final String[] metric : data) {			
			newRelicRestClient.sendNLWebMainStatisticsToPlateformAPI(metric[0], metric[1], time, metric[2], metric[3]);			
		}
		newRelicRestClient.sendNLWebMainStatisticsToInsightsAPI(data);
		nlWebStats.setLasduration(utc);
	}
	
	public void sendNLWebElementValuesToInsightsAPI() throws ApiException, NewRelicException, IOException {		
		final List<NLWebElementValue> nlWebElementValues = new ArrayList<>();		
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
	}
}
