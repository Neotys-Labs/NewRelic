package com.neotys.newrelic.infrastucture;


import java.io.IOException;
import java.util.Timer;

import com.google.common.base.Optional;
import org.apache.http.client.ClientProtocolException;

import com.neotys.extensions.action.engine.Context;

import io.swagger.client.*;
import io.swagger.client.api.ResultsApi;


public class NewRelicPluginData {
	private final String NEOLOAD_WEB_BASEURL="https://neoload-api.saas.neotys.com/v1/";
	private final int MAXDURATION_TIME=2000;
	
	private String NewRElicLicenseKeyY;
	private ApiClient NeoLoadWEB_API_CLIENT;
	private Context NLContext;
	private ResultsApi NLWEBresult;
	private String TestID=null;
	private NeoLoadStatAggregator NLaggregator=null;
	private static String NL_TEST_RUNNING="RUNNING";
	private String projectname;
	private NLGlobalStat NLStat=null;
	private String Insight_Accountid=null;
	private String Insight_APIKey=null;
	private String TestName=null;
	static final int TIMERFREQUENCY=30000;
	private String NewRelicApplicationName;
	private String NewRelicApplicationAPIKEY;
	static final int TIMERDELAY=0;
	Timer timerNewRelic = null ;
	
	public NewRelicPluginData(String newRElicLicenseKeyY, Context pContext, String Insight_AccountID, String Insight_APIKEY, String ApplicationNAme, String ApplicationAPIKEY, final Optional<String> proxyName) throws NewRelicException, IOException {
		super();
		NewRElicLicenseKeyY = newRElicLicenseKeyY;
		Insight_Accountid=Insight_AccountID;
		Insight_APIKey=Insight_APIKEY;
		NLContext = pContext;

		// TODO handle proxy

		//----define  the NLWEB API-----
		NeoLoadWEB_API_CLIENT = new ApiClient();
		NeoLoadWEB_API_CLIENT.setApiKey(NLContext.getAccountToken());
		NeoLoadWEB_API_CLIENT.setBasePath(NEOLOAD_WEB_BASEURL);
		InitNLAPi();
		//-------------------------
		NLContext = pContext;
		
		NLStat=new NLGlobalStat();
		projectname=GetProjecName();
		TestName=GetTestName();
		NewRelicApplicationName=ApplicationNAme;
		NewRelicApplicationAPIKEY=ApplicationAPIKEY;
		if(TestID==null) {
			setTestID(GetTestID());
			NLStat=new NLGlobalStat();
			if(NLaggregator==null)
				NLaggregator=new NeoLoadStatAggregator(NewRElicLicenseKeyY, projectname,NLWEBresult,TestID,NLStat,Insight_Accountid,Insight_APIKey,TestName,NewRelicApplicationName,NewRelicApplicationAPIKEY,GetTestScenarioName());
		}
	}	
	
	private void setTestID(String pTestID)
	{
		TestID=pTestID;
	}
	
	public void SetProjectName(String ProjectName)
	{
		projectname=ProjectName;
	}
	
	
	
	public NewRelicPluginData(String newRElicLicenseKeyY,Context pContext,String strInsight_AccountID,String strInsight_APIKEY,String ApplicationName,String ApplicationAPIKEY) throws ClientProtocolException, NewRelicException, IOException {
		super();
		NewRElicLicenseKeyY = newRElicLicenseKeyY;
		Insight_Accountid=strInsight_AccountID;
		Insight_APIKey=strInsight_APIKEY;
		NLContext = pContext;
		//----define  the NLWEB API-----
		NeoLoadWEB_API_CLIENT = new ApiClient();
		NeoLoadWEB_API_CLIENT.setApiKey(NLContext.getAccountToken());
		NeoLoadWEB_API_CLIENT.setBasePath(NEOLOAD_WEB_BASEURL);
		NLStat=new NLGlobalStat();
		InitNLAPi();
		NLStat=new NLGlobalStat();
		projectname=GetProjecName();
		NewRelicApplicationName=ApplicationName;
		NewRelicApplicationAPIKEY=ApplicationAPIKEY;
		TestName=GetTestName();
		if(TestID==null) {
			setTestID(GetTestID());

			if(NLaggregator==null)
				NLaggregator=new NeoLoadStatAggregator(NewRElicLicenseKeyY, projectname,NLWEBresult,TestID,NLStat,Insight_Accountid,Insight_APIKey,TestName,NewRelicApplicationName,NewRelicApplicationAPIKEY,GetTestScenarioName());
		}
	}
	
	public void StartTimer()
	{
		timerNewRelic = new Timer();
		timerNewRelic.scheduleAtFixedRate(NLaggregator,TIMERDELAY,TIMERFREQUENCY);
	}

	public void StopTimer()
	{
		timerNewRelic.cancel();

	}
	public void resumeTimer() throws NewRelicException, IOException
	{
		timerNewRelic = new Timer();
		NLaggregator=new NeoLoadStatAggregator(NewRElicLicenseKeyY, projectname,NLWEBresult,TestID,NLStat,Insight_Accountid,Insight_APIKey,TestName,NewRelicApplicationName,NewRelicApplicationAPIKEY,GetTestScenarioName());
			
		timerNewRelic.scheduleAtFixedRate(NLaggregator,TIMERDELAY,TIMERFREQUENCY);
		
	}
	private void InitNLAPi()
	{
		NLWEBresult=new ResultsApi(NeoLoadWEB_API_CLIENT);
	}
	
	/*private boolean IsTimeCloseEnougth(long NLWebduration)
	{
		boolean result=false;
		
		long NLduration=NLContext.getElapsedTime();
		//---convert the test NLwebduration in milliseconds
		NLWebduration=NLWebduration*1000;
		if(NLduration-NLWebduration<MAXDURATION_TIME)
			result=true;
		
		return result;
	}*/
	 private String GetTestName()
	 {
		 String ProjectName;
		 return ProjectName=NLContext.getTestName();
	 }
	 private String GetTestScenarioName()
	 {
		 String ProjectName;
		 return ProjectName=NLContext.getScenarioName();
	 }
	 private String GetProjecName()
	 {
		 String ProjectName;
		 return ProjectName=NLContext.getProjectName();
	 }

	
	private String GetTestID() 
	{
		String TestID;
		TestID=NLContext.getTestId();
		return TestID;
		
	}
}
