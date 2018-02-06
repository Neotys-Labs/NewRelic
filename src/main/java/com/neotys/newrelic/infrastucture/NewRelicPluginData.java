package com.neotys.newrelic.infrastucture;


import java.io.IOException;
import java.util.Timer;

import com.google.common.base.Optional;

import com.neotys.extensions.action.engine.Context;

import io.swagger.client.*;
import io.swagger.client.api.ResultsApi;


public class NewRelicPluginData {
	private final int MAXDURATION_TIME=2000;
	
	private String NewRElicLicenseKeyY;
	private ApiClient neoloadWebApiClient;
	private Context neoloadContext;
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
	
	public NewRelicPluginData(String newRElicLicenseKeyY, final Context neoloadContext, String Insight_AccountID, String Insight_APIKEY, String ApplicationNAme, String ApplicationAPIKEY, final Optional<String> proxyName) throws NewRelicException, IOException {
		super();
		NewRElicLicenseKeyY = newRElicLicenseKeyY;
		Insight_Accountid=Insight_AccountID;
		Insight_APIKey=Insight_APIKEY;
		this.neoloadContext = neoloadContext;

		// TODO handle proxy

		//----define  the NLWEB API-----
		this.neoloadWebApiClient = new ApiClient();
		neoloadWebApiClient.setApiKey(neoloadContext.getAccountToken());
		neoloadWebApiClient.setBasePath(neoloadContext.getWebPlatformApiUrl());
		InitNLAPi();
		//-------------------------
		
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
		NLWEBresult=new ResultsApi(neoloadWebApiClient);
	}
	
	/*private boolean IsTimeCloseEnougth(long NLWebduration)
	{
		boolean result=false;
		
		long NLduration=neoloadContext.getElapsedTime();
		//---convert the test NLwebduration in milliseconds
		NLWebduration=NLWebduration*1000;
		if(NLduration-NLWebduration<MAXDURATION_TIME)
			result=true;
		
		return result;
	}*/
	 private String GetTestName()
	 {
		 String ProjectName;
		 return ProjectName= neoloadContext.getTestName();
	 }
	 private String GetTestScenarioName()
	 {
		 String ProjectName;
		 return ProjectName= neoloadContext.getScenarioName();
	 }
	 private String GetProjecName()
	 {
		 String ProjectName;
		 return ProjectName= neoloadContext.getProjectName();
	 }

	
	private String GetTestID() 
	{
		String TestID;
		TestID= neoloadContext.getTestId();
		return TestID;
		
	}
}
