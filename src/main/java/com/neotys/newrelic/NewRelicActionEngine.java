package com.neotys.newrelic;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.neotys.action.result.ResultFactory;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.SampleResult;
import com.neotys.newrelic.fromnlweb.NLWebToNewRelic;
import com.neotys.newrelic.tonldataexchange.NewRelicToNLDataExchange;

public final class NewRelicActionEngine implements ActionEngine {
	private NLWebToNewRelic pluginData;	
	
	@Override
	public SampleResult execute(Context context, List<ActionParameter> parameters) {
		final SampleResult sampleResult = new SampleResult();
		final StringBuilder requestBuilder = new StringBuilder();
		final StringBuilder responseBuilder = new StringBuilder();
		
		final NewRelicActionArguments newRelicActionArguments;
		try {
			newRelicActionArguments = new NewRelicActionArguments(context, parameters);
		} catch (final IllegalArgumentException iae) {
			return ResultFactory.newErrorResult(context, Constants.STATUS_CODE_INVALID_PARAMETER, "Could not parse arguments: ", iae);
		}		
			
		final NewRelicToNLDataExchange newrelic;
						
		if(newRelicActionArguments.isSendNLWebDataToNewRelic())
		{
			pluginData =(NLWebToNewRelic)context.getCurrentVirtualUser().get("PLUGINDATA");
			
			if(pluginData == null){			

				try {
					pluginData=new NLWebToNewRelic(context, newRelicActionArguments);
					context.getCurrentVirtualUser().put("PLUGINDATA",pluginData);	
				} catch (NewRelicException | IOException | NoSuchAlgorithmException | KeyManagementException e) {
					// TODO Auto-generated catch block
					return getErrorResult(context, sampleResult, "Technical Error PLugin/Insight API:", e);				
				}
			} 
		}
		
		try {			
			sampleResult.sampleStart();	
			requestBuilder.append("NewRelicInfraStructureMonitoring request.\n");			
			newrelic = new NewRelicToNLDataExchange(context, newRelicActionArguments, System.currentTimeMillis()-context.getElapsedTime());		
			newrelic.startMonitor(responseBuilder);
		} catch (final Exception e) {
			// TODO 
			e.printStackTrace();
		}
		sampleResult.sampleEnd();
		sampleResult.setRequestContent(requestBuilder.toString());
		sampleResult.setResponseContent(responseBuilder.toString());		
		return sampleResult;
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
