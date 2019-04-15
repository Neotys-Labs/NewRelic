package com.neotys.newrelic.rest;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class HttpResponseUtilsTest {
    private final static String firstLink = "<https://api.newrelic.com/v2/applications/${APP_ID}/metrics.json?name=View&page=2&cursor=a1pHUmtaR1JiR1krYmc9PQ%3D%3D>; rel=\"next\", \n" +
            "<https://api.newrelic.com/v2/applications/${APP_ID}/metrics.json?name=View&page=3>; rel=\"last\"";
    private final static String secondLink = "<https://api.newrelic.com/v2/applications/12345/metrics.json>; rel=first, \n" +
            "<https://api.newrelic.com/v2/applications/12345/metrics.json?cursor=REF3TURBd05ibURTOHc9PQ%3D%3D>; rel=next";
    private final static String thirdLink = "<https://api.newrelic.com/v2/applications/12345/metrics.json>; rel=first, \n" +
            "<https://api.newrelic.com/v2/applications/12345/metrics.json?cursor=>; rel=next";

    @Test
    void testGetNextPageParams(){
        Multimap<String, String> params = ArrayListMultimap.create();

        // Extract the parameters from the first link
        final BasicHttpResponse firstResponse = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "SUCCESS");
        firstResponse.setHeader("Link", firstLink);

        Assert.assertTrue(HttpResponseUtils.getNextPageParams(firstResponse, params));
        Assert.assertEquals("2", params.get("page").toArray()[0]);
        Assert.assertEquals("a1pHUmtaR1JiR1krYmc9PQ%3D%3D", params.get("cursor").toArray()[0]);
        Assert.assertEquals("View", params.get("name").toArray()[0]);

        // Extract the parameters from the second link
        params.clear();
        final BasicHttpResponse secondResponse = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "SUCCESS");
        secondResponse.setHeader("Link", secondLink);

        Assert.assertTrue(HttpResponseUtils.getNextPageParams(secondResponse, params));
        Assert.assertTrue(params.get("page").isEmpty());
        Assert.assertEquals("REF3TURBd05ibURTOHc9PQ%3D%3D", params.get("cursor").toArray()[0]);
        Assert.assertTrue(params.get("name").isEmpty());

        // Extract the parameters from the third link
        params.clear();
        final BasicHttpResponse thirdResponse = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "SUCCESS");
        thirdResponse.setHeader("Link", thirdLink);

        // Be aware that the call must return false value because there is no more page after the current one
        Assert.assertFalse(HttpResponseUtils.getNextPageParams(thirdResponse, params));
        Assert.assertTrue(params.get("page").isEmpty());
        Assert.assertTrue(params.get("cursor").isEmpty());
        Assert.assertTrue(params.get("name").isEmpty());
    }
}
