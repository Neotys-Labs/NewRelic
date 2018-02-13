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
import com.neotys.newrelic.NewRelicActionArguments;
import com.neotys.newrelic.NewRelicOption;
import com.neotys.rest.error.NeotysAPIException;

public final class NewRelicActionEngine implements ActionEngine {
	private NewRelicPluginData pluginData;	
	
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
		long startTimestamp=0;
			
		final NewRelicIntegration newrelic;
						
		if(newRelicActionArguments.isSendNLWebDataToNewRelic())
		{
			pluginData =(NewRelicPluginData)context.getCurrentVirtualUser().get("PLUGINDATA");
			
			if(pluginData == null){			

				try {
					pluginData=new NewRelicPluginData(context, newRelicActionArguments);
					context.getCurrentVirtualUser().put("PLUGINDATA",pluginData);	
				} catch (NewRelicException | IOException | NoSuchAlgorithmException | KeyManagementException e) {
					// TODO Auto-generated catch block
					return getErrorResult(context, sampleResult, "Technical Error PLugin/Insight API:", e);				
				}
			} 
		}
		
		try {			
			sampleResult.sampleStart();
			startTimestamp=System.currentTimeMillis()-context.getElapsedTime();
			appendLineToStringBuilder(requestBuilder, "NewRelicInfraStructureMonitoring request.");
			
			newrelic = new NewRelicIntegration(context, newRelicActionArguments, startTimestamp);		
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
