package com.neotys.newrelic.infrastucture;

import com.google.common.base.Optional;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import com.neotys.newrelic.Constants;
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
	private final Optional<String> proxyName;	
	
	private final String newRelicLicenseKey;
	private final String componentsName;	
	private final String nrInsightApiKey;
	private final String nrInsightAccountId;
	private final String nlTestName;
	private final String testId;
	private final String nrApplicationId;
	private final String nlScenarioName;
	private final String nrApplicationName;
	private final ResultsApi nlWebResult;
		
	private Map<String, String> headers = null;
	private NLGlobalStat nlStat;
	private HTTPGenerator http;
	
	private void initHttpClient() {
		headers = new HashMap<>();
		headers.put("X-License-Key", newRelicLicenseKey);
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json");

	}

	public NeoLoadStatAggregator(final String newRelicLicenseKey, 
			final ResultsApi nlWebResult, 
			final String testId, 
			final NLGlobalStat nlStat, 
			final String nrInsightAccountId, 
			final String nrInsightApiKey, 
			final String Testname, 
			final String applicationName, 
			final String apiKey, 
			final String ScenarioName, 
			final Context neoloadContext, 
			final Optional<String> proxyName) throws NewRelicException, IOException {
		this.neoloadContext = neoloadContext;
		this.proxyName = proxyName;
		this.componentsName = "Statistics";
		this.newRelicLicenseKey = newRelicLicenseKey;
		this.nlStat = nlStat;
		this.testId = testId;
		this.nlWebResult = nlWebResult;
		this.nrApplicationId = getApplicationID(applicationName, apiKey);
		this.nlScenarioName = ScenarioName;
		this.nlTestName = Testname;
		this.nrInsightAccountId = nrInsightAccountId;
		this.nrInsightApiKey = nrInsightApiKey;
		this.nrApplicationName = applicationName;
		initHttpClient();
	}

	private void sendStatsToNewRelic() throws ApiException {
		TestStatistics StatsResult;
		long utc;
		long lasduration;

		utc = System.currentTimeMillis() / 1000;

		lasduration = nlStat.getLasduration();

		if (lasduration == 0 || (utc - lasduration) >= Constants.MIN_NEW_RELIC_DURATION) {


			StatsResult = nlWebResult.getTestStatistics(testId);


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

	public String getApplicationID(String ApplicaitoNAme, String APIKEY) throws NewRelicException, IOException {
		JSONObject jsoobj;
		String Url;
		JSONArray jsonApplication;
		Map<String, String> Parameters = null;
		Map<String, String> head = null;

		HTTPGenerator ApplicationAPI;
		String newRelicApplicationID = null;

		head = new HashMap<>();
		head.put("X-Api-Key", APIKEY);
		head.put("Content-Type", "application/json");
		Url = Constants.NEW_RELIC_URL + Constants.APPLICATIONS_JSON;
		Parameters = new HashMap<>();
		Parameters.put("filter[name]", ApplicaitoNAme);

		final com.google.common.base.Optional<Proxy> proxy = getProxy(neoloadContext, proxyName, Url);
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
		ArrayOfElementDefinition NLElement;
		ElementValues Values;
		List<Metric> NlValues = new ArrayList<>();
		try {


			NLElement = nlWebResult.getTestElements(testId, Constants.NLWEB_TRANSACTION);
			for (ElementDefinition element : NLElement) {
				if (element.getType().equalsIgnoreCase("TRANSACTION")) {
					Values = nlWebResult.getTestElementsValues(testId, element.getId());
					NlValues.add(new Metric(element, Values));
				}
			}

			for (Metric val : NlValues)
				sendValueMetricToInsightAPI(val.getElementValue());


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendValueMetricToInsightAPI(String[] data) throws NewRelicException {
		int httpcode;
		final Map<String, String> head = new HashMap<>();
		HTTPGenerator Insight_HTTP = null;


		head.put("X-Insert-Key", nrInsightApiKey);
		head.put("Content-Type", "application/json");
		String URL;
		long ltime = System.currentTimeMillis();

		URL = Constants.NEW_RELIC_INSIGHT_URL + nrInsightAccountId + "/events";
		String Exceptionmessage = null;

		String JSON_String = "[{\"eventType\":\"NeoLoadValues\","
				+ "\"account\" : \"" + nrInsightAccountId + "\","
				+ "\"appId\" : \"" + nrApplicationId + "\","
				+ "\"testName\" : \"" + nlTestName + "\","
				+ "\"scenarioName\" : \"" + nlScenarioName + "\","
				+ "\"applicationName\" : \"" + nrApplicationName + "\","
				+ "\"trendfield\": \"" + nrApplicationName + nlScenarioName + nlTestName + "\","
				+ "\"userPathName\" :\"" + data[1] + "\","
				+ "\"type\" :\"" + data[6] + "\","
				+ "\"transactionName\" :\"" + data[0] + "\","
				+ "\"path\" :\"" + data[2] + "\","
				+ "\"responseTime\":" + data[3] + ","
				+ "\"elementPerSecond\":" + data[5] + ","
				+ "\"downloadedBytesPerSecond\":" + data[4] + ","
				+ "\"timestamp\" : " + ltime + "}]";


		try {
			final com.google.common.base.Optional<Proxy> proxy = getProxy(neoloadContext, proxyName, URL);
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

		head.put("X-Insert-Key", nrInsightApiKey);
		head.put("Content-Type", "application/json");
		String URL;
		long ltime = System.currentTimeMillis();

		URL = Constants.NEW_RELIC_INSIGHT_URL + nrInsightAccountId + "/events";
		String Exceptionmessage = null;

		String JSON_String = "[{\"eventType\":\"NeoLoadData\","
				+ "\"account\" : \"" + nrInsightAccountId + "\","
				+ "\"appId\" : \"" + nrApplicationId + "\","
				+ "\"testName\" : \"" + nlTestName + "\","
				+ "\"scenarioName\" : \"" + nlScenarioName + "\","
				+ "\"applicationName\" : \"" + nrApplicationName + "\","
				+ "\"trendfield\": \"" + nrApplicationName + nlScenarioName + nlTestName + "\",";

		for (String[] metric : data) {
			JSON_String += "\"" + metric[1] + "\" : " + metric[3] + ",";

		}
		JSON_String += "\"MetricUnit\" : \"\","
				+ "\"timestamp\" : " + ltime + "}]";


		try {
			final com.google.common.base.Optional<Proxy> proxy = getProxy(neoloadContext, proxyName, URL);
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
			final com.google.common.base.Optional<Proxy> proxy = getProxy(neoloadContext, proxyName, Constants.NEW_RELIC_PLATFORM_API_URL);
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
