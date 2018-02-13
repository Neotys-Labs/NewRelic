package com.neotys.newrelic.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.neotys.extensions.action.engine.Proxy;

public class HTTPGenerator {

	private DefaultHttpClient httpClient;
	private String httpMethod;
	private String url;

	private HttpRequestBase request;
	private int StatusCode = 0;

	public HTTPGenerator(final String url,
						 final String method,
						 final Map<String, String> headers,
						 final Map<String, String> params,
						 final Optional<Proxy> proxy) {

		httpMethod = method;
		this.url = url;
		try {

			request = generateHTTPRequest(this.url);
			request = generateHeaders(headers, request);
			if (params != null && !params.isEmpty()) {
				if (httpMethod != "GET")
					request.setParams(generateParams(params));
				else {
					this.url = addGetParametersToUrl(url, params);
					request.setURI(new URL(this.url).toURI());
				}
			}
			if (this.url.contains("https")) {
				DefaultHttpClient Client = new DefaultHttpClient();
				HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

				SchemeRegistry registry = new SchemeRegistry();
				SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
				socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
				registry.register(new Scheme("https", socketFactory, 443));
				ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager(Client.getParams(), registry);
				httpClient = new DefaultHttpClient(mgr, Client.getParams());
				HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
			} else {
				DefaultHttpClient Client = new DefaultHttpClient();
				ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager(Client.getParams(), httpClient.getConnectionManager().getSchemeRegistry());
				httpClient = new DefaultHttpClient(mgr, Client.getParams());

			}

			if (proxy.isPresent()) {
				initProxy(proxy.get());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public HTTPGenerator(final String url, final Map<String, String> headers, final String jsonString, final Optional<Proxy> proxy) throws UnsupportedEncodingException {
		httpMethod = HttpPost.METHOD_NAME;
		StringEntity requestEntity = new StringEntity(jsonString, "application/json", "UTF-8");
		this.url = url;
		try {

			request = generateHTTPRequest(this.url);
			request = generateHeaders(headers, request);
			((HttpPost) request).setEntity(requestEntity);

			if (this.url.contains("https")) {
				DefaultHttpClient Client = new DefaultHttpClient();
				HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

				SchemeRegistry registry = new SchemeRegistry();
				SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
				socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
				registry.register(new Scheme("https", socketFactory, 443));
				ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager(Client.getParams(), registry);
				httpClient = new DefaultHttpClient(mgr, Client.getParams());
				HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

			} else {
				DefaultHttpClient Client = new DefaultHttpClient();
				ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager(Client.getParams(), httpClient.getConnectionManager().getSchemeRegistry());
				httpClient = new DefaultHttpClient(mgr, Client.getParams());
			}
			if (proxy.isPresent()) {
				initProxy(proxy.get());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void initProxy(final Proxy proxy) {
		final HttpHost proxyHttpHost = new HttpHost(proxy.getHost(), proxy.getPort(), "http");
		httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHttpHost);
		if (Strings.isNullOrEmpty(proxy.getLogin())) {
			httpClient.getCredentialsProvider().setCredentials(
					new AuthScope(proxy.getHost(), proxy.getPort()),
					new UsernamePasswordCredentials(proxy.getLogin(), proxy.getPassword()));
		}
	}

	private static HttpParams generateParams(final Map<String, String> params) {
		if (params != null) {
			HttpParams result = new BasicHttpParams();
			for (Map.Entry<String, String> entry : params.entrySet()) {
				result.setParameter(entry.getKey(), entry.getValue());
			}
			return result;
		}
		return null;		
	}

	private static HttpRequestBase generateHeaders(final Map<String, String> head, final HttpRequestBase request) {
		if (head != null) {
			for (Map.Entry<String, String> entry : head.entrySet()) {
				request.setHeader(entry.getKey(), entry.getValue());
			}
		}

		return request;
	}

	private HttpRequestBase generateHTTPRequest(final String url) {
		HttpRequestBase request = null;
		switch (httpMethod) {
			case HttpGet.METHOD_NAME:
				request = new HttpGet(url);
				break;
			case HttpPost.METHOD_NAME:
				request = new HttpPost(url);
				break;
			case HttpOptions.METHOD_NAME:
				break;
			case HttpPut.METHOD_NAME:
				request = new HttpPut(url);
				break;

		}
		return request;
	}

	public void closeHttpClient() {
		httpClient.getConnectionManager().shutdown();
	}

	private static String addGetParametersToUrl(String url, final Map<String, String> params) {

		if (!url.endsWith("?"))
			url += "?";

		List<NameValuePair> parameters = new LinkedList<>();

		if (params != null) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));

			}
		}

		String paramString = URLEncodedUtils.format(parameters, HTTP.UTF_8);

		url += paramString;
		return url;
	}

	public void setAllowHostnameSSL() {
		SSLSocketFactory sf = null;
		SSLContext sslContext = null;
		
		try {
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, null, null);
		} catch (NoSuchAlgorithmException e) {
			//<YourErrorHandling>
		} catch (KeyManagementException e) {
			//<YourErrorHandling>
		}

		try {
			sf = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			Scheme sch = new Scheme("https", 443, sf);
			httpClient.getConnectionManager().getSchemeRegistry().register(sch);

		} catch (Exception e) {
			//<YourErrorHandling>

		}


	}

	public JSONObject getJSONHTTPresponse() throws IOException {

		JSONObject json = null;
		final HttpResponse response = httpClient.execute(request);
		StatusCode = response.getStatusLine().getStatusCode();

		if (StatusCode == 200) {
			if (isJsonContent(response))
				json = new JSONObject(getStringResponse(response));

		}
		EntityUtils.consume(response.getEntity());
		response.getEntity().getContent().close();
		return json;


	}

	public int getHttpResponseCodeFromResponse() throws IOException {
		final HttpResponse response = httpClient.execute(request);

		StatusCode = response.getStatusLine().getStatusCode();
		EntityUtils.consume(response.getEntity());
		response.getEntity().getContent().close();

		return StatusCode;
	}

	private static String convertStreamToString(final InputStream is) {

		final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		final StringBuilder sb = new StringBuilder();

		String line ;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}


	public boolean isJsonContent(final HttpResponse resp) {
		boolean result = false;
		Header contentTypeHeader = resp.getFirstHeader("Content-Type");
		if (contentTypeHeader.getValue().contains("application/json")) {
			result = true;
		}

		return result;
	}

	public String getStringResponse(final HttpResponse resp) {
		String result = null;
		try {

			HttpEntity entity = resp.getEntity();

			if (entity != null) {

				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				result = convertStreamToString(instream);			

				instream.close();
				if (resp.getStatusLine().getStatusCode() != 200) {
					return null;
				}

			}


		} catch (ClientProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return result;
	}
}
