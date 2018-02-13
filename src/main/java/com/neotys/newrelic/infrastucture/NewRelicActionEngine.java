package com.neotys.newrelic.infrastucture;

import static com.neotys.action.argument.Arguments.getArgumentLogString;
import static com.neotys.action.argument.Arguments.parseArguments;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import com.google.common.base.Optional;
import com.neotys.action.result.ResultFactory;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Logger;
import com.neotys.extensions.action.engine.SampleResult;
import com.neotys.newrelic.Constants;
import com.neotys.rest.error.NeotysAPIException;

public final class NewRelicActionEngine implements ActionEngine {
	private NewRelicPluginData pluginData;	
	
	@Override
	public SampleResult execute(Context context, List<ActionParameter> parameters) {
		final SampleResult sampleResult = new SampleResult();
		final StringBuilder requestBuilder = new StringBuilder();
		final StringBuilder responseBuilder = new StringBuilder();
		long Start_TS=0;

		final Map<String, Optional<String>> parsedArgs;
		try {
			parsedArgs = parseArguments(parameters, NewRelicOption.values());
		} catch (final IllegalArgumentException iae) {
			return ResultFactory.newErrorResult(context, Constants.STATUS_CODE_INVALID_PARAMETER, "Could not parse arguments: ", iae);
		}

		if (context.getWebPlatformRunningTestUrl() == null) {
			return ResultFactory.newErrorResult(context, Constants.STATUS_CODE_BAD_CONTEXT, "Bad context: ", new NewRelicException("No NeoLoad Web test is running"));
		}

		final Logger logger = context.getLogger();
		if (logger.isDebugEnabled()) {
			logger.debug("Executing " + this.getClass().getName() + " with parameters: "
					+ getArgumentLogString(parsedArgs, NewRelicOption.values()));
		}
		
		NewRelicIntegration newrelic;
				
		final String newRelicApiKey = parsedArgs.get(NewRelicOption.NewRelicApiKey.getName()).get();
		final String newRelicApplicationName = parsedArgs.get(NewRelicOption.NewRelicApplicationName.getName()).get();

		final boolean isNewRelicPluginEnabled = Boolean.parseBoolean(parsedArgs.get(NewRelicOption.EnableNewRelicPlugin.getName()).get());
		final Optional<String> licenseKey = parsedArgs.get(NewRelicOption.NewRelicLicenseKey.getName());
		final Optional<String> insightAccountId = parsedArgs.get(NewRelicOption.InsightAccountId.getName());
		final Optional<String> insightApiKey = parsedArgs.get(NewRelicOption.InsightApiKey.getName());

		final String dataExchangeApiUrl = parsedArgs.get(NewRelicOption.NeoLoadDataExchangeApiUrl.getName()).get();
		final Optional<String> dataExchangeApiKey = parsedArgs.get(NewRelicOption.NeoLoadDataExchangeApiKey.getName());
		final Optional<String> proxyName = parsedArgs.get(NewRelicOption.NeoLoadProxy.getName());
		
		if(isNewRelicPluginEnabled)
		{
			pluginData =(NewRelicPluginData)context.getCurrentVirtualUser().get("PLUGINDATA");
			
			if(pluginData == null){
				if (!licenseKey.isPresent() || licenseKey.get().equals("")) {
					return ResultFactory.newErrorResult(context, Constants.STATUS_CODE_INVALID_PARAMETER, "Invalid argument: " + NewRelicOption.NewRelicLicenseKey.getName() + " cannot null if the NewRelic Plugin is enabled");
				}
				if (!insightAccountId.isPresent() || insightAccountId.get().equals("")) {
					return ResultFactory.newErrorResult(context, Constants.STATUS_CODE_INVALID_PARAMETER, "Invalid argument: " + NewRelicOption.InsightAccountId.getName() + " cannot null if the NewRelic Plugin is enabled");
				}
				if (!insightApiKey.isPresent() || insightApiKey.get().equals("")) {
					return ResultFactory.newErrorResult(context, Constants.STATUS_CODE_INVALID_PARAMETER, "Invalid argument: " + NewRelicOption.InsightApiKey.getName() + " cannot null if the NewRelic Plugin is enabled");
				}

				try {
					pluginData=new NewRelicPluginData(licenseKey.get(), context, insightAccountId.get(), insightApiKey.get(), newRelicApiKey, newRelicApplicationName, proxyName);
					context.getCurrentVirtualUser().put("PLUGINDATA",pluginData);	
				} catch (NewRelicException | IOException | NoSuchAlgorithmException | KeyManagementException e) {
					// TODO Auto-generated catch block
					return getErrorResult(context, sampleResult, "Technical Error PLugin/Insight API:", e);				
				}
			} 
		}
		
		try {			
			sampleResult.sampleStart();
			Start_TS=System.currentTimeMillis()-context.getElapsedTime();
			appendLineToStringBuilder(requestBuilder, "NewRelicInfraStructureMonitoring request.");
			
			newrelic = new NewRelicIntegration(newRelicApiKey, newRelicApplicationName, dataExchangeApiUrl, dataExchangeApiKey, context,  proxyName, Start_TS);		
			newrelic.startMonitor(responseBuilder);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NewRelicException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NeotysAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sampleResult.sampleEnd();
		sampleResult.setRequestContent(requestBuilder.toString());
		sampleResult.setResponseContent(responseBuilder.toString());		
		return sampleResult;
	}

	private static void appendLineToStringBuilder(final StringBuilder sb, final String line){
		sb.append(line).append("\n");
	}

	/**
	 * This method allows to easily create an error result and log exception.
	 */
	private static SampleResult getErrorResult(final Context context, final SampleResult result, final String errorMessage, final Exception exception) {
		result.setError(true);
		result.setStatusCode("NL-NewRelicInfraStructureMonitoring_ERROR");
		result.setResponseContent(errorMessage);
		if(exception != null){
			context.getLogger().error(errorMessage, exception);
		} else{
			context.getLogger().error(errorMessage);
		}
		return result;
	}

	@Override
	public void stopExecute() {
		// TODO add code executed when the test have to stop.
	}

}
