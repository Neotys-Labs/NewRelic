package com.neotys.newrelic.infrastucture;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.google.common.base.Optional;
import com.neotys.action.argument.Arguments;
import com.neotys.action.argument.Option;
import com.neotys.extensions.action.Action;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.newrelic.Constants;
import com.neotys.newrelic.NewRelicOption;


public final class NewRelicAction implements Action{
	
	private static final ImageIcon LOGO_ICON;
	static {
		final URL iconURL = NewRelicAction.class.getResource(Constants.CUSTOM_ACTION_ICON);
		if (iconURL != null) {
			LOGO_ICON = new ImageIcon(iconURL);
		}
		else {
			LOGO_ICON = null;
		}
	}

	@Override
	public String getType() {
		return Constants.CUSTOM_ACTION_TYPE;
	}

	@Override
	public List<ActionParameter> getDefaultActionParameters() {
		final List<ActionParameter> parameters = new ArrayList<>();

		for (final NewRelicOption option : NewRelicOption.values()) {
			if (Option.AppearsByDefault.True.equals(option.getAppearsByDefault())) {
				parameters.add(new ActionParameter(option.getName(), option.getDefaultValue(),
						option.getType()));
			}
		}

		return parameters;
	
	}

	@Override
	public Class<? extends ActionEngine> getEngineClass() {
		return NewRelicActionEngine.class;
	}

	@Override
	public Icon getIcon() {
		return LOGO_ICON;
	}
	@Override
	public String getDescription() {
		final StringBuilder description = new StringBuilder();
		// TODO validate description
		description.append("NewRelicMonitoring Action will retrieve all the counters measured by NewRelic Infrastructure\n")
				.append(Arguments.getArgumentDescriptions(NewRelicOption.values()));
		return description.toString();
	}

	@Override
	public String getDisplayName() {
		return Constants.CUSTOM_ACTION_DISPLAY_NAME;
	}

	@Override
	public String getDisplayPath() {
		return Constants.CUSTOM_ACTION_DISPLAY_PATH;
	}

	@Override
	public Optional<String> getMinimumNeoLoadVersion() {
		return Optional.of("6.3");
	}

	@Override
	public Optional<String> getMaximumNeoLoadVersion() {
		return Optional.absent();
	}
}
