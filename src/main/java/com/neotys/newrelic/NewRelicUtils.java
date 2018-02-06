package com.neotys.newrelic;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Proxy;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import io.swagger.client.ApiClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Created by anouvel on 06/02/2018.
 */
public class NewRelicUtils {

	private NewRelicUtils(){

	}

	public static Optional<Proxy> getProxy(final Context context, final Optional<String> proxyName, final String url) throws MalformedURLException {
		if (proxyName.isPresent()) {
			return Optional.fromNullable(context.getProxyByName(proxyName.get(), new URL(url)));
		}
		return Optional.absent();
	}

	// FIXME should be in common with dynatrace Advanced action.
	public static void initProxyForNeoloadWebApiClient(final ApiClient neoloadWebApiClient, final Proxy proxy) throws KeyManagementException, NoSuchAlgorithmException {
		neoloadWebApiClient.getHttpClient().setProxy(toOkHttpProxy(proxy));
		if (!Strings.isNullOrEmpty(proxy.getLogin())) {
			Authenticator proxyAuthenticator = new Authenticator() {
				@Override
				public Request authenticate(java.net.Proxy p, Response response) throws IOException {
					final String credential = Credentials.basic(proxy.getLogin(), proxy.getPassword());
					return response.request().newBuilder()
							.header("Proxy-Authorization", credential)
							.build();
				}

				@Override
				public Request authenticateProxy(java.net.Proxy p, Response response) throws IOException {
					final String credential = Credentials.basic(proxy.getLogin(), proxy.getPassword());
					return response.request().newBuilder()
							.header("Proxy-Authorization", credential)
							.build();
				}
			};
			neoloadWebApiClient.getHttpClient().setAuthenticator(proxyAuthenticator);
		}
		// Create a trust manager that does not validate certificate chains
		final TrustManager[] trustAllCerts = new TrustManager[]{
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
}
