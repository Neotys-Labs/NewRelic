package com.neotys.newrelic.infrastucture;


import com.google.common.base.Optional;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import io.swagger.client.ApiClient;
import io.swagger.client.api.ResultsApi;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Timer;

import static com.neotys.newrelic.NewRelicUtils.getProxy;
import static com.neotys.newrelic.NewRelicUtils.initProxyForNeoloadWebApiClient;


public class NewRelicPluginData {
	private static final String NLWEB_VERSION = "v1";

	private final int MAXDURATION_TIME=2000;

	private String newRelicLicenseKey;
	private ApiClient neoloadWebApiClient;
	private Context neoloadContext;
	private final Optional<String> proxyName;
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
	
	public NewRelicPluginData(String newRelicLicenseKey, final Context neoloadContext, String Insight_AccountID, String Insight_APIKEY, String ApplicationNAme, String ApplicationAPIKEY, final Optional<String> proxyName) throws NewRelicException, IOException, NoSuchAlgorithmException, KeyManagementException {
		super();
		this.newRelicLicenseKey = newRelicLicenseKey;
		Insight_Accountid=Insight_AccountID;
		Insight_APIKey=Insight_APIKEY;
		this.neoloadContext = neoloadContext;
		this.proxyName = proxyName;

		//----define  the NLWEB API-----
		this.neoloadWebApiClient = new ApiClient();
		this.neoloadWebApiClient.setApiKey(neoloadContext.getAccountToken());
		final String basePath = getBasePath(neoloadContext);
		this.neoloadWebApiClient.setBasePath(basePath);
		final Optional<Proxy> proxyOptional = getProxy(neoloadContext, proxyName, basePath);
		if(proxyOptional.isPresent()) {
			initProxyForNeoloadWebApiClient(neoloadWebApiClient, proxyOptional.get());
		}
		initNeoloadWebApi();
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
				NLaggregator=new NeoLoadStatAggregator(this.newRelicLicenseKey,NLWEBresult,TestID,NLStat,Insight_Accountid,Insight_APIKey,TestName,NewRelicApplicationName,NewRelicApplicationAPIKEY,GetTestScenarioName(), neoloadContext, proxyName);
		}
	}

	private String getBasePath(final Context context) {
		final String webPlatformApiUrl = context.getWebPlatformApiUrl();
		final StringBuilder basePathBuilder = new StringBuilder(webPlatformApiUrl);
		if(!webPlatformApiUrl.endsWith("/")) {
			basePathBuilder.append("/");
		}
		basePathBuilder.append(NLWEB_VERSION + "/");
		return basePathBuilder.toString();
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
		NLaggregator=new NeoLoadStatAggregator(newRelicLicenseKey,NLWEBresult,TestID,NLStat,Insight_Accountid,Insight_APIKey,TestName,NewRelicApplicationName,NewRelicApplicationAPIKEY,GetTestScenarioName(), neoloadContext, proxyName);
			
		timerNewRelic.scheduleAtFixedRate(NLaggregator,TIMERDELAY,TIMERFREQUENCY);
		
	}
	private void initNeoloadWebApi()
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
