package com.neotys.newrelic.infrastucture;

import com.google.common.base.Optional;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import com.neotys.newrelic.Constants;
import com.neotys.newrelic.NewRelicActionArguments;
import com.neotys.newrelic.http.HTTPGenerator;
import io.swagger.client.ApiException;
import io.swagger.client.api.ResultsApi;
import io.swagger.client.model.ArrayOfElementDefinition;
import io.swagger.client.model.ElementDefinition;
import io.swagger.client.model.ElementValues;
import io.swagger.client.model.TestStatistics;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

import static com.neotys.newrelic.NewRelicUtils.getProxy;

public class NeoLoadStatAggregator extends TimerTask {
	
	private final Context neoloadContext;	
	private final NewRelicActionArguments newRelicActionArguments;
	private final String componentsName;	
	private final ResultsApi nlWebResult;		
	private final Map<String, String> headers = new HashMap<>();
	private NLGlobalStat nlStat;
	private HTTPGenerator http;
	private final String newRelicApplicationId;
	

	public NeoLoadStatAggregator(final Context neoloadContext, final NewRelicActionArguments newRelicActionArguments, final ResultsApi nlWebResult, final NLGlobalStat nlStat) throws NewRelicException, IOException {
		this.neoloadContext = neoloadContext;
		this.newRelicActionArguments = newRelicActionArguments;
		this.componentsName = "Statistics";		
		this.nlStat = nlStat;		
		this.nlWebResult = nlWebResult;
		this.newRelicApplicationId = getApplicationID(newRelicActionArguments);		 
		headers.put("X-License-Key", newRelicActionArguments.getNewRelicLicenseKey().get());
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json");
	}

	private void sendStatsToNewRelic() throws ApiException {
		TestStatistics StatsResult;
		long utc;
		long lasduration;

		utc = System.currentTimeMillis() / 1000;

		lasduration = nlStat.getLasduration();

		if (lasduration == 0 || (utc - lasduration) >= Constants.MIN_NEW_RELIC_DURATION) {
			StatsResult = nlWebResult.getTestStatistics(neoloadContext.getTestId());
			lasduration = sendData(StatsResult, lasduration);
			nlStat.setLasduration(lasduration);
			sendValuesToNewRelic();
		}
	}

	public long sendData(TestStatistics stat, long LasDuration) {
		int time = 0;
		List<String[]> data;
		long utc;
		utc = System.currentTimeMillis() / 1000;

		if (nlStat == null)
			nlStat = new NLGlobalStat(stat);
		else {
			nlStat.UpdateStat(stat);
		}
		data = nlStat.GetNLData();
		if (LasDuration == 0)
			time = 0;
		else {
			time = (int) (utc - LasDuration);
		}
		for (String[] metric : data) {
			try {
				sendMetricToPluginAPI(metric[0], metric[1], time, metric[2], metric[3]);

			} catch (NewRelicException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {

			sendMetricToInsightAPI(data, time);
		} catch (NewRelicException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return utc;
	}

	public String getApplicationID(final NewRelicActionArguments newRelicActionArguments) throws NewRelicException, IOException {
		JSONObject jsoobj;
		String Url;
		JSONArray jsonApplication;
		Map<String, String> Parameters = null;
		Map<String, String> head = null;

		HTTPGenerator ApplicationAPI;
		String newRelicApplicationID = null;

		head = new HashMap<>();
		head.put("X-Api-Key", newRelicActionArguments.getNewRelicAPIKey());
		head.put("Content-Type", "application/json");
		Url = Constants.NEW_RELIC_URL + Constants.APPLICATIONS_JSON;
		Parameters = new HashMap<>();
		Parameters.put("filter[name]", newRelicActionArguments.getNewRelicApplicationName());

		final com.google.common.base.Optional<Proxy> proxy = getProxy(neoloadContext, newRelicActionArguments.getProxyName(), Url);
		ApplicationAPI = new HTTPGenerator(Url, "GET", head, Parameters, proxy);


		jsoobj = ApplicationAPI.getJSONHTTPresponse();
		if (jsoobj != null) {
			if (jsoobj.has("applications")) {
				jsonApplication = jsoobj.getJSONArray("applications");
				newRelicApplicationID = String.valueOf(jsonApplication.getJSONObject(0).getInt("id"));
				if (newRelicApplicationID == null)
					throw new NewRelicException("No Application find in The NewRelic Account");
			} 
		} 
		return newRelicApplicationID;

	}

	private void sendValuesToNewRelic() {		
		final List<Metric> NlValues = new ArrayList<>();
		try {
			final ArrayOfElementDefinition NLElement = nlWebResult.getTestElements(neoloadContext.getTestId(), Constants.NLWEB_TRANSACTION);
			for (ElementDefinition element : NLElement) {
				if (element.getType().equalsIgnoreCase("TRANSACTION")) {
					final ElementValues Values = nlWebResult.getTestElementsValues(neoloadContext.getTestId(), element.getId());
					NlValues.add(new Metric(element, Values));
				}
			}
			for (Metric val : NlValues){
				sendValueMetricToInsightAPI(val.getElementValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendValueMetricToInsightAPI(final String[] data) throws NewRelicException {
		int httpcode;
		final Map<String, String> head = new HashMap<>();
		HTTPGenerator Insight_HTTP = null;


		head.put("X-Insert-Key", newRelicActionArguments.getNewRelicInsightsAPIKey().get());
		head.put("Content-Type", "application/json");
		String URL;
		long ltime = System.currentTimeMillis();

		URL = Constants.NEW_RELIC_INSIGHT_URL + newRelicActionArguments.getNewRelicAccountId() + "/events";
		String Exceptionmessage = null;

		String JSON_String = "[{\"eventType\":\"NeoLoadValues\","
				+ "\"account\" : \"" + newRelicActionArguments.getNewRelicAccountId() + "\","
				+ "\"appId\" : \"" + newRelicApplicationId + "\","
				+ "\"testName\" : \"" + neoloadContext.getTestName() + "\","
				+ "\"scenarioName\" : \"" + neoloadContext.getScenarioName() + "\","
				+ "\"applicationName\" : \"" + newRelicActionArguments.getNewRelicApplicationName() + "\","
				+ "\"trendfield\": \"" + newRelicActionArguments.getNewRelicApplicationName() + neoloadContext.getScenarioName() + neoloadContext.getTestName() + "\","
				+ "\"userPathName\" :\"" + data[1] + "\","
				+ "\"type\" :\"" + data[6] + "\","
				+ "\"transactionName\" :\"" + data[0] + "\","
				+ "\"path\" :\"" + data[2] + "\","
				+ "\"responseTime\":" + data[3] + ","
				+ "\"elementPerSecond\":" + data[5] + ","
				+ "\"downloadedBytesPerSecond\":" + data[4] + ","
				+ "\"timestamp\" : " + ltime + "}]";

		try {
			final com.google.common.base.Optional<Proxy> proxy = getProxy(neoloadContext, newRelicActionArguments.getProxyName(), URL);
			Insight_HTTP = new HTTPGenerator(URL, head, JSON_String, proxy);
			httpcode = Insight_HTTP.getHttpResponseCodeFromResponse();
			switch (httpcode) {

				case Constants.BAD_REQUEST:
					Exceptionmessage = "The request or headers are in the wrong format, or the URL is incorrect, or the GUID does not meet the validation requirements.";
					break;
				case Constants.UNAUTHORIZED:
					Exceptionmessage = "Authentication error (no license key header, or invalid license key).";
					break;
				case Constants.NOT_FOUND:
					Exceptionmessage = "Invalid URL.";
					break;
				case Constants.METHOD_NOT_ALLOWED:
					Exceptionmessage = "Returned if the method is an invalid or unexpected type (GET/POST/PUT/etc.).";
					break;
				case Constants.REQUEST_ENTITY_TOO_LARGE:
					Exceptionmessage = "Too many metrics were sent in one request, or too many components (instances) were specified in one request, or other single-request limits were reached.";
					break;
				case Constants.INTERNAL_SERVER_ERROR:
					Exceptionmessage = "Unexpected server error";
					break;
				case Constants.BAD_GATEWAY:
					Exceptionmessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					break;
				case Constants.SERVICE_UNAVAIBLE:
					Exceptionmessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					break;
				case Constants.GATEWAY_TIMEOUT:
					Exceptionmessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					break;

			}
			if (Exceptionmessage != null)
				throw new NewRelicException(Exceptionmessage);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (Insight_HTTP != null) {
				Insight_HTTP.closeHttpClient();
			}
		}

	}

	private void sendMetricToInsightAPI(List<String[]> data, int time) throws NewRelicException {
		int httpcode;
		final Map<String, String> head = new HashMap<>();
		HTTPGenerator insight_HTTP = null;

		head.put("X-Insert-Key", newRelicActionArguments.getNewRelicInsightsAPIKey().get());
		head.put("Content-Type", "application/json");
		String URL;
		long ltime = System.currentTimeMillis();

		URL = Constants.NEW_RELIC_INSIGHT_URL + newRelicActionArguments.getNewRelicAccountId() + "/events";
		String Exceptionmessage = null;

		String JSON_String = "[{\"eventType\":\"NeoLoadData\","
				+ "\"account\" : \"" + newRelicActionArguments.getNewRelicAccountId() + "\","
				+ "\"appId\" : \"" + newRelicApplicationId + "\","
				+ "\"testName\" : \"" + neoloadContext.getTestName() + "\","
				+ "\"scenarioName\" : \"" + neoloadContext.getScenarioName() + "\","
				+ "\"applicationName\" : \"" + newRelicActionArguments.getNewRelicApplicationName() + "\","
				+ "\"trendfield\": \"" + newRelicActionArguments.getNewRelicApplicationName() + neoloadContext.getScenarioName() + neoloadContext.getTestName() + "\",";

		for (String[] metric : data) {
			JSON_String += "\"" + metric[1] + "\" : " + metric[3] + ",";

		}
		JSON_String += "\"MetricUnit\" : \"\","
				+ "\"timestamp\" : " + ltime + "}]";


		try {
			final com.google.common.base.Optional<Proxy> proxy = getProxy(neoloadContext, newRelicActionArguments.getProxyName(), URL);
			insight_HTTP = new HTTPGenerator(URL, head, JSON_String, proxy);
			httpcode = insight_HTTP.getHttpResponseCodeFromResponse();
			switch (httpcode) {

				case Constants.BAD_REQUEST:
					Exceptionmessage = "The request or headers are in the wrong format, or the URL is incorrect, or the GUID does not meet the validation requirements.";
					break;
				case Constants.UNAUTHORIZED:
					Exceptionmessage = "Authentication error (no license key header, or invalid license key).";
					break;
				case Constants.NOT_FOUND:
					Exceptionmessage = "Invalid URL.";
					break;
				case Constants.METHOD_NOT_ALLOWED:
					Exceptionmessage = "Returned if the method is an invalid or unexpected type (GET/POST/PUT/etc.).";
					break;
				case Constants.REQUEST_ENTITY_TOO_LARGE:
					Exceptionmessage = "Too many metrics were sent in one request, or too many components (instances) were specified in one request, or other single-request limits were reached.";
					break;
				case Constants.INTERNAL_SERVER_ERROR:
					Exceptionmessage = "Unexpected server error";
					break;
				case Constants.BAD_GATEWAY:
					Exceptionmessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					break;
				case Constants.SERVICE_UNAVAIBLE:
					Exceptionmessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					break;
				case Constants.GATEWAY_TIMEOUT:
					Exceptionmessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					break;

			}
			if (Exceptionmessage != null)
				throw new NewRelicException(Exceptionmessage);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(insight_HTTP!=null){
				insight_HTTP.closeHttpClient();
			}
		}
	}

	private void sendMetricToPluginAPI(final String MetricName, final String MetricPath, final int Duration, final String unit, final String value) throws NewRelicException {
		int httpcode;
		String Exceptionmessage = null;

		String jsonString = "{\"agent\":{"
				+ "\"host\" : \"" + Constants.NLGUID + "\","
				+ "\"version\" : \"" + Constants.VERSION + "\""
				+ "},"
				+ "\"components\": ["
				+ "{"
				+ "\"name\": \"NeoLoad\","
				+ "\"guid\": \"" + Constants.NLGUID + "\","
				+ " \"duration\" : " + String.valueOf(Duration) + ","
				+ " \"metrics\" : {"
				+ " \"Component/NeoLoad/" + componentsName + "/" + MetricPath + "[" + unit + "]\": " + value + ""
				+ "}"
				+ "}"
				+ "]"
				+ "}";

		try {
			final com.google.common.base.Optional<Proxy> proxy = getProxy(neoloadContext, newRelicActionArguments.getProxyName(), Constants.NEW_RELIC_PLATFORM_API_URL);
			http = new HTTPGenerator(Constants.NEW_RELIC_PLATFORM_API_URL, headers, jsonString, proxy);
			httpcode = http.getHttpResponseCodeFromResponse();
			switch (httpcode) {

				case Constants.BAD_REQUEST:
					Exceptionmessage = "The request or headers are in the wrong format, or the URL is incorrect, or the GUID does not meet the validation requirements.";
					break;
				case Constants.UNAUTHORIZED:
					Exceptionmessage = "Authentication error (no license key header, or invalid license key).";
					break;
				case Constants.NOT_FOUND:
					Exceptionmessage = "Invalid URL.";
					break;
				case Constants.METHOD_NOT_ALLOWED:
					Exceptionmessage = "Returned if the method is an invalid or unexpected type (GET/POST/PUT/etc.).";
					break;
				case Constants.REQUEST_ENTITY_TOO_LARGE:
					Exceptionmessage = "Too many metrics were sent in one request, or too many components (instances) were specified in one request, or other single-request limits were reached.";
					break;
				case Constants.INTERNAL_SERVER_ERROR:
					Exceptionmessage = "Unexpected server error";
					break;
				case Constants.BAD_GATEWAY:
					Exceptionmessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					break;
				case Constants.SERVICE_UNAVAIBLE:
					Exceptionmessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					break;
				case Constants.GATEWAY_TIMEOUT:
					Exceptionmessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					break;

			}
			if (Exceptionmessage != null)
				throw new NewRelicException(Exceptionmessage);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		http.closeHttpClient();


	}

	@Override
	public void run() {
		try {
			sendStatsToNewRelic();
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
