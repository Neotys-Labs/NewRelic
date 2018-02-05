package com.neotys.newrelic.infrastucture;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.google.common.base.Optional;
import com.neotys.extensions.action.Action;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;


public final class NewRelicAction implements Action{
	static final String NeoLoadAPIHost="NeoLoadAPIHost";
	static final String NeoLoadAPIport="NeoLoadAPIport";
	static final String NeoLoadKeyAPI="NeoLoadKeyAPI";
	static final String NewRelic_APIKEY="NewRelic_APIKEY";
	static final String NewRelic_ApplicationName="NewRelic_ApplicationName";
	static final String HTTP_PROXY_HOST="HTTP_PROXY_HOST";
	static final String HTTP_PROXY_PORT="HTTP_PROXY_PORT";
	static final String HTTP_PROXY_LOGIN="HTTP_PROXY_LOGIN";
	static final String HTTP_PROXY_PASSWORD="HTTP_PROXY_PASSWORD";
	static final String NewRelic_License_Key="NewRelic_License_Key";
	static final String ENABLE_NEWRELIC_PLUGIN="ENABLE_NEWRELIC_PLUGIN";
	static final String Insight_AccountID="Insight_AccountID";
	static final String Insight_ApiKey="Insight_ApiKey";
	private static final String BUNDLE_NAME = "com.neotys.NewRelic.NewRelicInfrastructureAction.bundle";
	private static final String DISPLAY_NAME = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("displayName");
	private static final String DISPLAY_PATH = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("displayPath");

	@Override
	public String getType() {
		return "NewRelicInfraStructureMonitoring";
	}

	@Override
	public List<ActionParameter> getDefaultActionParameters() {
		final List<ActionParameter> parameters = new ArrayList<ActionParameter>();
		// TODO Add default parameters.
		//API key
		parameters.add(new ActionParameter(NewRelic_APIKEY,"NewRelic APIKEY"));
		parameters.add(new ActionParameter(NewRelic_ApplicationName,"ApplicationName"));
		parameters.add(new ActionParameter(NeoLoadAPIHost,"localhost"));
		parameters.add(new ActionParameter(NeoLoadAPIport,"7400"));
		parameters.add(new ActionParameter(NeoLoadKeyAPI,""));
		parameters.add(new ActionParameter(ENABLE_NEWRELIC_PLUGIN, "false"));
		return parameters;
	
	}

	@Override
	public Class<? extends ActionEngine> getEngineClass() {
		return NewRelicActionEngine.class;
	}

	@Override
	public Icon getIcon() {
		// TODO Add an icon
		return LOGO_ICON;
	}
	private static final ImageIcon LOGO_ICON;
	static {
		final URL iconURL = NewRelicAction.class.getResource("newrelic.png");
		if (iconURL != null) {
			LOGO_ICON = new ImageIcon(iconURL);
		}
		else {
			LOGO_ICON = null;
		}
		}	
	@Override
	public String getDescription() {
		final StringBuilder description = new StringBuilder();
		// TODO Add description
		description.append("NewRelicINfrastructureMonitoring Action will retrieve all the counters mesaured by NewRelic Infrastructure\n")
		.append("The parameters are : \n")
		.append("NewRelic_APIKEY  : NewRelic API Key\n")
		.append("NewRelic_ApplicationName  : Application name\n")
		.append("NeoLoadAPIHost : IP or Host of the NeoLaod controller\n")
		.append("NeoLoadAPIport : Port of the NeoLoad DataExchange API\n")
		.append("NeoLoadKeyAPI : Neoload DataExchange API key\n")
		.append("ENABLE_NEWRELIC_PLUGIN : TEnable the NewRelic Plugin. Value Possible true or false\n")
		.append("HTTP_PROXY_HOST : Optional - Host of the HTTP proxy\n")
		.append("HTTP_PROXY_PORT : Optional - Port of the HTTP proxy\n")
		.append("HTTP_PROXY_LOGIN : Optional - Account of the HTTP proxy\n")
		.append("HTTP_PROXY_PASSWORD :Optional - Password of the HTTP proxy\n")
		.append("NewRelic_License_Key :Optional - The NewRelic License Key is required when ENABLE_NEWRELIC_PLUGIN equals true\n")
		.append("Insight_AccountID :Optional - Insight_AccountID  Required if ENABLE_NEWRELIC_PLUGIN equals true \n")
		.append("Insight_ApiKey :Optional - Insight_ApiKey  Required if ENABLE_NEWRELIC_PLUGIN equals true \n");
		return description.toString();
	}

	@Override
	public String getDisplayName() {
		return DISPLAY_NAME;
	}

	@Override
	public String getDisplayPath() {
		return DISPLAY_PATH;
	}

	@Override
	public Optional<String> getMinimumNeoLoadVersion() {
		return Optional.of("6.1");
	}

	@Override
	public Optional<String> getMaximumNeoLoadVersion() {
		return Optional.absent();
	}
}
