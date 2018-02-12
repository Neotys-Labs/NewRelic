package com.neotys.newrelic.infrastucture;

import com.google.common.base.Optional;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
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
	private static final int MIN_NEW_RELIC_DURATION = 30;
	private static final String NewRelicURL = "https://api.newrelic.com/v2/";
	private static final String NewRelicApplicationAPI = "applications.json";
	private static final String NEW_RELIC_PLUG_URL = "https://platform-api.newrelic.com/platform/v1/metrics";
	private static String NEW_RELIC_INSIGHT_URL = "https://insights-collector.newrelic.com/v1/accounts/";
	private static final String NLGUID = "com.neotys.NeoLoad.plugin";
	private static final String NLWEB_TRANSACTION = "TRANSACTION";
	private static final String NLWEB_PAGE = "PAGE";
	private static final String NLWEB_REQUEST = "REQUEST";
	private static final int BAD_REQUEST = 400;
	private static final int UNAUTHORIZED = 403;
	private static final int NOT_FOUND = 404;
	private static final int METHOD_NOT_ALLOWED = 405;
	private static final int REQUEST_ENTITY_TOO_LARGE = 413;
	private static final int INTERNAL_SERVER_ERROR = 500;
	private static final int BAD_GATEWAY = 502;
	private static final int SERVICE_UNAVAIBLE = 503;
	private static final int GATEWAY_TIMEOUT = 504;
	private static final int HTTP_RESPONSE = 200;
	private static final String VERSION = "1.0.0";


	private final Context neoloadContext;
	private final Optional<String> proxyName;
	private Map<String, String> headers = null;
	private HTTPGenerator http;
	private String NewRElicLicenseKey;
	private String ComponentsName;
	NLGlobalStat NLStat;
	private String Insight_APIKEY;
	private String Insight_AccountID;
	private String TestName;
	private final String TestID;
	private String NewRelicAPplicationID;
	private String NLScenarioName;
	private String ApplicationNAme;
	ResultsApi NLWEBresult;


	private void InitHttpClient() {
		headers = new HashMap<>();
		headers.put("X-License-Key", NewRElicLicenseKey);
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json");

	}

	public NeoLoadStatAggregator(String pNewRElicLicenseKeyY, ResultsApi pNLWEBresult, String pTestID, NLGlobalStat pNLStat, String I_AccountID, String I_APIKEY, String Testname, String ApplicationName, String ApIKEY, String ScenarioName, final Context neoloadContext, final Optional<String> proxyName) throws NewRelicException, IOException {
		this.neoloadContext = neoloadContext;
		this.proxyName = proxyName;
		ComponentsName = "Statistics";
		NewRElicLicenseKey = pNewRElicLicenseKeyY;
		NLStat = pNLStat;
		TestID = pTestID;
		NLWEBresult = pNLWEBresult;
		NewRelicAPplicationID = GetApplicationID(ApplicationName, ApIKEY);
		NLScenarioName = ScenarioName;
		TestName = Testname;
		Insight_AccountID = I_AccountID;
		Insight_APIKEY = I_APIKEY;
		ApplicationNAme = ApplicationName;
		InitHttpClient();
	}

	private void SendStatsToNewRelic() throws ApiException {
		TestStatistics StatsResult;
		long utc;
		long lasduration;

		utc = System.currentTimeMillis() / 1000;

		lasduration = NLStat.getLasduration();

		if (lasduration == 0 || (utc - lasduration) >= MIN_NEW_RELIC_DURATION) {


			StatsResult = NLWEBresult.getTestStatistics(TestID);


			lasduration = SendData(StatsResult, lasduration);
			NLStat.setLasduration(lasduration);

			SendValuesToNewRelic();
		}
	}

	public long SendData(TestStatistics stat, long LasDuration) {
		int time = 0;
		List<String[]> data;
		long utc;
		utc = System.currentTimeMillis() / 1000;

		if (NLStat == null)
			NLStat = new NLGlobalStat(stat);
		else {
			NLStat.UpdateStat(stat);
		}
		data = NLStat.GetNLData();
		if (LasDuration == 0)
			time = 0;
		else {
			time = (int) (utc - LasDuration);
		}
		for (String[] metric : data) {
			try {
				SendMetricToPluginAPi(metric[0], metric[1], time, metric[2], metric[3]);

			} catch (NewRelicException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {

			SendMetricToInsightAPI(data, time);
		} catch (NewRelicException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return utc;
	}

	public String GetApplicationID(String ApplicaitoNAme, String APIKEY) throws NewRelicException, IOException {
		JSONObject jsoobj;
		String Url;
		JSONArray jsonApplication;
		Map<String, String> Parameters = null;
		Map<String, String> head = null;

		HTTPGenerator ApplicationAPI;
		String NewRelicApplicationID = null;

		head = new HashMap<>();
		head.put("X-Api-Key", APIKEY);
		head.put("Content-Type", "application/json");
		Url = NewRelicURL + NewRelicApplicationAPI;
		Parameters = new HashMap<String, String>();
		Parameters.put("filter[name]", ApplicaitoNAme);

		final com.google.common.base.Optional<Proxy> proxy = getProxy(neoloadContext, proxyName, Url);
		ApplicationAPI = new HTTPGenerator(Url, "GET", head, Parameters, proxy);


		jsoobj = ApplicationAPI.getJSONHTTPresponse();
		if (jsoobj != null) {
			if (jsoobj.has("applications")) {
				jsonApplication = jsoobj.getJSONArray("applications");
				NewRelicApplicationID = String.valueOf(jsonApplication.getJSONObject(0).getInt("id"));
				if (NewRelicApplicationID == null)
					throw new NewRelicException("No Application find in The NewRelic Account");
			} else
				NewRelicApplicationID = null;
		} else
			NewRelicApplicationID = null;

		return NewRelicApplicationID;

	}

	private void SendValuesToNewRelic() throws ApiException {
		ArrayOfElementDefinition NLElement;
		ElementValues Values;
		List<NLGlobalValues> NlValues = new ArrayList<>();
		try {


			NLElement = NLWEBresult.getTestElements(TestID, NLWEB_TRANSACTION);
			for (ElementDefinition element : NLElement) {
				if (element.getType().equalsIgnoreCase("TRANSACTION")) {
					Values = NLWEBresult.getTestElementsValues(TestID, element.getId());
					NlValues.add(new NLGlobalValues(element, Values));
				}
			}

			for (NLGlobalValues val : NlValues)
				SendValueMetricToInsightAPI(val.GetElementValue());


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void SendValueMetricToInsightAPI(String[] data) throws NewRelicException {
		int httpcode;
		final Map<String, String> head = new HashMap<>();
		HTTPGenerator Insight_HTTP = null;


		head.put("X-Insert-Key", Insight_APIKEY);
		head.put("Content-Type", "application/json");
		String URL;
		long ltime = System.currentTimeMillis();

		URL = NEW_RELIC_INSIGHT_URL + Insight_AccountID + "/events";
		String Exceptionmessage = null;

		String JSON_String = "[{\"eventType\":\"NeoLoadValues\","
				+ "\"account\" : \"" + Insight_AccountID + "\","
				+ "\"appId\" : \"" + NewRelicAPplicationID + "\","
				+ "\"testName\" : \"" + TestName + "\","
				+ "\"scenarioName\" : \"" + NLScenarioName + "\","
				+ "\"applicationName\" : \"" + ApplicationNAme + "\","
				+ "\"trendfield\": \"" + ApplicationNAme + NLScenarioName + TestName + "\","
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

				case BAD_REQUEST:
					Exceptionmessage = "The request or headers are in the wrong format, or the URL is incorrect, or the GUID does not meet the validation requirements.";
					break;
				case UNAUTHORIZED:
					Exceptionmessage = "Authentication error (no license key header, or invalid license key).";
					break;
				case NOT_FOUND:
					Exceptionmessage = "Invalid URL.";
					break;
				case METHOD_NOT_ALLOWED:
					Exceptionmessage = "Returned if the method is an invalid or unexpected type (GET/POST/PUT/etc.).";
					break;
				case REQUEST_ENTITY_TOO_LARGE:
					Exceptionmessage = "Too many metrics were sent in one request, or too many components (instances) were specified in one request, or other single-request limits were reached.";
					break;
				case INTERNAL_SERVER_ERROR:
					Exceptionmessage = "Unexpected server error";
					break;
				case BAD_GATEWAY:
					Exceptionmessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					break;
				case SERVICE_UNAVAIBLE:
					Exceptionmessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					break;
				case GATEWAY_TIMEOUT:
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

	private void SendMetricToInsightAPI(List<String[]> data, int time) throws NewRelicException {
		int httpcode;
		final Map<String, String> head = new HashMap<>();
		HTTPGenerator Insight_HTTP = null;

		head.put("X-Insert-Key", Insight_APIKEY);
		head.put("Content-Type", "application/json");
		String URL;
		long ltime = System.currentTimeMillis();

		URL = NEW_RELIC_INSIGHT_URL + Insight_AccountID + "/events";
		String Exceptionmessage = null;

		String JSON_String = "[{\"eventType\":\"NeoLoadData\","
				+ "\"account\" : \"" + Insight_AccountID + "\","
				+ "\"appId\" : \"" + NewRelicAPplicationID + "\","
				+ "\"testName\" : \"" + TestName + "\","
				+ "\"scenarioName\" : \"" + NLScenarioName + "\","
				+ "\"applicationName\" : \"" + ApplicationNAme + "\","
				+ "\"trendfield\": \"" + ApplicationNAme + NLScenarioName + TestName + "\",";

		for (String[] metric : data) {
			JSON_String += "\"" + metric[1] + "\" : " + metric[3] + ",";

		}
		JSON_String += "\"MetricUnit\" : \"\","
				+ "\"timestamp\" : " + ltime + "}]";


		try {
			final com.google.common.base.Optional<Proxy> proxy = getProxy(neoloadContext, proxyName, URL);
			Insight_HTTP = new HTTPGenerator(URL, head, JSON_String, proxy);
			httpcode = Insight_HTTP.getHttpResponseCodeFromResponse();
			switch (httpcode) {

				case BAD_REQUEST:
					Exceptionmessage = "The request or headers are in the wrong format, or the URL is incorrect, or the GUID does not meet the validation requirements.";
					break;
				case UNAUTHORIZED:
					Exceptionmessage = "Authentication error (no license key header, or invalid license key).";
					break;
				case NOT_FOUND:
					Exceptionmessage = "Invalid URL.";
					break;
				case METHOD_NOT_ALLOWED:
					Exceptionmessage = "Returned if the method is an invalid or unexpected type (GET/POST/PUT/etc.).";
					break;
				case REQUEST_ENTITY_TOO_LARGE:
					Exceptionmessage = "Too many metrics were sent in one request, or too many components (instances) were specified in one request, or other single-request limits were reached.";
					break;
				case INTERNAL_SERVER_ERROR:
					Exceptionmessage = "Unexpected server error";
					break;
				case BAD_GATEWAY:
					Exceptionmessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					break;
				case SERVICE_UNAVAIBLE:
					Exceptionmessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					break;
				case GATEWAY_TIMEOUT:
					Exceptionmessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					break;

			}
			if (Exceptionmessage != null)
				throw new NewRelicException(Exceptionmessage);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Insight_HTTP.closeHttpClient();
		}
	}

	private void SendMetricToPluginAPi(String MetricName, String MetricPath, int Duration, String unit, String Value) throws NewRelicException {
		int httpcode;
		String Exceptionmessage = null;

		String JSON_String = "{\"agent\":{"
				+ "\"host\" : \"" + NLGUID + "\","
				+ "\"version\" : \"" + VERSION + "\""
				+ "},"
				+ "\"components\": ["
				+ "{"
				+ "\"name\": \"NeoLoad\","
				+ "\"guid\": \"" + NLGUID + "\","
				+ " \"duration\" : " + String.valueOf(Duration) + ","
				+ " \"metrics\" : {"
				+ " \"Component/NeoLoad/" + ComponentsName + "/" + MetricPath + "[" + unit + "]\": " + Value + ""
				+ "}"
				+ "}"
				+ "]"
				+ "}";

		try {
			final com.google.common.base.Optional<Proxy> proxy = getProxy(neoloadContext, proxyName, NEW_RELIC_PLUG_URL);
			http = new HTTPGenerator(NEW_RELIC_PLUG_URL, headers, JSON_String, proxy);
			httpcode = http.getHttpResponseCodeFromResponse();
			switch (httpcode) {

				case BAD_REQUEST:
					Exceptionmessage = "The request or headers are in the wrong format, or the URL is incorrect, or the GUID does not meet the validation requirements.";
					break;
				case UNAUTHORIZED:
					Exceptionmessage = "Authentication error (no license key header, or invalid license key).";
					break;
				case NOT_FOUND:
					Exceptionmessage = "Invalid URL.";
					break;
				case METHOD_NOT_ALLOWED:
					Exceptionmessage = "Returned if the method is an invalid or unexpected type (GET/POST/PUT/etc.).";
					break;
				case REQUEST_ENTITY_TOO_LARGE:
					Exceptionmessage = "Too many metrics were sent in one request, or too many components (instances) were specified in one request, or other single-request limits were reached.";
					break;
				case INTERNAL_SERVER_ERROR:
					Exceptionmessage = "Unexpected server error";
					break;
				case BAD_GATEWAY:
					Exceptionmessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					break;
				case SERVICE_UNAVAIBLE:
					Exceptionmessage = "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
					break;
				case GATEWAY_TIMEOUT:
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
			SendStatsToNewRelic();
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
