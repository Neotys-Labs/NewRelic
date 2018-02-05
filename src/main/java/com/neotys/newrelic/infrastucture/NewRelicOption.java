package com.neotys.newrelic.infrastucture;

import com.neotys.action.argument.ArgumentValidator;
import com.neotys.action.argument.Option;
import com.neotys.extensions.action.ActionParameter;

import static com.neotys.action.argument.DefaultArgumentValidator.ALWAYS_VALID;
import static com.neotys.action.argument.DefaultArgumentValidator.BOOLEAN_VALIDATOR;
import static com.neotys.action.argument.DefaultArgumentValidator.NON_EMPTY;
import static com.neotys.action.argument.Option.AppearsByDefault.False;
import static com.neotys.action.argument.Option.AppearsByDefault.True;
import static com.neotys.action.argument.Option.OptionalRequired.Optional;
import static com.neotys.action.argument.Option.OptionalRequired.Required;
import static com.neotys.extensions.action.ActionParameter.Type.TEXT;

/**
 * Created by anouvel on 05/02/2018.
 */
public enum NewRelicOption implements Option{
	NewRelicApiKey("newRelicApiKey", Required, True, TEXT,
			"NewRelic APIKEY",
			"NewRelic API Key",
			NON_EMPTY),
	NewRelicApplicationName("newRelicApplicationName", Required, True, TEXT,
			"ApplicationName",
			"Application name",
			NON_EMPTY),
	EnableNewRelicPlugin("enableNewRelicPlugin", Required, True, TEXT,
			"false",
			"To enable the NewRelic Plugin. Possible values are true and false.",
			BOOLEAN_VALIDATOR),
	NewRelicLicenseKey("newRelicLicenseKey", Optional, False, TEXT,
			"",
			"The NewRelic License Key is required when enableNewRelicPlugin is true.",
			ALWAYS_VALID),
	InsightAccountId("insightAccountId", Optional, False, TEXT,
			"",
			"The Insight account ID is required when enableNewRelicPlugin is true.",
			ALWAYS_VALID),
	InsightApiKey("insightApiKey", Optional, False, TEXT,
			"",
			"The Insight API key is required when enableNewRelicPlugin is true.",
			ALWAYS_VALID),
	NeoLoadDataExchangeApiUrl("dataExchangeApiUrl", Required, True, TEXT,
			"http://localhost:7400/DataExchange/v1/Service.svc/",
			"Where the DataExchange server is located. Typically the NeoLoad controller.",
			NON_EMPTY),

	NeoLoadDataExchangeApiKey("dataExchangeApiKey", Optional, True, TEXT,
			"",
			"Identification key specified in NeoLoad.",
			ALWAYS_VALID),

	NeoLoadProxy("proxyName", Optional, False, TEXT,
			"",
			"The NeoLoad proxy name to access Dynatrace.",
			ALWAYS_VALID);
	;
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
