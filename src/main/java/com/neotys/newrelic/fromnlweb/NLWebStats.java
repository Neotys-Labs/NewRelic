package com.neotys.newrelic.fromnlweb;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.swagger.client.model.TestStatistics;

public class NLWebStats {

	private float LastRequestCountPerSecond=0;
	private float LastRequestDurationAverage=0;
	

	private float LastTransactionDurationAverage=0;
	private int   LastVirtualUserCount=0;
	private long TotalGlobalCountFailure=0;
	private float TotalGlobalDownloadedBytes=0;
	private float TotalGlobalDownloadedBytesPerSecond=0;
	private long TotalIterationCountFailure=0;
	private long TotalIterationCountSuccess=0;
	private long TotalRequestCountFailure=0;
	private float TotalRequestCountPerSecond=0;
	private long TotalRequestCountSuccess=0;
	private long TotalTransactionCountFailure=0;
	private long TotalTransactionCountSuccess=0;
	private float TotalTransactionCountPerSecond=0;
	
	private static String LastRequestCountPerSecond_MetricName="Request Count";
	private static String LastRequestCountPerSecond_Component="requestCount";
	private static String LastRequestCountPerSecond_Unit="request/Second";

	private static String LastRequestDurationAverage_MetricName="Request duration";
	private static String LastRequestDurationAverage_Component="requestduration";
	private static String LastRequestDurationAverage_Unit="Second";

	private static String LastTransactionDurationAverage_MetricName="Average Transaction Duration";
	private static String LastTransactionDurationAverage_Component="averageTransactionDuration";
	private static String LastTransactionDurationAverage_Unit="Second";

	
	private static String LastVirtualUserCount_MetricName="User Load";
	private static String LastVirtualUserCount_Component="userLoad";
	private static String LastVirtualUserCount_Unit="Count";
	
	private static String TotalGlobalCountFailure_MetricName="Number of Failure";
	private static String TotalGlobalCountFailure_Component="globalCountFailure";
	private static String TotalGlobalCountFailure_Unit="count";
	
	private static String TotalGlobalDownloadedBytes_MetricName="Downloaded Bytes";
	private static String TotalGlobalDownloadedBytes_Component="dowLoadedBytes";
	private static String TotalGlobalDownloadedBytes_Unit="Bytes";
	
	private static String TotalGlobalDownloadedBytesPerSecond_MetricName="Downloaded Bytes";
	private static String TotalGlobalDownloadedBytesPerSecond_Component="downloadedBytesPerSecond";
	private static String TotalGlobalDownloadedBytesPerSecond_Unit="Bytes/Second";
	
	private static String TotalIterationCountFailure_MetricName="Iteration in Failure";
	private static String TotalIterationCountFailure_Component="iterationFailure";
	private static String TotalIterationCountFailure_Unit="Count";
	
	private static String TotalIterationCountSuccess_MetricName="Iteration in Success";
	private static String TotalIterationCountSuccess_Component="iterationSuccess";
	private static String TotalIterationCountSuccess_Unit="Count";
	
	private static String TotalRequestCountFailure_MetricName="Request in Failure";
	private static String TotalRequestCountFailure_Component="requestFailure";
	private static String TotalRequestCountFailure_Unit="Count";
	
	private static String TotalRequestCountPerSecond_MetricName="Number of request";
	private static String TotalRequestCountPerSecond_Component="requestCount";
	private static String TotalRequestCountPerSecond_Unit="Request/Second";
	
	private static String TotalRequestCountSuccess_MetricName="Request in Success";
	private static String TotalRequestCountSuccess_Component="requestSuccess";
	private static String TotalRequestCountSuccess_Unit="Count";
	
	private static String TotalTransactionCountFailure_MetricName="Transaction in Failure";
	private static String TotalTransactionCountFailure_Component="transactionFailure";
	private static String TotalTransactionCountFailure_Unit="Count";
	
	private static String TotalTransactionCountSucess_MetricName="Transaction in Success";
	private static String TotalTransactionCountSucess_Component="transactionSuccess";
	private static String TotalTransactionCountSucess_Unit="Count";

	private static String TotalTransactionCountPerSecond_MetricName="Number of Transaction";
	private static String TotalTransactionCountPerSecond_Component="transactionCount";
	private static String TotalTransactionCountPerSecond_Unit="Transaction/Second";

	private long lasduration=0;

	
	public NLWebStats()
	{
		lasduration=0;
	}
	
	public float getLastRequestDurationAverage() {
		return LastRequestDurationAverage;
	}



	public void setLastRequestDurationAverage(float lastRequestDurationAverage) {
		LastRequestDurationAverage = lastRequestDurationAverage/1000;
	}

	public NLWebStats(float lastRequestCountPerSecond, float lastTransactionDurationAverage, int lastVirtualUserCount,
			long totalGlobalCountFailure, float totalGlobalDownloadedBytes, float totalGlobalDownloadedBytesPerSecond,
			long totalIterationCountFailure, long totalIterationCountSuccess, long totalRequestCountFailure,
			float totalRequestCountPerSecond, long totalRequestCountSuccess,
			long totalTransactionCountFailure,long totalTransactionCountSucess, float totalTransactionCountPerSecond,float lastrequestduration
			) {
		super();
		
		
		LastRequestCountPerSecond = lastRequestCountPerSecond;
		LastTransactionDurationAverage = lastTransactionDurationAverage/1000;
		
		LastRequestDurationAverage=lastrequestduration/1000;
		
		LastVirtualUserCount = lastVirtualUserCount;
		
		TotalTransactionCountPerSecond = totalTransactionCountPerSecond;
		
		if(TotalGlobalCountFailure==0)
			TotalGlobalCountFailure = totalGlobalCountFailure;
		else
			TotalGlobalCountFailure = totalGlobalCountFailure- TotalGlobalCountFailure;
		
		
		if(TotalGlobalDownloadedBytes==0)
			TotalGlobalDownloadedBytes = totalGlobalDownloadedBytes;
		else
			TotalGlobalDownloadedBytes = totalGlobalDownloadedBytes- TotalGlobalDownloadedBytes;
		
		

		if(TotalGlobalDownloadedBytesPerSecond==0)
			TotalGlobalDownloadedBytesPerSecond =totalGlobalDownloadedBytesPerSecond;
		else
			TotalGlobalDownloadedBytesPerSecond = totalGlobalDownloadedBytesPerSecond- TotalGlobalDownloadedBytesPerSecond;
		
		if(TotalIterationCountFailure==0)
			TotalIterationCountFailure = totalIterationCountFailure;
		else
			TotalIterationCountFailure = totalIterationCountFailure- TotalIterationCountFailure;
		
		if(TotalIterationCountSuccess==0)
			TotalIterationCountSuccess = totalIterationCountSuccess;
		else
			TotalIterationCountSuccess = totalIterationCountSuccess- TotalIterationCountSuccess;
	
		if(TotalRequestCountFailure==0)
			TotalRequestCountFailure = totalRequestCountFailure;
		else
			TotalRequestCountFailure = totalRequestCountFailure- TotalRequestCountFailure;
	
		if(TotalRequestCountPerSecond==0)
			TotalRequestCountPerSecond = totalRequestCountPerSecond;
		else
			TotalRequestCountPerSecond = totalRequestCountPerSecond- TotalRequestCountPerSecond;
	
		if(TotalRequestCountSuccess==0)
			TotalRequestCountSuccess = totalRequestCountSuccess;
		else
			TotalRequestCountSuccess = totalRequestCountSuccess- TotalRequestCountSuccess;
	
			
		if(TotalTransactionCountFailure==0)
			TotalTransactionCountFailure = totalTransactionCountFailure;
		else
			TotalTransactionCountFailure = totalTransactionCountFailure- TotalTransactionCountFailure;
	
		if(TotalTransactionCountSuccess==0)
			TotalTransactionCountSuccess = totalTransactionCountSucess;
		else
			TotalTransactionCountSuccess =  totalTransactionCountSucess- TotalTransactionCountSuccess;
		
		if(TotalTransactionCountPerSecond==0)
			TotalTransactionCountPerSecond = totalTransactionCountPerSecond;
		else
			TotalTransactionCountPerSecond = totalTransactionCountPerSecond- TotalTransactionCountPerSecond;
	
		
	}
	
	
	public NLWebStats(TestStatistics response)
	{
		
		
		LastRequestCountPerSecond = response.getLastRequestCountPerSecond();
		LastTransactionDurationAverage = response.getLastTransactionDurationAverage()/1000;
		LastVirtualUserCount = response.getLastVirtualUserCount();
		LastRequestDurationAverage=response.getTotalRequestDurationAverage()/1000;
	
		
		if(TotalGlobalCountFailure==0)
			TotalGlobalCountFailure = response.getTotalGlobalCountFailure();
		else
			TotalGlobalCountFailure = response.getTotalGlobalCountFailure()- TotalGlobalCountFailure;
		
		
		if(TotalGlobalDownloadedBytes==0)
			TotalGlobalDownloadedBytes = response.getTotalGlobalDownloadedBytes();
		else
			TotalGlobalDownloadedBytes = response.getTotalGlobalDownloadedBytes()- TotalGlobalDownloadedBytes;
		
		

		
		if(TotalGlobalDownloadedBytesPerSecond==0)
			TotalGlobalDownloadedBytesPerSecond = response.getTotalGlobalDownloadedBytesPerSecond();
		else
			TotalGlobalDownloadedBytesPerSecond = response.getTotalGlobalDownloadedBytesPerSecond()- TotalGlobalDownloadedBytesPerSecond;
		
		if(TotalIterationCountFailure==0)
			TotalIterationCountFailure = response.getTotalIterationCountFailure();
		else
			TotalIterationCountFailure = response.getTotalIterationCountFailure()- TotalIterationCountFailure;
		
		if(TotalIterationCountSuccess==0)
			TotalIterationCountSuccess = response.getTotalIterationCountSuccess();
		else
			TotalIterationCountSuccess = response.getTotalIterationCountSuccess()- TotalIterationCountSuccess;
	
		if(TotalRequestCountFailure==0)
			TotalRequestCountFailure = response.getTotalRequestCountFailure();
		else
			TotalRequestCountFailure = response.getTotalRequestCountFailure()- TotalRequestCountFailure;
	
		if(TotalRequestCountPerSecond==0)
			TotalRequestCountPerSecond = response.getTotalRequestCountPerSecond();
		else
			TotalRequestCountPerSecond = response.getTotalRequestCountPerSecond()- TotalRequestCountPerSecond;
	
		if(TotalRequestCountSuccess==0)
			TotalRequestCountSuccess = response.getTotalRequestCountSuccess();
		else
			TotalRequestCountSuccess = response.getTotalRequestCountSuccess()- TotalRequestCountSuccess;
	
			
		if(TotalTransactionCountFailure==0)
			TotalTransactionCountFailure = response.getTotalTransactionCountFailure();
		else
			TotalTransactionCountFailure = response.getTotalTransactionCountFailure()- TotalTransactionCountFailure;
	
	
		if(TotalTransactionCountSuccess==0)
			TotalTransactionCountSuccess = response.getTotalTransactionCountSuccess();
		else
			TotalTransactionCountSuccess = response.getTotalTransactionCountSuccess()- TotalTransactionCountSuccess;
	
		if(TotalTransactionCountPerSecond==0)
			TotalTransactionCountPerSecond = response.getTotalTransactionCountFailure();
		else
			TotalTransactionCountPerSecond = response.getTotalTransactionCountPerSecond()- TotalTransactionCountPerSecond;
	

		}
	
	public long getLasduration() {
		return lasduration;
	}


	public void setLasduration(long lasduration) {
		this.lasduration = lasduration;
	}
	
	public void UpdateStat(TestStatistics response)
	{
		setLastRequestCountPerSecond(response.getLastRequestCountPerSecond());
		setLastTransactionDurationAverage(response.getLastTransactionDurationAverage());
		setLastVirtualUserCount(response.getLastVirtualUserCount());
		setTotalGlobalCountFailure(response.getTotalGlobalCountFailure());
		setTotalGlobalDownloadedBytes(response.getTotalGlobalDownloadedBytes());
		setTotalGlobalDownloadedBytesPerSecond(response.getTotalGlobalDownloadedBytesPerSecond());
		setTotalIterationCountFailure(response.getTotalIterationCountFailure());
		setTotalIterationCountSuccess(response.getTotalIterationCountSuccess());
		setTotalRequestCountFailure(response.getTotalRequestCountFailure());
		setTotalRequestCountPerSecond(response.getTotalRequestCountPerSecond());
		setTotalRequestCountSuccess(response.getTotalRequestCountSuccess());
		setTotalTransactionCountFailure(response.getTotalTransactionCountFailure());
		setTotalTransactionCountPerSecond(response.getTotalTransactionCountPerSecond());
		setTotalTransactionCountSucess(response.getTotalTransactionCountSuccess());
		setLastRequestDurationAverage(response.getTotalRequestDurationAverage());
	}
	public float getLastRequestCountPerSecond() {
		return LastRequestCountPerSecond;
	}

	public String[] GetRequestrequestDuration()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[4];
		result[0]=LastRequestDurationAverage_MetricName;
		result[1]=LastRequestDurationAverage_Component;
		result[2]=LastRequestDurationAverage_Unit;
		result[3]=df.format(getLastRequestDurationAverage());
		return result;
		
	}
	
	public String[] GetRequestCountData()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[4];
		result[0]=LastRequestCountPerSecond_MetricName;
		result[1]=LastRequestCountPerSecond_Component;
		result[2]=LastRequestCountPerSecond_Unit;
		result[3]=df.format(getLastRequestCountPerSecond());
		return result;
		
	}
	public void setLastRequestCountPerSecond(float lastRequestCountPerSecond) {
		LastRequestCountPerSecond = lastRequestCountPerSecond;
	}


	public float getLastTransactionDurationAverage() {
		return LastTransactionDurationAverage;
	}
	public String[] GetTransactionDuractionData()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[4];
		result[0]=LastTransactionDurationAverage_MetricName;
		result[1]=LastTransactionDurationAverage_Component;
		result[2]=LastTransactionDurationAverage_Unit;
		result[3]=df.format(getLastTransactionDurationAverage());
		return result;
		
	}


	public void setLastTransactionDurationAverage(float lastTransactionDurationAverage) {
		LastTransactionDurationAverage = lastTransactionDurationAverage/1000;
	}


	public int getLastVirtualUserCount() {
		return LastVirtualUserCount;
	}


	public String[] GetVirtualUserCountData()
	{
		String[] result=new String[4];
		result[0]=LastVirtualUserCount_MetricName;
		result[1]=LastVirtualUserCount_Component;
		result[2]=LastVirtualUserCount_Unit;
		result[3]=String.valueOf(getLastVirtualUserCount());
		return result;
		
	}
	
	public void setLastVirtualUserCount(int lastVirtualUserCount) {
		LastVirtualUserCount = lastVirtualUserCount;
	}


	public long getTotalGlobalCountFailure() {
		return TotalGlobalCountFailure;
	}

	public String[] GetTotalGlobalCountFailureData()
	{
		String[] result=new String[4];
		result[0]=TotalGlobalCountFailure_MetricName;
		result[1]=TotalGlobalCountFailure_Component;
		result[2]=TotalGlobalCountFailure_Unit;
		result[3]=String.valueOf(getTotalGlobalCountFailure());
		return result;
		
	}

	public void setTotalGlobalCountFailure(long totalGlobalCountFailure) {
		if(TotalGlobalCountFailure==0)
			TotalGlobalCountFailure = totalGlobalCountFailure;
		else
			TotalGlobalCountFailure = totalGlobalCountFailure -TotalGlobalCountFailure;
	}

	public List<String[]> GetNLData()
	{
		List<String[]> result = new ArrayList<String[]>();
		result.add(GetRequestCountData());
		result.add(GetTotalGlobalCountFailureData());
		result.add(GetTotalGlobalDownloadedBytesData());		
		result.add(GetTotalGlobalDownloadedBytesPerSecondData());
		result.add(GetTotalIterationCountFailureData());
		result.add(GetTotalIterationCountSuccessData());
		result.add(GetTotalRequestCountFailureData());
		result.add(GetTotalRequestCountPerSecondData());
		result.add(GetTotalRequestCountSuccessData());
		result.add(GetTransactionCountFailureData());
		result.add(GetTransactionCountPefSecondData());
		result.add(GetTransactionCountSucessData());
		result.add(GetTransactionDuractionData());
		result.add(GetVirtualUserCountData());
		result.add(GetRequestrequestDuration());
		
		return result;
	}

	public float getTotalGlobalDownloadedBytes() {
		return TotalGlobalDownloadedBytes;
	}


	public String[] GetTotalGlobalDownloadedBytesData()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[4];
		result[0]=TotalGlobalDownloadedBytes_MetricName;
		result[1]=TotalGlobalDownloadedBytes_Component;
		result[2]=TotalGlobalDownloadedBytes_Unit;
		result[3]=df.format(getTotalGlobalDownloadedBytes());
		return result;
		
	}
	
	public void setTotalGlobalDownloadedBytes(float totalGlobalDownloadedBytes) {
		if(TotalGlobalDownloadedBytes==0)
			TotalGlobalDownloadedBytes = totalGlobalDownloadedBytes;
		else
			TotalGlobalDownloadedBytes = totalGlobalDownloadedBytes-TotalGlobalDownloadedBytes;
	}


	public float getTotalGlobalDownloadedBytesPerSecond() {
		return TotalGlobalDownloadedBytesPerSecond;
	}


	public String[] GetTotalGlobalDownloadedBytesPerSecondData()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[4];
		result[0]=TotalGlobalDownloadedBytesPerSecond_MetricName;
		result[1]=TotalGlobalDownloadedBytesPerSecond_Component;
		result[2]=TotalGlobalDownloadedBytesPerSecond_Unit;
		result[3]=df.format(getTotalGlobalDownloadedBytesPerSecond());
		return result;
		
	}
	
	public void setTotalGlobalDownloadedBytesPerSecond(float totalGlobalDownloadedBytesPerSecond) {
		if(TotalGlobalDownloadedBytesPerSecond==0)
			TotalGlobalDownloadedBytesPerSecond = totalGlobalDownloadedBytesPerSecond;
		else
			TotalGlobalDownloadedBytesPerSecond = totalGlobalDownloadedBytesPerSecond-TotalGlobalDownloadedBytesPerSecond;
			
	}


	public long getTotalIterationCountFailure() {
		return TotalIterationCountFailure;
	}


	public String[] GetTotalIterationCountFailureData()
	{
		String[] result=new String[4];
		result[0]=TotalIterationCountFailure_MetricName;
		result[1]=TotalIterationCountFailure_Component;
		result[2]=TotalIterationCountFailure_Unit;
		result[3]=String.valueOf(getTotalIterationCountFailure());
		return result;
		
	}
	
	public void setTotalIterationCountFailure(long totalIterationCountFailure) {
		if(TotalIterationCountFailure==0)
			TotalIterationCountFailure = totalIterationCountFailure;
		else
			TotalIterationCountFailure = totalIterationCountFailure-TotalIterationCountFailure;
	}


	public long getTotalIterationCountSuccess() {
		return TotalIterationCountSuccess;
	}


	public String[] GetTotalIterationCountSuccessData()
	{
		String[] result=new String[4];
		result[0]=TotalIterationCountSuccess_MetricName;
		result[1]=TotalIterationCountSuccess_Component;
		result[2]=TotalIterationCountSuccess_Unit;
		result[3]=String.valueOf(getTotalIterationCountSuccess());
		return result;
		
	}

	public void setTotalIterationCountSuccess(long totalIterationCountSuccess) {
		if(TotalIterationCountSuccess==0)
			TotalIterationCountSuccess = totalIterationCountSuccess;
		else
			TotalIterationCountSuccess = totalIterationCountSuccess-TotalIterationCountSuccess;
	}


	public long getTotalRequestCountFailure() {
		return TotalRequestCountFailure;
	}


	public String[] GetTotalRequestCountFailureData()
	{
		String[] result=new String[4];
		result[0]=TotalRequestCountFailure_MetricName;
		result[1]=TotalRequestCountFailure_Component;
		result[2]=TotalRequestCountFailure_Unit;
		result[3]=String.valueOf(getTotalRequestCountFailure());
		return result;
		
	}

	public void setTotalRequestCountFailure(long totalRequestCountFailure) {
		if(TotalRequestCountFailure==0)
			TotalRequestCountFailure = totalRequestCountFailure;
		else
			TotalRequestCountFailure = totalRequestCountFailure-TotalRequestCountFailure;
	}


	public float getTotalRequestCountPerSecond() {
		return TotalRequestCountPerSecond;
	}

	public String[] GetTotalRequestCountPerSecondData()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[4];
		result[0]=TotalRequestCountPerSecond_MetricName;
		result[1]=TotalRequestCountPerSecond_Component;
		result[2]=TotalRequestCountPerSecond_Unit;
		result[3]=df.format(getTotalRequestCountPerSecond());
		return result;
		
	}
	public void setTotalRequestCountPerSecond(Float totalRequestCountPerSecond) {
		if(TotalRequestCountPerSecond==0)
			TotalRequestCountPerSecond = totalRequestCountPerSecond;
		else
			TotalRequestCountPerSecond = totalRequestCountPerSecond-TotalRequestCountPerSecond;
	}


	public long getTotalRequestCountSuccess() {
		return TotalRequestCountSuccess;
	}

	public String[] GetTotalRequestCountSuccessData()
	{
		String[] result=new String[4];
		result[0]=TotalRequestCountSuccess_MetricName;
		result[1]=TotalRequestCountSuccess_Component;
		result[2]=TotalRequestCountSuccess_Unit;
		result[3]=String.valueOf(getTotalRequestCountSuccess());
		return result;
		
	}
	
	public void setTotalRequestCountSuccess(long totalRequestCountSuccess) {
		if(TotalRequestCountSuccess==0)
			TotalRequestCountSuccess = totalRequestCountSuccess;
		else
			TotalRequestCountSuccess=totalRequestCountSuccess-TotalRequestCountSuccess;
	}

	

	public long getTotalTransactionCountFailure() {
		return TotalTransactionCountFailure;
	}


	public String[] GetTransactionCountFailureData()
	{
		String[] result=new String[4];
		result[0]=TotalTransactionCountFailure_MetricName;
		result[1]=TotalTransactionCountFailure_Component;
		result[2]=TotalTransactionCountFailure_Unit;
		result[3]=String.valueOf(getTotalTransactionCountFailure());
		return result;
		
	}
	
	public void setTotalTransactionCountFailure(long totalTransactionCountFailure) {
		if(TotalTransactionCountFailure==0)
			TotalTransactionCountFailure = totalTransactionCountFailure;
		else
			TotalTransactionCountFailure = totalTransactionCountFailure-TotalTransactionCountFailure;
	}
	
	public long getTotalTransactionCountSucess() {
		return TotalTransactionCountSuccess;
	}


	public String[] GetTransactionCountSucessData()
	{
		
		String[] result=new String[4];
		result[0]=TotalTransactionCountSucess_MetricName;
		result[1]=TotalTransactionCountSucess_Component;
		result[2]=TotalTransactionCountSucess_Unit;
		result[3]=String.valueOf(getTotalTransactionCountSucess());
		return result;
		
	}
	
	public void setTotalTransactionCountSucess(long totalTransactionCountFailure) {
		if(TotalTransactionCountSuccess==0)
			TotalTransactionCountSuccess = totalTransactionCountFailure;
		else
			TotalTransactionCountSuccess = totalTransactionCountFailure-TotalTransactionCountSuccess;
	}

	public float getTotalTransactionCountPerSecond() {
		return TotalTransactionCountPerSecond;
	}

	public String[] GetTransactionCountPefSecondData()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[4];
		result[0]=TotalTransactionCountPerSecond_MetricName;
		result[1]=TotalTransactionCountPerSecond_Component;
		result[2]=TotalTransactionCountPerSecond_Unit;
		result[3]=df.format(getTotalTransactionCountPerSecond());
		return result;
		
	}
	
	public void setTotalTransactionCountPerSecond(float totalTransactionCountPerSecond) {
		if(TotalTransactionCountPerSecond==0)
			TotalTransactionCountPerSecond = totalTransactionCountPerSecond;
		else
			TotalTransactionCountPerSecond = totalTransactionCountPerSecond-TotalTransactionCountPerSecond;
	}


	
	
}
