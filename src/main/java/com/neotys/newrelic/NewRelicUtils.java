package com.neotys.newrelic;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpStatus;

import java.util.Optional;
import com.google.common.base.Strings;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import io.swagger.client.ApiClient;

/**
 * Created by anouvel on 06/02/2018.
 */
public class NewRelicUtils {

	private NewRelicUtils() {

	}

	public static Optional<Proxy> getProxy(final Context context, final Optional<String> proxyName, final String url) throws MalformedURLException {
		if (proxyName.isPresent()) {
			return Optional.ofNullable(context.getProxyByName(proxyName.get(), new URL(url)));
		}
		return Optional.empty();
	}

	// FIXME should be in common with dynatrace Advanced action.
	public static void initProxyForNeoloadWebApiClient(final ApiClient neoloadWebApiClient, final Proxy proxy)
			throws KeyManagementException, NoSuchAlgorithmException {
		neoloadWebApiClient.getHttpClient().setProxy(toOkHttpProxy(proxy));
		if (!Strings.isNullOrEmpty(proxy.getLogin())) {
			Authenticator proxyAuthenticator = new Authenticator() {
				@Override
				public Request authenticate(java.net.Proxy p, Response response) throws IOException {
					final String credential = Credentials.basic(proxy.getLogin(), proxy.getPassword());
					return response.request().newBuilder().header("Proxy-Authorization", credential).build();
				}

				@Override
				public Request authenticateProxy(java.net.Proxy p, Response response) throws IOException {
					final String credential = Credentials.basic(proxy.getLogin(), proxy.getPassword());
					return response.request().newBuilder().header("Proxy-Authorization", credential).build();
				}
			};
			neoloadWebApiClient.getHttpClient().setAuthenticator(proxyAuthenticator);
		}
		// Create a trust manager that does not validate certificate chains
		final TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					@Override
					public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
					}

					@Override
					public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
					}

					@Override
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return new java.security.cert.X509Certificate[0];
					}
				}
		};

		// Install the all-trusting trust manager
		final SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, trustAllCerts, null);
		// Create an ssl socket factory with our all-trusting manager
		final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

		neoloadWebApiClient.getHttpClient().setSslSocketFactory(sslSocketFactory);
		neoloadWebApiClient.getHttpClient().setHostnameVerifier((hostname, session) -> true);
	}

	private static java.net.Proxy toOkHttpProxy(final Proxy proxy) {
		return new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxy.getHost(), proxy.getPort()));
	}

	public static String getExceptionmessage(final int httpcode) {
		switch (httpcode) {
			case HttpStatus.SC_BAD_REQUEST:	return "The request or headers are in the wrong format, or the URL is incorrect, or the GUID does not meet the validation requirements.";
			case HttpStatus.SC_FORBIDDEN: return "Authentication error (no license key header, or invalid license key).";
			case HttpStatus.SC_NOT_FOUND: return "Invalid URL.";
			case HttpStatus.SC_METHOD_NOT_ALLOWED: return "Returned if the method is an invalid or unexpected type (GET/POST/PUT/etc.).";				
			case HttpStatus.SC_REQUEST_TOO_LONG: return "Too many metrics were sent in one request, or too many components (instances) were specified in one request, or other single-request limits were reached.";
			case HttpStatus.SC_INTERNAL_SERVER_ERROR: return "Unexpected server error";
			case HttpStatus.SC_BAD_GATEWAY: return "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";				
			case HttpStatus.SC_SERVICE_UNAVAILABLE: return "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
			case HttpStatus.SC_GATEWAY_TIMEOUT: return "All 50X errors mean there is a transient problem in the server completing requests, and no data has been retained. Clients are expected to resend the data after waiting one minute. The data should be aggregated appropriately, combining multiple timeslice data values for the same metric into a single aggregate timeslice data value.";
		}
		return null;
	}
}
