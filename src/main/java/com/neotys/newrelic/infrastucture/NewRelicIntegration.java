package com.neotys.newrelic.infrastucture;

import com.google.common.base.Strings;
import com.neotys.newrelic.http.HTTPGenerator;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClient;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClientFactory;
import com.neotys.rest.dataexchange.model.ContextBuilder;
import com.neotys.rest.dataexchange.model.EntryBuilder;
import com.neotys.rest.error.NeotysAPIException;
import org.apache.http.client.ClientProtocolException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;

public class NewRelicIntegration {
	private DataExchangeAPIClient client;
	private ContextBuilder Context;
	private HTTPGenerator http;
	private static final String NewRelicURL="https://api.newrelic.com/v2/";
	private static final String NewRelicApplicationAPI="applications.json";
	private static final String NewRelicMetricNameAPI="/metrics.json";
	private static final String NewRelicHostAPI="/hosts.json";
	private static final String NewRelicMetricDataAPI="/metrics/data.json";
	private static final String NeoLoadLocation="NewRelic";
	private EntryBuilder entry;
	private String NewRElicAPIKEY;
	private String NewRelic_Application;
	private String PROXYHOST;
	private String PROXYPASS;
	private String PROXYUSER;
	private String PROXYPORT;
	private String NewRelicApplicationID;
	private long StartingTS;
	private HashMap<String,String> Header = null;
	private HashMap<String,String> Parameters=null;
	private static List<String> RelevantMetriNames = Arrays.asList("min", "max", "average","used_mb","percent");
	private static List<String> NonRelevantMetricname = Arrays.asList("Datastore/statement","Datastore/instance","CPU","Memory","Error/","connects");
	public NewRelicIntegration(String NewrelicAPIKEY, String NewRelicApplication, final String dataExchangeApiUrl, final com.google.common.base.Optional<String> dataExchangeApiKey, final com.neotys.extensions.action.engine.Context pContext, final com.google.common.base.Optional<String> proxyName, final long startTS)
	{
		StartingTS=startTS;
		Context = new ContextBuilder();
		Context.hardware("NewRelic").location(NeoLoadLocation).software("NewRelic")

			.script("NewRelicInfrasfructureMonitoring" + System.currentTimeMillis());
		NewRElicAPIKEY=NewrelicAPIKEY;
		NewRelic_Application=NewRelicApplication;

		//TODO handle proxy
	

		try {
			client = DataExchangeAPIClientFactory.newClient(dataExchangeApiUrl, Context.build(), dataExchangeApiKey.orNull());
			
		} catch (GeneralSecurityException | IOException | ODataException | URISyntaxException | NeotysAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public void StartMonitor(StringBuilder build) throws NewRelicException, IOException, JSONException, GeneralSecurityException, URISyntaxException, NeotysAPIException, ParseException
	{
		HashMap<String,String> Hosts;
		List<String> Metrics;
		
		InitHttpClien();
  	    NewRelicApplicationID=GetApplicationID();
		Hosts=GetApplicationsHosts();
		for (Entry<String, String> entry : Hosts.entrySet())
		{
			Metrics=GetMetricNameByHost(entry.getKey());
			for (int i = 0; i < Metrics.size(); i++) 
			{
				 build.append("Sending Metrics Data for Application" + NewRelic_Application+" on the host "+ entry.getValue()+ " metrics name "+ Metrics.get(i)+"\n");
				 SendMetricDataFromMetricsNameToNL(Metrics.get(i), entry.getKey(), NewRelic_Application, entry.getValue());
			}

		}

		
	}
	private boolean ISrelevantMetricName(String Metricname)
	{
		boolean result=false;
		  for(String listItem : NonRelevantMetricname){
			   if(Metricname.contains(listItem)){
			      return true;
			   }
			}
		return result;
	}
	@SuppressWarnings("null")
	public HashMap<String,String> GetApplicationsHosts() throws IOException
	{
		JSONObject jsoobj;
		String Url;
		JSONArray array;
		HashMap<String,String> Hostnames= null;
		Parameters=null;
		Url=NewRelicURL+"applications/"+NewRelicApplicationID+NewRelicHostAPI;

		if(! Strings.isNullOrEmpty(PROXYHOST)&&! Strings.isNullOrEmpty(PROXYPORT))
			http=new HTTPGenerator(Url, "GET",PROXYHOST,PROXYPORT,PROXYUSER,PROXYPASS, Header,Parameters );
		else
			http=new HTTPGenerator(Url, "GET", Header,Parameters );

		//----get the list of Hosts of the application-----------
	//		http.NewHttpRequest(Url, "GET", Header, Parameters);
		jsoobj=http.GetJSONHTTPresponse();
		if(jsoobj != null)
		{
			Hostnames=new HashMap<String,String>();
			array=jsoobj.getJSONArray("application_hosts");
			for(int j=0;j<array.length();j++)
				Hostnames.put(String.valueOf(array.getJSONObject(j).getInt("id")), array.getJSONObject(j).getString("host"));
		}
		//-------------------------------------------------------
		http.CloseHttpClient();
		return Hostnames;
	}


	private long GetTimeMillisFromDate(String date) throws ParseException
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss"); 
		formatter.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
		Date d = formatter.parse(date);
		long timestamp = d.getTime();
		
		return timestamp;	
	}
	private void CreateEntry(String ApplicationName,String HostName,String MetricName,String MetricValueName,double value,String unit,String ValueDate) throws GeneralSecurityException, IOException, URISyntaxException, NeotysAPIException, ParseException
	{
	  entry=new EntryBuilder(Arrays.asList("NewRelic", ApplicationName, HostName,MetricName,MetricValueName), Long.parseLong(ValueDate));
		entry.unit(unit);
		entry.value(value);
		client.addEntry(entry.build());
	}
  public List<String> GetMetricNameByHost(String HostID) throws ClientProtocolException, IOException
  {
	  JSONObject jsoobj;
	  JSONArray array;
	  List<String> MetriNames= new ArrayList<String>();
	  String Url=NewRelicURL+"applications/"+NewRelicApplicationID+"/hosts/"+HostID+NewRelicMetricNameAPI;
	  Parameters=null;

	  if(! Strings.isNullOrEmpty(PROXYHOST)&&! Strings.isNullOrEmpty(PROXYPORT))
		  http=new HTTPGenerator(Url, "GET",PROXYHOST,PROXYPORT,PROXYUSER,PROXYPASS, Header,Parameters );
	  else
		  http=new HTTPGenerator(Url, "GET", Header,Parameters );

	//  http.NewHttpRequest(Url, "GET", Header, Parameters);
		jsoobj=http.GetJSONHTTPresponse();
		if(jsoobj != null)
		{
			array = jsoobj.getJSONArray("metrics");
			for(int i = 0 ; i < array.length() ; i++)
			{
			  if(ISrelevantMetricName(array.getJSONObject(i).getString("name")))
				  MetriNames.add( array.getJSONObject(i).getString("name"));
			}
			
		}
	  http.CloseHttpClient();
		return MetriNames;
  }
  private String GetUTCDate()
  {
	  ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
	  utc=utc.minusSeconds(60);
	  return utc.format(DateTimeFormatter.ofPattern("yyyy-MM-dd+hh:mm:ss"));
  }
  private boolean IsMetricrelevant(String Metriname)
  {
	  boolean result= false;
	  for(String listItem : RelevantMetriNames){
		   if(Metriname.contains(listItem)){
		      return true;
		   }
		}
	  return result;
  }
  
  public void SendMetricDataFromMetricsNameToNL(String MetricName,String HostID,String Applicationname,String Hostname) throws ClientProtocolException, IOException, JSONException, GeneralSecurityException, URISyntaxException, NeotysAPIException, ParseException
  {
	  JSONObject jsoobj;
		String Url;
		JSONArray array;
		JSONArray timeslices;
		JSONObject values;
		JSONObject metric_data;
		String now=GetUTCDate();
		String MetricDate;
		String MetricValueName;
		long metricdate;
		
		 Url=NewRelicURL+"applications/"+NewRelicApplicationID+"/hosts/"+HostID+NewRelicMetricDataAPI;
		 Parameters= new HashMap<String,String>();
		 Parameters.put("names[]",MetricName);
		 Parameters.put("from", now);
		 Parameters.put("period", "1");
		 Parameters.put("summarize", "false");
		 Parameters.put("raw","true");

	  if(! Strings.isNullOrEmpty(PROXYHOST)&&! Strings.isNullOrEmpty(PROXYPORT))
		  http=new HTTPGenerator(Url, "GET",PROXYHOST,PROXYPORT,PROXYUSER,PROXYPASS, Header,Parameters );
	  else
		  http=new HTTPGenerator(Url, "GET", Header,Parameters );

	  //http.NewHttpRequest(Url, "GET", Header, Parameters);
			jsoobj=http.GetJSONHTTPresponse();
			if(jsoobj != null)
			{		
					if(jsoobj.has("metric_data"))
					{
						metric_data=jsoobj.getJSONObject("metric_data");
					
					
						array = metric_data.getJSONArray("metrics");
						for(int i = 0 ; i < array.length() ; i++)
						{
								timeslices=array.getJSONObject(i).getJSONArray("timeslices");
								for(int j=0;j<timeslices.length();j++)
								{
										MetricDate=timeslices.getJSONObject(j).getString("from");
										metricdate=GetTimeMillisFromDate(MetricDate);
										if(metricdate>=StartingTS)
										{
											MetricDate=String.valueOf(metricdate);
											values=timeslices.getJSONObject(j).getJSONObject("values");
											for(Object key : values.keySet())
											{
												MetricValueName=(String)key;
												if(IsMetricrelevant(MetricValueName))
														CreateEntry(Applicationname, Hostname, MetricName,MetricValueName, values.getDouble(MetricValueName), "", MetricDate);
											}
										}
								}
						}
					}
				
			}
	  http.CloseHttpClient();
  }
	public  String GetApplicationID() throws NewRelicException, IOException
	{
		JSONObject jsoobj;
		String Url;
		JSONArray jsonApplication;
		

		Url=NewRelicURL+NewRelicApplicationAPI;
		Parameters= new HashMap<String,String>();
		Parameters.put("filter[name]",NewRelic_Application);
		if(! Strings.isNullOrEmpty(PROXYHOST)&&! Strings.isNullOrEmpty(PROXYPORT))
			http=new HTTPGenerator(Url, "GET",PROXYHOST,PROXYPORT,PROXYUSER,PROXYPASS, Header,Parameters );
		else
			http=new HTTPGenerator(Url, "GET", Header,Parameters );
		
		
		jsoobj=http.GetJSONHTTPresponse();
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

		http.CloseHttpClient();
		return NewRelicApplicationID;
		
	}
	
	
	public NewRelicIntegration(String NewrelicAPIKEY, String NewRelicApplication,String NeoLoadAPIHost,String NeoLoadAPIport,String NeoLoadKeyAPI,  String HttpProxyHost,String HttpProxyPort,String HttpProxyUser,String HttpProxyPass, long startTS)
	{
		StartingTS=startTS;
		Context = new ContextBuilder();
		Context.hardware("NewRelic").location(NeoLoadLocation).software("NewRelic")

			.script("NewRelicInfrasfructureMonitoring" + System.currentTimeMillis());
		NewRElicAPIKEY=NewrelicAPIKEY;
		NewRelic_Application=NewRelicApplication;
		PROXYHOST=HttpProxyHost;
		PROXYPASS=HttpProxyPass;
		PROXYPORT=HttpProxyPort;
		PROXYUSER=HttpProxyUser;

		try {
			client = DataExchangeAPIClientFactory.newClient("http://"+NeoLoadAPIHost+":"+NeoLoadAPIport+"/DataExchange/v1/Service.svc/", Context.build(), NeoLoadKeyAPI);
			
		} catch (GeneralSecurityException | IOException | ODataException | URISyntaxException | NeotysAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void InitHttpClien()
	{
		Header= new HashMap<String,String>();
		Header.put("X-Api-Key", NewRElicAPIKEY);
		Header.put("Content-Type", "application/json");
		
		
	}
}

