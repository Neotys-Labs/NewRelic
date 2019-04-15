package com.neotys.newrelic.rest;

import com.google.common.collect.Multimap;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import static com.neotys.newrelic.rest.HTTPGeneratorUtils.convertStreamToString;
import static com.neotys.newrelic.rest.HTTPGeneratorUtils.isJsonContent;


public class HttpResponseUtils {

	public static boolean isSuccessHttpCode(final int httpCode) {
		return httpCode >= HttpStatus.SC_OK
				&& httpCode <= HttpStatus.SC_MULTI_STATUS;
	}

	public static String getStringResponse(final HttpResponse resp) throws IOException {
		final HttpEntity entity = resp.getEntity();
		if (entity != null) {
			// A Simple JSON Response Read
			try (final InputStream inputStream = entity.getContent()) {
				return convertStreamToString(inputStream);
			}
		}
		return null;
	}

	public static JSONArray getJsonArrayResponse(final HttpResponse httpResponse) throws IOException {
		if (isJsonContent(httpResponse)) {
			final String stringResponse = getStringResponse(httpResponse);
			if (stringResponse != null) {
				return new JSONArray(stringResponse);
			}
		}
		return null;
	}

	public static JSONObject getJsonResponse(final HttpResponse response) throws IOException {
		if (isJsonContent(response)) {
			final String stringResponse = getStringResponse(response);
			if (stringResponse != null) {
				return new JSONObject(stringResponse);
			}
		}
		return null;
	}

	public static boolean getNextPageParams(final HttpResponse response, final Multimap<String, String> params) {
		// Remove previous values because this is a multimap
		params.clear();

		Header[] headers = response.getHeaders("Link");
		if (headers.length == 0) return false;

		String[] links = headers[0].getValue().split(",");
		for (String link : links) {
			// We try with these two strings because the first response from REST API contains double quotes but the other ones don't
			if(link.endsWith("rel=\"next\"") || link.endsWith("rel=next")) {
				// Retrieve the exact URL
				link = link.replaceAll("(^<)|(>; rel=\"?next\"?)$", "");

				// Verify that there is at least one page after
				if(link.endsWith("cursor=")) return false;

				// Extract the parameters
				String[] parameters = link.split("\\?")[1].split("&");

				for (String parameter : parameters) {
					String[] keyValuePair = parameter.split("=");

					params.put(keyValuePair[0], keyValuePair[1]);
				}

				if (params.containsKey("cursor")) return true;
			}
		}

		return false;
	}
}
