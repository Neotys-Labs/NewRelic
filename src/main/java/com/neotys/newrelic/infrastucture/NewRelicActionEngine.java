package com.neotys.newrelic.infrastucture;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import com.google.common.base.Strings;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.SampleResult;
import com.neotys.rest.error.NeotysAPIException;


public final class NewRelicActionEngine implements ActionEngine {
	private String NeoLoadAPIHost;
	private String NeoLoadAPIport;
	private String NeoLoadKeyAPI;
	private String NewRelic_APIKEY;
	private String NewRelic_Application;
	private String PROXYHOST;
	private String PROXYPASS;
	private String PROXYUSER;
	private String PROXYPORT;
	private String NewRelic_License_Key;
	private String PLUGIN_ENABLED_VALUE;
	private String ProjectName;
	private String Insight_APIKEY;
	private String Insight_AccountID;
	private String NLScenarioName;
	NewRelicPluginData pluginData ;
	
	private boolean IsNewRelicEnabled=false;
	
	private static final String NewRelicMetricDataAPI="/metrics/data.json";
	
	@Override
	public SampleResult execute(Context context, List<ActionParameter> parameters) {
		final SampleResult sampleResult = new SampleResult();
		final StringBuilder requestBuilder = new StringBuilder();
		final StringBuilder responseBuilder = new StringBuilder();
		long Start_TS=0;
		
		NewRelicIntegration newrelic;
		
		boolean PluginStored=false;
		
		for(ActionParameter parameter:parameters) {
			switch(parameter.getName()) 
			{
			case  NewRelicAction.NeoLoadAPIHost:
				NeoLoadAPIHost = parameter.getValue();
				break;
			case  NewRelicAction.NeoLoadAPIport:
				NeoLoadAPIport = parameter.getValue();
				break;
			case  NewRelicAction.NeoLoadKeyAPI:
				NeoLoadKeyAPI = parameter.getValue();
				break;
			
			case  NewRelicAction.NewRelic_APIKEY:
				NewRelic_APIKEY = parameter.getValue();
				break;
			case  NewRelicAction.NewRelic_ApplicationName:
				NewRelic_Application = parameter.getValue();
				break;
			case  NewRelicAction.HTTP_PROXY_HOST:
				PROXYHOST = parameter.getValue();
				break;
			case  NewRelicAction.HTTP_PROXY_PASSWORD:
				PROXYPASS = parameter.getValue();
				break;
			case  NewRelicAction.HTTP_PROXY_LOGIN:
				PROXYUSER = parameter.getValue();
				break;
			case  NewRelicAction.HTTP_PROXY_PORT:
				PROXYPORT = parameter.getValue();
				break;
			case  NewRelicAction.NewRelic_License_Key:
				NewRelic_License_Key = parameter.getValue();
				break;
			case  NewRelicAction.ENABLE_NEWRELIC_PLUGIN:
				PLUGIN_ENABLED_VALUE = parameter.getValue();
				break;
			
			case  NewRelicAction.Insight_ApiKey:
				Insight_APIKEY = parameter.getValue();
				break;
			case  NewRelicAction.Insight_AccountID:
				Insight_AccountID = parameter.getValue();
					break;
			}
		}
		
		if (Strings.isNullOrEmpty(NeoLoadAPIHost)) {
			return getErrorResult(context, sampleResult, "Invalid argument: NeoLoadAPIHost cannot be null "
					+ NewRelicAction.NeoLoadAPIHost + ".", null);
		}
		if (Strings.isNullOrEmpty(NeoLoadAPIport)) {
			return getErrorResult(context, sampleResult, "Invalid argument: NeoLoadAPIport cannot be null "
					+ NewRelicAction.NeoLoadAPIport + ".", null);
		}
		else
		{
			try
			{
				int test= Integer.parseInt(NeoLoadAPIport);
			}
			catch(Exception e)
			{
				return getErrorResult(context, sampleResult, "Invalid argument: NeoLoadAPIport needs to be an Integer "
						+ NewRelicAction.NeoLoadAPIport + ".", null);
			}
			
		}
		
		if (Strings.isNullOrEmpty(NewRelic_APIKEY)) {
			return getErrorResult(context, sampleResult, "Invalid argument: NewRelic_APIKEY cannot be null "
					+ NewRelicAction.NewRelic_APIKEY + ".", null);
		}
		if (Strings.isNullOrEmpty(NewRelic_Application)) {
			return getErrorResult(context, sampleResult, "Invalid argument: NewRelic_Application cannot be null "
					+ NewRelicAction.NewRelic_ApplicationName + ".", null);
		}
		if (! Strings.isNullOrEmpty(PROXYHOST) ) {
			if(Strings.isNullOrEmpty(PROXYPORT))
				return getErrorResult(context, sampleResult, "Invalid argument: PROXYPORT cannot be null if you specify a Proxy Host"
						+ NewRelicAction.HTTP_PROXY_PORT + ".", null);
		}
		if (! Strings.isNullOrEmpty(PROXYPORT) ) {
			if(Strings.isNullOrEmpty(PROXYHOST))
				return getErrorResult(context, sampleResult, "Invalid argument: PROXYHOST cannot be null if you specify a Proxy Host"
						+ NewRelicAction.HTTP_PROXY_HOST + ".", null	);
			
						
	
		}
		
		if (Strings.isNullOrEmpty(PLUGIN_ENABLED_VALUE) ) 
			IsNewRelicEnabled=false;
		else
		{
			if(PLUGIN_ENABLED_VALUE.equalsIgnoreCase("true"))
				IsNewRelicEnabled=true;
		}
		
		if(IsNewRelicEnabled)
		{
			pluginData =(NewRelicPluginData)context.getCurrentVirtualUser().get("PLUGINDATA");
			
			if(pluginData == null){
				if(Strings.isNullOrEmpty(NewRelic_License_Key))
					return getErrorResult(context, sampleResult, "Invalid argument: NewRelic_License_Key cannot be null if the NewRelic Plugin is enabled"
							+ NewRelicAction.NewRelic_License_Key + ".", null);
			
				if(Strings.isNullOrEmpty(Insight_AccountID))
					return getErrorResult(context, sampleResult, "Invalid argument: Insight_AccountID cannot be null if the NewRelic Plugin is enabled"
							+ NewRelicAction.Insight_AccountID + ".", null);
				if(Strings.isNullOrEmpty(Insight_APIKEY))
					return getErrorResult(context, sampleResult, "Invalid argument: Insight_ApiKey cannot be null if the NewRelic Plugin is enabled"
							+ NewRelicAction.Insight_ApiKey + ".", null);
				
			    // Delay by two seconds to ensure no conflicts in re-establishing connection
				try {
					if(!Strings.isNullOrEmpty(PROXYPORT))
					{
						pluginData=new NewRelicPluginData(NewRelic_License_Key,PROXYHOST,PROXYPORT,PROXYUSER,PROXYPASS,context,Insight_AccountID, Insight_APIKEY,NewRelic_Application,NewRelic_APIKEY);
					}
					else
					{
						pluginData=new NewRelicPluginData(NewRelic_License_Key,context,Insight_AccountID, Insight_APIKEY,NewRelic_Application,NewRelic_APIKEY);
					}
				} catch (NewRelicException | IOException e) {
					// TODO Auto-generated catch block
					return getErrorResult(context, sampleResult, "Technical Error PLugin/Insight API:", e);
				
				}
			} else{
				PluginStored=true;
			}
			
		//	pluginData.StartTimer();

		}
		
				
		
		
		try {
			if(IsNewRelicEnabled)
			{
				if(!PluginStored)
					pluginData.StartTimer();
				else
					pluginData.resumeTimer();
			}
			sampleResult.sampleStart();
			Start_TS=System.currentTimeMillis()-context.getElapsedTime();
			appendLineToStringBuilder(requestBuilder, "NewRelicInfraStructureMonitoring request.");
			
			if(!Strings.isNullOrEmpty(PROXYPORT))
				newrelic = new NewRelicIntegration(NewRelic_APIKEY, NewRelic_Application, NeoLoadAPIHost, NeoLoadAPIport, NeoLoadKeyAPI,  PROXYHOST, PROXYPORT, PROXYUSER, PROXYPASS,Start_TS);
			else
				newrelic = new NewRelicIntegration(NewRelic_APIKEY, NewRelic_Application, NeoLoadAPIHost, NeoLoadAPIport, NeoLoadKeyAPI, Start_TS);
			
			
			newrelic.StartMonitor(responseBuilder);
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
		
		// TODO perform execution.

		sampleResult.sampleEnd();

		sampleResult.setRequestContent(requestBuilder.toString());
		sampleResult.setResponseContent(responseBuilder.toString());
		if(IsNewRelicEnabled)
		{
			pluginData.StopTimer();
			if(!PluginStored)
				context.getCurrentVirtualUser().put("PLUGINDATA",pluginData);
			
		}
		
		
		return sampleResult;
	}

	private void appendLineToStringBuilder(final StringBuilder sb, final String line){
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
