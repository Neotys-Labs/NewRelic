package com.neotys.NewRelic.NewRelicInfrastructureAction;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.Context;

import org.mockito.Mockito;

public class NewRelicInfraStructureMonitoringActionTest {
	@Test
	public void shouldReturnType() {
		final NewRelicInfraStructureMonitoringAction action = new NewRelicInfraStructureMonitoringAction();
		assertEquals("NewRelicInfraStructureMonitoring", action.getType());
	}

	/*@Test
	public void TestAPI()
	{
		final NewRelicInfraStructureMonitoringActionEngine action = new NewRelicInfraStructureMonitoringActionEngine();
		List<ActionParameter> parameters = new ArrayList<>();
		parameters.add(new ActionParameter("NewRelic_APIKEY","c03644706a684a25dc9cd3ff197a6590cee243098a8eb90"));
		parameters.add(new ActionParameter("NewRelic_ApplicationName","PHP Application"));
		parameters.add(new ActionParameter("NeoLoadAPIHost","localhost"));
		parameters.add(new ActionParameter("NeoLoadAPIport","7400"));
		parameters.add(new ActionParameter("NeoLoadKeyAPI",""));
		parameters.add(new ActionParameter("NeoLoadLocation","Gemenos"));
	//	parameters.add(new ActionParameter("HTTP_PROXY_HOST","127.0.0.1"));
	//	parameters.add(new ActionParameter("HTTP_PROXY_PORT","8888"));

		action.execute(Mockito.mock(Context.class), parameters);
		
		
	}*/
	/*@Test
	public void TestSendingAPI()
	{
		final NewRelicInfraStructureMonitoringActionEngine action = new NewRelicInfraStructureMonitoringActionEngine();
		List<ActionParameter> parameters = new ArrayList<>();
		parameters.add(new ActionParameter("NewRelic_APIKEY","b61192dfa2321eff7be739cf2d21b549"));
		parameters.add(new ActionParameter("NewRelic_ApplicationName","PHP Application"));
		parameters.add(new ActionParameter("NeoLoadAPIHost","localhost"));
		parameters.add(new ActionParameter("NeoLoadAPIport","7400"));
		parameters.add(new ActionParameter("NeoLoadKeyAPI",""));
		parameters.add(new ActionParameter("NeoLoadLocation","Gemenos"));
	//	parameters.add(new ActionParameter("HTTP_PROXY_HOST","127.0.0.1"));
	//	parameters.add(new ActionParameter("HTTP_PROXY_PORT","8888"));
		parameters.add(new ActionParameter("NewRelic_License_Key","158b7534e1f3aa37fed0970d19918afe28a8eb90"));
		parameters.add(new ActionParameter("NeoLoadWEB_API_KEY","15304f743f34ca33c458927a40945b7424a2066b95563774"));
		parameters.add(new ActionParameter("ENABLE_NEWRELIC_PLUGIN","true"));
		parameters.add(new ActionParameter("ProjectName","Ushahidi"));
		parameters.add(new ActionParameter("Insight_AccountID","1552023"));
		parameters.add(new ActionParameter("Insight_ApiKey","BHln4r9MEKdEetTeqgJfcY4S-qo-4YwE"));
		
		action.execute(Mockito.mock(Context.class), parameters);
		
		
	}*/
}
