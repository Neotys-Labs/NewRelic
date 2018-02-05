package com.neotys.newrelic.infrastucture;

import java.text.DecimalFormat;
import java.util.List;

import io.swagger.client.model.ElementDefinition;
import io.swagger.client.model.ElementValues;

public class NLGlobalValues {

	
	private String MetricName;
	

	private String MetricPath;
	private float MetricAvergageResponsetime;
	private float MetricElementPerSecond;
	private float MetricDownloadedBytesPersocond;
	private String UserPath;
	
	
	
	public NLGlobalValues(String metricName, String metricPath, float metricAvergageResponsetime,
			float metricElementPerSecond,float metricdownloadedbytespersecond) {
		super();
		MetricName = metricName;
		MetricPath = metricPath;
		MetricAvergageResponsetime = metricAvergageResponsetime;
		MetricElementPerSecond = metricElementPerSecond;
	}
	public NLGlobalValues(ElementDefinition definition,ElementValues values) {
		super();
		DefineMetricName(definition);
		SetValues(values);
	}
	public float getMetricDownloadedBytesPersocond() {
		return MetricDownloadedBytesPersocond;
	}
	public void setMetricDownloadedBytesPersocond(float metricDownloadedBytesPersocond) {
		MetricDownloadedBytesPersocond = metricDownloadedBytesPersocond;
	}
	
	public String getMetricName() {
		return MetricName;
	}


	public void setMetricName(String metricName) {
		MetricName = metricName;
	}


	public String getMetricPath() {
		return MetricPath;
	}

	public void DefineMetricName(ElementDefinition definition)
	{
		MetricName=definition.getName();
		MetricPath=GetPath(definition.getPath());
		UserPath=GetUserPathName(definition.getPath());
	}

	public void setMetricPath(String metricPath) {
		MetricPath = metricPath;
	}


	public float getMetricAvergageResponsetime() {
		return MetricAvergageResponsetime;
	}


	public void setMetricAvergageResponsetime(float metricAvergageResponsetime) {
		MetricAvergageResponsetime = metricAvergageResponsetime;
	}


	public float getMetricElementPerSecond() {
		return MetricElementPerSecond;
	}


	public void setMetricElementPerSecond(float metricElementPerSecond) {
		MetricElementPerSecond = metricElementPerSecond;
	}

	public void SetValues(ElementValues values)
	{
		MetricAvergageResponsetime=values.getAvgDuration()/1000;
		MetricElementPerSecond=values.getElementPerSecond();
		MetricDownloadedBytesPersocond=values.getDownloadedBytesPerSecond();
	}
	
	private String GetUserPathName(List<String> list)
	{
		return list.get(0);
	}
	
	private String GetPath(List<String> list)
	{
		String result = "";
		
		for(String p : list )
		{
			result+=p+"/";
		}
		result=result.substring(0, result.length() - 1);
		
		return result;
		
	}
	
	public String[] GetElementValue()
	{
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result=new String[7];
		result[0]=MetricName;
		result[1]=UserPath;
		result[2]=MetricPath;
		result[3]=df.format(MetricAvergageResponsetime);
		result[4]=df.format(MetricDownloadedBytesPersocond);
		result[5]=df.format(MetricElementPerSecond);
		result[6]="TRANSACTION";
		return result;
		
	}
}
