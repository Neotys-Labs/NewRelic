package com.neotys.newrelic.infrastucture;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import com.neotys.newrelic.http.HTTPGenerator;

import io.swagger.client.ApiException;
import io.swagger.client.api.ResultsApi;
import io.swagger.client.model.ArrayOfElementDefinition;
import io.swagger.client.model.ElementDefinition;
import io.swagger.client.model.ElementValues;

import io.swagger.client.model.TestStatistics;

public class NeoLoadStatAggregator extends TimerTask{
	private HashMap<String,String> Header = null;
	private HTTPGenerator http;
	private String NewRElicLicenseKey;
	private String ComponentsName;
	private final String NEW_RELIC_PLUG_URL="https://platform-api.newrelic.com/platform/v1/metrics";
	private String NEW_RELIC_INSIGHT_URL="https://insights-collector.newrelic.com/v1/accounts/";
	private final String NLGUID="com.neotys.NeoLoad.plugin";
	NLGlobalStat NLStat;
	private String VERSION="1.0.0";
	private String Insight_APIKEY;
	private String Insight_AccountID;
	private String TestName;
	private final String TestID;
	private final int BAD_REQUEST=400;
	private final int UNAUTHORIZED=403;
	private final int NOT_FOUND=404;
	private final int METHOD_NOT_ALLOWED=405;
	private final int REQUEST_ENTITY_TOO_LARGE=413;
	private final int INTERNAL_SERVER_ERROR=500;
	private final int BAD_GATEWAY=502;
	private final int SERVICE_UNAVAIBLE=503;
	private final int GATEWAY_TIMEOUT=504;
	private final int HTTP_RESPONSE=200;
	private static int MIN_NEW_RELIC_DURATION=30;
	private static final String NewRelicURL="https://api.newrelic.com/v2/";
	private static final String NewRelicApplicationAPI="applications.json";
	private String NewRelicAPplicationID;
	private String NLScenarioName;
	private String ApplicationNAme;
	ResultsApi NLWEBresult;
	private static final String NLWEB_TRANSACTION="TRANSACTION";
	private static final String NLWEB_PAGE="PAGE";
	private static final String NLWEB_REQUEST="REQUEST";
		
	
	private void InitHttpClient()
	{
		Header= new HashMap<String,String>();
		Header.put("X-License-Key", NewRElicLicenseKey);
		Header.put("Content-Type", "application/json");
		Header.put("Accept","application/json");
		
	}
	public NeoLoadStatAggregator(String pNewRElicLicenseKeyY,String pĈomponentName,ResultsApi pNLWEBresult,String pTestID,NLGlobalStat pNLStat,String I_AccountID,String I_APIKEY,String Testname,String ApplicationName, String ApIKEY,String ScenarioName) throws NewRelicException, IOException
	{
		ComponentsName="Statistics";
		NewRElicLicenseKey=pNewRElicLicenseKeyY;
		NLStat=pNLStat;
		TestID=pTestID;
		NLWEBresult=pNLWEBresult;
		NewRelicAPplicationID=GetApplicationID(ApplicationName,ApIKEY);
		NLScenarioName=ScenarioName;
		TestName=Testname;
		Insight_AccountID=I_AccountID;
		Insight_APIKEY=I_APIKEY;
		ApplicationNAme=ApplicationName;
		InitHttpClient();
	}
	private void SendStatsToNewRelic() throws ApiException
	{
		TestStatistics StatsResult;
		long utc;
		long lasduration;
		
		utc=System.currentTimeMillis()/1000;
		
		lasduration=NLStat.getLasduration();
		
		if(lasduration==0 || (utc-lasduration)>=MIN_NEW_RELIC_DURATION)
		{
		
				
			StatsResult=NLWEBresult.getTestStatistics(TestID);
			
			
			lasduration=SendData(StatsResult,lasduration);
			NLStat.setLasduration(lasduration);
		
			SendValuesToNewRelic();
		}
	}
	
	public long SendData(TestStatistics stat,long LasDuration) 
	{
		int time = 0;
		List<String[]> data;
		long utc;
		utc=System.currentTimeMillis()/1000;
		
		if(NLStat==null)
			NLStat=new NLGlobalStat(stat);
		else
		{
			NLStat.UpdateStat(stat);
		}
		data=NLStat.GetNLData();
		if(LasDuration==0)
			time=0;
		else
		{
			time=(int) (utc-LasDuration);
		}
		for(String[] metric : data)
		{
			try {
				SendMetricToPluginAPi(metric[0], metric[1], time, metric[2], metric[3]);
				
			} catch (NewRelicException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {

		SendMetricToInsightAPI(data,time);
		} catch (NewRelicException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return utc;
	}
	
	public  String GetApplicationID(String ApplicaitoNAme,String APIKEY) throws NewRelicException, IOException
	{
		JSONObject jsoobj;
		String Url;
		JSONArray jsonApplication;
		HashMap<String,String> Parameters = null;
		HashMap<String,String> head = null;
		
		HTTPGenerator ApplicationAPI; 
		String NewRelicApplicationID=null;
		
		head= new HashMap<String,String>();
		head.put("X-Api-Key", APIKEY);
		head.put("Content-Type", "application/json");
		Url=NewRelicURL+NewRelicApplicationAPI;
		Parameters= new HashMap<String,String>();
		Parameters.put("filter[name]",ApplicaitoNAme);
		
		ApplicationAPI=new HTTPGenerator(Url, "GET", head,Parameters );
		
		
		jsoobj=ApplicationAPI.GetJSONHTTPresponse();
		if(jsoobj != null)
		{
			if(jsoobj.has("applications"))
			{
				jsonApplication=jsoobj.getJSONArray("applications");
				NewRelicApplicationID=String.valueOf(jsonApplication.getJSONObject(0).getInt("id"));
				if(NewRelicApplicationID ==null)
					throw new NewRelicException("No Application find in The NewRelic Account");
			}
			else
				NewRelicApplicationID=null;
		}
		else
			NewRelicApplicationID=null;
		
		return NewRelicApplicationID;
		
	}
	private void SendValuesToNewRelic() throws ApiException
	{
		ArrayOfElementDefinition NLElement;
		ElementValues Values;
		List<NLGlobalValues> NlValues = null ;
		NlValues=new  ArrayList<NLGlobalValues>();
		try
		{
		
				
			NLElement=NLWEBresult.getTestElements(TestID, NLWEB_TRANSACTION);
			for(ElementDefinition element : NLElement)
			{	
				if(element.getType().equalsIgnoreCase("TRANSACTION"))
				{
					Values=NLWEBresult.getTestElementsValues(TestID,element.getId());
					NlValues.add(new NLGlobalValues(element,Values));
				}
			}
			
			for(NLGlobalValues val:NlValues)
				SendValueMetricToInsightAPI(val.GetElementValue());
				
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	private void SendValueMetricToInsightAPI(String[] data) throws NewRelicException
	{
		int httpcode;
		HashMap<String,String> head = null;
		HTTPGenerator Insight_HTTP; 
		
		head= new HashMap<String,String>();
		head.put("X-Insert-Key", Insight_APIKEY);
		head.put("Content-Type", "application/json");
		String URL;
		long ltime=System.currentTimeMillis();
		
		URL=NEW_RELIC_INSIGHT_URL+Insight_AccountID+"/events";
		String Exceptionmessage=null;
			
		String JSON_String="[{\"eventType\":\"NeoLoadValues\","
					 + "\"account\" : \""+Insight_AccountID+"\","
					 + "\"appId\" : \""+NewRelicAPplicationID+"\","
 					 + "\"testName\" : \""+TestName+"\","
 					 + "\"scenarioName\" : \""+NLScenarioName+"\","
					+ "\"applicationName\" : \"" + ApplicationNAme+"\","
					+ "\"trendfield\": \""+ApplicationNAme+NLScenarioName+TestName+"\","
					+ "\"userPathName\" :\""+data[1]+"\","
					+ "\"type\" :\""+data[6]+"\","
					+ "\"transactionName\" :\""+data[0]+"\","
					+ "\"path\" :\""+data[2]+"\","
					+ "\"responseTime\":"+data[3]+","
					+ "\"elementPerSecond\":"+data[5]+","
					+ "\"downloadedBytesPerSecond\":"+data[4]	+","	
					+ "\"timestamp\" : "+ltime+"}]";
		
	
		Insight_HTTP=new HTTPGenerator(URL,  head,JSON_String );
		try {
			httpcode=Insight_HTTP.GetHttpResponseCodeFromResponse();
			switch(httpcode)
			{
				
				  case BAD_REQUEST :
					  Exceptionmessage="The request or headers are in the wrong format, or the URL is incorrect, or the GUID does not meet the validation requirements.";
					  break;
				  case UNAUTHORIZED :
					  Exceptionmessage="Authentication error (no license key header, or invalid license key).";
					  break;
				  case NOT_FOUND :
					  Exceptionmessage="Invalid URL.";
					  break;
				  case METHOD_NOT_ALLOWED :
					  Exceptionmessage="Returned if the method is an invalid or unexpected type (GET/POST/PUT/etc.).";
					  break;
				  case REQUEST_ENTITY_TOO_LARGE :
					  Exceptionmessage="Too many metrics were sent in one request, or too many components (instances) were specified in one request, or other single-request limits were reached.";
					  break;
				  case INTERNAL_SERVER_ERROR :
					  Exceptionmessage="Unexpected server error";
					  break;
				  case BAD_GATEWAY :
					  Exceptionmessage="All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					  break;
				  case SERVICE_UNAVAIBLE :
					  Exceptionmessage="All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					  break;
				  case GATEWAY_TIMEOUT :
					  Exceptionmessage="All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					  break;
				
			}
		    if(Exceptionmessage!=null)
		    	throw new NewRelicException(Exceptionmessage);
				 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Insight_HTTP.CloseHttpClient();

	}
	private void SendMetricToInsightAPI(List<String[]> data, int time) throws NewRelicException
	{
		int httpcode;
		HashMap<String,String> head = null;
		HTTPGenerator Insight_HTTP; 
		
		head= new HashMap<String,String>();
		head.put("X-Insert-Key", Insight_APIKEY);
		head.put("Content-Type", "application/json");
		String URL;
		long ltime=System.currentTimeMillis();
		
		URL=NEW_RELIC_INSIGHT_URL+Insight_AccountID+"/events";
		String Exceptionmessage=null;
			
		String JSON_String="[{\"eventType\":\"NeoLoadData\","
					 + "\"account\" : \""+Insight_AccountID+"\","
					 + "\"appId\" : \""+NewRelicAPplicationID+"\","
 					 + "\"testName\" : \""+TestName+"\","
 					 + "\"scenarioName\" : \""+NLScenarioName+"\","
					+ "\"applicationName\" : \"" + ApplicationNAme+"\","
					+ "\"trendfield\": \""+ApplicationNAme+NLScenarioName+TestName+"\",";
					
		  	for(String[] metric :  data)
			{
				JSON_String+="\""+metric[1]+"\" : "+ metric[3]+",";
		 				
			}
		  	JSON_String+=  "\"MetricUnit\" : \"\","
 					   + "\"timestamp\" : "+ltime+"}]";
		
		 
		Insight_HTTP=new HTTPGenerator(URL,  head,JSON_String );
		try {
			httpcode=Insight_HTTP.GetHttpResponseCodeFromResponse();
			switch(httpcode)
			{
				
				  case BAD_REQUEST :
					  Exceptionmessage="The request or headers are in the wrong format, or the URL is incorrect, or the GUID does not meet the validation requirements.";
					  break;
				  case UNAUTHORIZED :
					  Exceptionmessage="Authentication error (no license key header, or invalid license key).";
					  break;
				  case NOT_FOUND :
					  Exceptionmessage="Invalid URL.";
					  break;
				  case METHOD_NOT_ALLOWED :
					  Exceptionmessage="Returned if the method is an invalid or unexpected type (GET/POST/PUT/etc.).";
					  break;
				  case REQUEST_ENTITY_TOO_LARGE :
					  Exceptionmessage="Too many metrics were sent in one request, or too many components (instances) were specified in one request, or other single-request limits were reached.";
					  break;
				  case INTERNAL_SERVER_ERROR :
					  Exceptionmessage="Unexpected server error";
					  break;
				  case BAD_GATEWAY :
					  Exceptionmessage="All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					  break;
				  case SERVICE_UNAVAIBLE :
					  Exceptionmessage="All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					  break;
				  case GATEWAY_TIMEOUT :
					  Exceptionmessage="All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					  break;
				
			}
		    if(Exceptionmessage!=null)
		    	throw new NewRelicException(Exceptionmessage);
				 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Insight_HTTP.CloseHttpClient();

	}
	private void SendMetricToPluginAPi(String MetricName,String MetricPath,int Duration,String unit,String Value) throws NewRelicException
	{
		int httpcode;
		String Exceptionmessage=null;
		
		String JSON_String="{\"agent\":{"
					 + "\"host\" : \""+NLGUID+"\","
					 + "\"version\" : \""+VERSION+"\""
		      +"},"
		      +"\"components\": ["
		       + "{"
		         + "\"name\": \"NeoLoad\","
		          +"\"guid\": \""+NLGUID+"\","
		         +" \"duration\" : "+String.valueOf(Duration)+","
		         +" \"metrics\" : {"
		           +" \"Component/NeoLoad/"+ComponentsName+"/"+MetricPath+"["+unit+"]\": "+Value+""
		          +"}"
		       + "}"
		      +"]"
		    +"}";
		
			
		http=new HTTPGenerator(NEW_RELIC_PLUG_URL,  Header,JSON_String );
		try {
			httpcode=http.GetHttpResponseCodeFromResponse();
			switch(httpcode)
			{
				
				  case BAD_REQUEST :
					  Exceptionmessage="The request or headers are in the wrong format, or the URL is incorrect, or the GUID does not meet the validation requirements.";
					  break;
				  case UNAUTHORIZED :
					  Exceptionmessage="Authentication error (no license key header, or invalid license key).";
					  break;
				  case NOT_FOUND :
					  Exceptionmessage="Invalid URL.";
					  break;
				  case METHOD_NOT_ALLOWED :
					  Exceptionmessage="Returned if the method is an invalid or unexpected type (GET/POST/PUT/etc.).";
					  break;
				  case REQUEST_ENTITY_TOO_LARGE :
					  Exceptionmessage="Too many metrics were sent in one request, or too many components (instances) were specified in one request, or other single-request limits were reached.";
					  break;
				  case INTERNAL_SERVER_ERROR :
					  Exceptionmessage="Unexpected server error";
					  break;
				  case BAD_GATEWAY :
					  Exceptionmessage="All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					  break;
				  case SERVICE_UNAVAIBLE :
					  Exceptionmessage="All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					  break;
				  case GATEWAY_TIMEOUT :
					  Exceptionmessage="All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					  break;
				
			}
		    if(Exceptionmessage!=null)
		    	throw new NewRelicException(Exceptionmessage);
				 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		http.CloseHttpClient();


	}
	@Override
	 public void run() {
		 try {
			SendStatsToNewRelic();
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
}
