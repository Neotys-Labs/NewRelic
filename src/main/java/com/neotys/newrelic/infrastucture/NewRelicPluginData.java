package com.neotys.newrelic.infrastucture;


import com.google.common.base.Optional;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import io.swagger.client.ApiClient;
import io.swagger.client.api.ResultsApi;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Timer;

import static com.neotys.newrelic.NewRelicUtils.getProxy;
import static com.neotys.newrelic.NewRelicUtils.initProxyForNeoloadWebApiClient;


public class NewRelicPluginData {
	private static final String NLWEB_VERSION = "v1";
	private static final int TIMERFREQUENCY=30000;
	private static final int TIMERDELAY=0;
	
	private final NLGlobalStat nlStat;		
	private final Timer timerNewRelic;	
	private final ApiClient neoloadWebApiClient;
	
	private final NeoLoadStatAggregator nlAggregator;
	
	public NewRelicPluginData(String newRelicLicenseKey, final Context neoloadContext, final String insightAccountId, final String insightApiKey, final String applicationName, final String applicationApiKey, final Optional<String> proxyName) throws NewRelicException, IOException, NoSuchAlgorithmException, KeyManagementException {
		super();	

		//----define  the NLWEB API-----
		this.neoloadWebApiClient = new ApiClient();
		this.neoloadWebApiClient.setApiKey(neoloadContext.getAccountToken());
		final String basePath = getBasePath(neoloadContext);
		this.neoloadWebApiClient.setBasePath(basePath);
		final Optional<Proxy> proxyOptional = getProxy(neoloadContext, proxyName, basePath);
		if(proxyOptional.isPresent()) {
			initProxyForNeoloadWebApiClient(neoloadWebApiClient, proxyOptional.get());
		}	
		this.nlStat=new NLGlobalStat();				
		this.nlAggregator=new NeoLoadStatAggregator(newRelicLicenseKey,new ResultsApi(neoloadWebApiClient),neoloadContext.getTestId(),nlStat,insightAccountId,insightApiKey,neoloadContext.getTestName(),applicationName,applicationApiKey,neoloadContext.getScenarioName(), neoloadContext, proxyName);
		this.timerNewRelic = new Timer();
		timerNewRelic.scheduleAtFixedRate(nlAggregator,TIMERDELAY,TIMERFREQUENCY);
	
	}

	private static String getBasePath(final Context context) {
		final String webPlatformApiUrl = context.getWebPlatformApiUrl();
		final StringBuilder basePathBuilder = new StringBuilder(webPlatformApiUrl);
		if(!webPlatformApiUrl.endsWith("/")) {
			basePathBuilder.append("/");
		}
		basePathBuilder.append(NLWEB_VERSION + "/");
		return basePathBuilder.toString();
	}
	
	
}
