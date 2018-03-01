package com.neotys.newrelic.fromnlweb;

import static com.neotys.newrelic.NewRelicUtils.getProxy;
import static com.neotys.newrelic.NewRelicUtils.initProxyForNeoloadWebApiClient;

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

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.ResultsApi;
import io.swagger.client.model.ArrayOfElementDefinition;
import io.swagger.client.model.ElementDefinition;
import io.swagger.client.model.ElementValues;
import io.swagger.client.model.TestStatistics;

public class NLWebClient {
		
	private final String testId;			
	private final ApiClient neoloadWebApiClient;	
	private final ResultsApi resultsApi;		
	private final NLWebStats nlWebStats;	
	
	public NLWebClient(final Context context, final NewRelicActionArguments newRelicActionArguments) throws MalformedURLException, KeyManagementException, NoSuchAlgorithmException {
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
	
	public NLWebMainStatistics getMainStatistics() throws ApiException {
		final Optional<Long> lastTimestamp = nlWebStats.getLastTimestamp();			
		final long currentTimestamp = System.currentTimeMillis();
		nlWebStats.setLastTimestamp(currentTimestamp);		
		final TestStatistics testStatistics = resultsApi.getTestStatistics(testId);
		if(testStatistics == null){
			throw new ApiException("Cannot find any statistics on NeoLoad Web for testId " + testId);
		}
		nlWebStats.updateTestStatistics(testStatistics);
		return new NLWebMainStatistics(nlWebStats.getNLData(), lastTimestamp, currentTimestamp);
	}
	
	public List<NLWebElementValue> getElementValues() throws ApiException {		
		final List<NLWebElementValue> nlWebElementValues = new ArrayList<>();		
		final ArrayOfElementDefinition NLElement = resultsApi.getTestElements(testId, Constants.NLWEB_TRANSACTION);
		for (ElementDefinition element : NLElement) {
			if (element.getType().equalsIgnoreCase("TRANSACTION")) {
				final ElementValues Values = resultsApi.getTestElementsValues(testId, element.getId());
				nlWebElementValues.add(new NLWebElementValue(element, Values));
			}
		}
		return nlWebElementValues;
	}
		
}
