package com.neotys.newrelic;

import static com.neotys.action.argument.Arguments.getArgumentLogString;
import static com.neotys.action.argument.Arguments.parseArguments;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.Context;

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
		
	// NeoLoad -> New Relic
	private final boolean sendNLWebDataToNewRelic;
	private final Optional<String> proxyName;
	
	// NeoLoad
	private final String dataExchangeApiUrl;
	private final Optional<String> dataExchangeApiKey;
	
	public NewRelicActionArguments(final Context context, final List<ActionParameter> parameters) throws IllegalArgumentException {
		
		final Map<String, com.google.common.base.Optional<String>> parsedArgs = parseArguments(parameters, NewRelicOption.values());
		if (context.getLogger().isDebugEnabled()) {
			context.getLogger().debug("Executing " + this.getClass().getName() + " with parameters: "+ getArgumentLogString(parsedArgs, NewRelicOption.values()));
		}
		// Required
		this.newRelicAPIKey = parsedArgs.get(NewRelicOption.NewRelicAPIKey.getName()).get();
		this.newRelicApplicationName = parsedArgs.get(NewRelicOption.NewRelicApplicationName.getName()).get();
		this.dataExchangeApiUrl = parsedArgs.get(NewRelicOption.NeoLoadDataExchangeApiUrl.getName()).get();
		
		// Optional
		final Optional<String> sendNLWebDataToNewRelicArg = Optional.ofNullable(parsedArgs.get(NewRelicOption.SendNLWebDataToNewRelic.getName()).orNull());	
		this.sendNLWebDataToNewRelic = sendNLWebDataToNewRelicArg.map(a -> "true".equals(a)).orElse(false);
		this.newRelicLicenseKey = Optional.ofNullable(parsedArgs.get(NewRelicOption.NewRelicLicenseKey.getName()).orNull());	
		this.newRelicAccountId = Optional.ofNullable(parsedArgs.get(NewRelicOption.NewRelicAccountId.getName()).orNull());	
		this.newRelicInsightsAPIKey = Optional.ofNullable(parsedArgs.get(NewRelicOption.NewRelicInsightsAPIKey.getName()).orNull());	
		this.dataExchangeApiKey = Optional.ofNullable(parsedArgs.get(NewRelicOption.NeoLoadDataExchangeApiKey.getName()).orNull());	
		this.proxyName = Optional.ofNullable(parsedArgs.get(NewRelicOption.NeoLoadProxy.getName()).orNull());	
	}
	
	public String getNewRelicAPIKey() {
		return newRelicAPIKey;
	}
	
	public String getNewRelicApplicationName() {
		return newRelicApplicationName;
	}
	
	public String getDataExchangeApiUrl() {
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
}
