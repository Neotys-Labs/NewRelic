package com.neotys.newrelic;

import com.neotys.action.argument.ArgumentValidator;
import com.neotys.action.argument.Option;
import com.neotys.extensions.action.ActionParameter;

import static com.neotys.action.argument.DefaultArgumentValidator.BOOLEAN_VALIDATOR;
import static com.neotys.action.argument.DefaultArgumentValidator.NON_EMPTY;
import static com.neotys.action.argument.Option.AppearsByDefault.False;
import static com.neotys.action.argument.Option.AppearsByDefault.True;
import static com.neotys.action.argument.Option.OptionalRequired.Optional;
import static com.neotys.action.argument.Option.OptionalRequired.Required;
import static com.neotys.extensions.action.ActionParameter.Type.*;

/**
 * Created by anouvel on 05/02/2018.
 */
public enum NewRelicOption implements Option {
	// Required
	NewRelicAPIKey("newRelicAPIKey", Required, True, PASSWORD, "", "New Relic API key. List of New Relic API keys are defined on New Relic menu Account settings, section INTEGRATIONS, subsection API keys.", NON_EMPTY),
	NewRelicApplicationName("newRelicApplicationName", Required, True, TEXT, "", "New Relic application name. List of New Relic application names are on New Relic menu APM.", NON_EMPTY),
	NeoLoadDataExchangeApiUrl("dataExchangeApiUrl", Required, True, TEXT, "http://localhost:7400/DataExchange/v1/Service.svc/", "The URL of the DataExchange server (located on the NeoLoad Controller).", NON_EMPTY),

	// Optional, visible by default
	SendNLWebDataToNewRelic("sendNLWebDataToNewRelic", Optional, True, TEXT, "true", "When set to 'true', sends NeoLoad Web data to New Relic (requires NeoLoad Web module). When set to 'false', only retrieves data from New Relic.", BOOLEAN_VALIDATOR),
	NewRelicLicenseKey("newRelicLicenseKey", Optional, True, PASSWORD, "", "The New Relic license key. Required when argument 'sendNLWebDataToNewRelic' is true.", NON_EMPTY),
	NewRelicAccountId("newRelicAccountId", Optional, True, TEXT, "", "The New Relic Account Id. It appears in the URL when going on New Relic menu Account settings 'https://rpm.newrelic.com/accounts/<accountId>'. Required when argument 'sendNLWebDataToNewRelic' is true.", NON_EMPTY),
	NewRelicInsightsAPIKey("newRelicInsightsAPIKey", Optional, True, TEXT, "", "The New Relic Insights API key. List of New Relic Insights API keys are defined on New Relic menu Insights, section 'Manage data', subsection API Keys. Required when argument 'sendNLWebDataToNewRelic' is true.", NON_EMPTY),

	// Optional, not visible by default	
	NeoLoadDataExchangeApiKey("dataExchangeApiKey", Optional, False, PASSWORD, "", "Identification key specified in NeoLoad.", NON_EMPTY),
	NeoLoadProxy("proxyName", Optional, False, TEXT, "", "The NeoLoad proxy name to access New Relic.", NON_EMPTY);

	private final String name;
	private final Option.OptionalRequired optionalRequired;
	private final Option.AppearsByDefault appearsByDefault;
	private final ActionParameter.Type type;
	private final String defaultValue;
	private final String description;
	private final ArgumentValidator argumentValidator;

	NewRelicOption(final String name, final Option.OptionalRequired optionalRequired,
			final Option.AppearsByDefault appearsByDefault,
			final ActionParameter.Type type, final String defaultValue, final String description,
			final ArgumentValidator argumentValidator) {
		this.name = name;
		this.optionalRequired = optionalRequired;
		this.appearsByDefault = appearsByDefault;
		this.type = type;
		this.defaultValue = defaultValue;
		this.description = description;
		this.argumentValidator = argumentValidator;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Option.OptionalRequired getOptionalRequired() {
		return optionalRequired;
	}

	@Override
	public Option.AppearsByDefault getAppearsByDefault() {
		return appearsByDefault;
	}
	
	@Override
	public ActionParameter.Type getType() {
		return type;
	}
	
	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public ArgumentValidator getArgumentValidator() {
		return argumentValidator;
	}
}
