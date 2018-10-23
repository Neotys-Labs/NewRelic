package com.neotys.newrelic;

import java.util.*;

import static com.google.common.base.Strings.emptyToNull;

/**
 * 
 * @author srichert
 * @date 13 févr. 2018
 */
public class NewRelicActionArguments {
	
	// New Relic
	private final String newRelicAPIKey;
	private final String newRelicApplicationName;	
	private final Optional<String> newRelicLicenseKey;
	private final Optional<String> newRelicAccountId;
	private final Optional<String> newRelicInsightsAPIKey;
	private final List<String> newRelicRelevantMetricNames;
	private final List<String> newRelicRelevantMetricValues;
		
	// NeoLoad -> New Relic
	private final boolean sendNLWebDataToNewRelic;
	private final Optional<String> proxyName;
	
	// NeoLoad
	private final Optional<String> dataExchangeApiUrl;
	private final Optional<String> dataExchangeApiKey;
	
	public NewRelicActionArguments(final Map<String, com.google.common.base.Optional<String>> parsedArgs) throws IllegalArgumentException {	
		// Required
		this.newRelicAPIKey = parsedArgs.get(NewRelicOption.NewRelicAPIKey.getName()).get();
		this.newRelicApplicationName = parsedArgs.get(NewRelicOption.NewRelicApplicationName.getName()).get();
		this.dataExchangeApiUrl = Optional.ofNullable(emptyToNull(parsedArgs.get(NewRelicOption.NeoLoadDataExchangeApiUrl.getName()).orNull()));
		
		// Optional
		final Optional<String> sendNLWebDataToNewRelicArg = Optional.ofNullable(parsedArgs.get(NewRelicOption.SendNLWebDataToNewRelic.getName()).orNull());	
		this.sendNLWebDataToNewRelic = sendNLWebDataToNewRelicArg.map(a -> "true".equals(a)).orElse(false);
		this.newRelicLicenseKey = Optional.ofNullable(parsedArgs.get(NewRelicOption.NewRelicLicenseKey.getName()).orNull());	
		this.newRelicAccountId = Optional.ofNullable(parsedArgs.get(NewRelicOption.NewRelicAccountId.getName()).orNull());	
		this.newRelicInsightsAPIKey = Optional.ofNullable(parsedArgs.get(NewRelicOption.NewRelicInsightsAPIKey.getName()).orNull());	
		this.dataExchangeApiKey = Optional.ofNullable(parsedArgs.get(NewRelicOption.NeoLoadDataExchangeApiKey.getName()).orNull());	
		this.proxyName = Optional.ofNullable(parsedArgs.get(NewRelicOption.NeoLoadProxy.getName()).orNull());
		final String newRelicRelevantMetricNamesString = parsedArgs.get(NewRelicOption.NewRelicRelevantMetricNames.getName()).or(Constants.NEW_RELIC_DEFAULT_RELEVANT_METRIC_NAMES);
		this.newRelicRelevantMetricNames = new ArrayList<>(Arrays.asList(newRelicRelevantMetricNamesString.split("\\s*,\\s*")));
		final String newRelicRelevantMetricValuesString = parsedArgs.get(NewRelicOption.NewRelicRelevantMetricValues.getName()).or(Constants.NEW_RELIC_DEFAULT_RELEVANT_METRIC_VALUES);
		this.newRelicRelevantMetricValues = new ArrayList<>(Arrays.asList(newRelicRelevantMetricValuesString.split("\\s*,\\s*")));
	}
	
	public String getNewRelicAPIKey() {
		return newRelicAPIKey;
	}
	
	public String getNewRelicApplicationName() {
		return newRelicApplicationName;
	}
	
	public Optional<String> getDataExchangeApiUrl() {
		return dataExchangeApiUrl;
	}
	public boolean isSendNLWebDataToNewRelic() {
		return sendNLWebDataToNewRelic;
	}
	
	public Optional<String> getNewRelicLicenseKey() {
		return newRelicLicenseKey;
	}
	
	public Optional<String> getNewRelicAccountId() {
		return newRelicAccountId;
	}
	public Optional<String> getNewRelicInsightsAPIKey() {
		return newRelicInsightsAPIKey;
	}
	
	public Optional<String> getDataExchangeApiKey() {
		return dataExchangeApiKey;
	}
	
	public Optional<String> getProxyName() {
		return proxyName;
	}
	
	public List<String> getNewRelicRelevantMetricNames() {
		return newRelicRelevantMetricNames;
	}
	
	public List<String> getNewRelicRelevantMetricValues() {
		return newRelicRelevantMetricValues;
	}
}
