# New Relic Integration

## Overview

This Advanced Action allows you to integrate [NeoLoad](https://www.neotys.com/neoload/overview) with [New Relic](https://newrelic.com/) in order to correlate data from one tool to another. 

It has 3 capabilities: 
* **Retrive New Relic data and inject them into NeoLoad**: Retrieve metrics of the SUT from New Relic and inject them in NeoLoad Controller through the [Data Exchange API](https://www.neotys.com/documents/doc/neoload/latest/en/html/#7676.htm). This allows the correlation of load performance and APM results from the NeoLoad's [Dashboards](https://www.neotys.com/documents/doc/neoload/latest/en/html/#1440.htm).
* **NeoLoad Web -> New Relic Plugins**: Retrieve the NeoLoad [Main Statistics](https://www.neotys.com/documents/doc/nlweb/latest/en/html/#22968.htm) from NeoLoad Web and inject them to the New Relic [Plugins API](https://docs.newrelic.com/docs/plugins/plugin-developer-resources/developer-reference/work-directly-plugin-api). This allows the correlation of load performance and APM results from New Relic [Plugins](https://newrelic.com/plugins).
* **NeoLoad Web -> New Relic Insights**: Retrieve the [Main Statistics](https://www.neotys.com/documents/doc/nlweb/latest/en/html/#22968.htm) and the [Transaction values](https://www.neotys.com/documents/doc/nlweb/latest/en/html/#26321.htm) from NeoLoad Web and inject them to the New Relic [Insights API](https://docs.newrelic.com/docs/insights/insights-data-sources/custom-data/insert-custom-events-insights-api). This allows the correlation of load performance and APM results from New Relic [Insights](https://newrelic.com/insights).

| Property | Value |
| ----------------    | ----------------   |
| Maturity | Stable |
| Author | Neotys R&D team |
| License           | [BSD Simplified](https://www.neotys.com/documents/legal/bsd-neotys.txt) |
| NeoLoad         | From version 6.3|
| Requirements | <ul><li>License FREE edition, or Enterprise edition, or Professional with Integration & Advanced Usage)</li><li>New Relic account with Infrastructures and Plugins</li></ul>|
| Optionals | <ul><li>NeoLoad Web SaaS subscription (for option to send data from NeoLoad Web to New Relic)</li><li>New Relic account with Insights</li></ul>|
| Bundled in NeoLoad | No |
| Download Binaries    | See the [latest release](https://github.com/Neotys-Labs/NewRelic/releases/latest)|


## Installation

1. Download the [latest release](https://github.com/Neotys-Labs/NewRelic/releases/latest)
1. Read the NeoLoad documentation to see [How to install a custom Advanced Action](https://www.neotys.com/documents/doc/neoload/latest/en/html/#25928.htm)

<p align="center"><img src="/screenshots/new_relic_advanced_action.png" alt="New Relic Advanced Action" /></p>

## Set-up

Once installed, how to use in a given NeoLoad project:

1. Create a User Path "New Relic".
2. Insert Custom action "New Relic Monitoring" in the "Actions" container (custom action is inside Advanced > APM > New Relic).

<p align="center"><img src="/screenshots/new_relic_user_path.png" alt="New Relic User Path" /></p>

3. Select the "Actions" container and set a pacing duration of 60 seconds.

<p align="center"><img src="/screenshots/actions_container_pacing.png" alt="Action's Pacing" /></p>

4. Select the "Actions" container and set the runtime parameters "Reset user session and emulate new browser between each iteration" to "No".

<p align="center"><img src="/screenshots/actions_container_reset_iteration_no.png" alt="Action's Runtime parameters" /></p>

5. Create a Population "New Relic" which contains 100% of User Path "New Relic".

<p align="center"><img src="/screenshots/new_relic_population.png" alt="New Relic Population" /></p>

6. In the Runtime section, select your scenario, select the "New Relic" population and define a constant load of 1 user for the full duration of the load test.

<p align="center"><img src="/screenshots/new_relic_load_variation_policy.png" alt="Load Variation Policy" /></p>

7. Verify to have a license with "Integration & Advanced Usage".

<p align="center"><img src="/screenshots/license_integration_and_advanced_usage.png" alt="License with Integration & Advanced Usage" /></p>

8. [Optional] If you use option to send data from NeoLoad Web to New Relic, verify that NeoLoad Web data transfer is properly configured on the Controller preferences (see Preferences / General settings / NeoLoad Web).

<p align="center"><img src="/screenshots/nlweb_preferences.png" alt="NeoLoad Web Preferences" /></p>

## Parameters

| Name                     | Description       | Required/Optional|
| ---------------          | ----------------- |----------------- |
| newRelicAPIKey          |  New Relic API key. List of New Relic API keys are defined on New Relic menu Account settings, section INTEGRATIONS, subsection API keys. |Required|
| newRelicApplicationName          | New Relic application name. List of New Relic application names are on New Relic menu APM.  |Required|
| dataExchangeApiUrl          | The URL of the DataExchange server (located on the NeoLoad Controller).  |Required|
| sendNLWebDataToNewRelic | When set to 'true', sends NeoLoad Web data to New Relic (requires NeoLoad Web module). When set to 'false', only retrieves data from New Relic.  |Optional|
| newRelicLicenseKey | The New Relic license key. Required when argument 'sendNLWebDataToNewRelic' is true.  |Optional|
| newRelicAccountId | The New Relic Account Id. It appears in the URL when going on New Relic menu Account settings 'https://rpm.newrelic.com/accounts/<accountId>'. Required when argument 'sendNLWebDataToNewRelic' is true. |Optional|
| newRelicInsightsAPIKey | The New Relic Insights API key. List of New Relic Insights API keys are defined on New Relic menu Insights, section 'Manage data', subsection API Keys. Required when argument 'sendNLWebDataToNewRelic' is true.  |Optional|
| dataExchangeApiKey | "Identification key specified in NeoLoad."  |Optional|
| proxyName | The NeoLoad proxy name to access New Relic. |Optional|
| newRelicRelevantMetricNames | "The list of relevant metric names to monitor from New Relic."  |Optional|
| newRelicRelevantMetricValues | "The list of relevant metric values to monitor from New Relic."  |Optional|

<p align="center"><img src="/screenshots/parameters.png" alt="New Relic Monitoring Advanced Action Parameters" /></p>

Tip: Get NeoLoad API information in NeoLoad preferences: Project Preferences / [REST API](https://www.neotys.com/documents/doc/neoload/latest/en/html/#7652.htm).

## NeoLoad Error Codes
* NL-NEW_RELIC_ACTION-01: Issue while parsing advanced action arguments.
* NL-NEW_RELIC_ACTION-02: Technical Error. See details for more information.
* NL-NEW_RELIC_ACTION-03: A NeoLoad Web test should be running when argument 'sendNLWebDataToNewRelic' is true.
* NL-NEW_RELIC_ACTION-04: Not enough delay between the two New Relic advanced action execution. Make sure to have at least 60 seconds pacing on the Actions container.  


