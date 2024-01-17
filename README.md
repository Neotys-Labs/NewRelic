<p align="center"><img src="/screenshots/NewRelic-logo.png" width="40%" alt="New Relic Logo" /></p>

# New Relic Integration for NeoLoad

## Overview

This Advanced Action allows you to integrate [NeoLoad](https://www.tricentis.com/products/performance-testing-neoload) with [New Relic](https://newrelic.com/) in order to correlate data from one tool to another. 

This bundle provides both an inbound and an outbound integration:
* **Inbound (New Relic &rarr; NeoLoad)**: Retrieves metrics of the SUT from New Relic and injects them in NeoLoad Controller through the [Data Exchange API](https://documentation.tricentis.com/neoload/latest/en/content/apis/data_exchange_api.htm). This allows the correlation of load performance and APM results from the NeoLoad's [Dashboards](https://documentation.tricentis.com/nlweb/latest/en/content/reference_guide/dashboards.htm). Since NeoLoad GUI 6.8 version, these metrics are sent to NeoLoad Web.
* **Outbound (NeoLoad Web &rarr; New Relic)**: With load testing data in New Relic, a tester can build complex dashboards correlating several types of metrics.
   * Retrieves the NeoLoad [Main Statistics](https://documentation.tricentis.com/nlweb/latest/en/content/views_and_features/overview_indicators_panel.htm) from NeoLoad Web (Indicators of the Test overview) and injects them to the [New Relic Plugins](https://newrelic.com/plugins) [API](https://docs.newrelic.com/docs/plugins/plugin-developer-resources/developer-reference/work-directly-plugin-api) and  [New Relic Insights](https://newrelic.com/insights) [API](https://docs.newrelic.com/docs/insights/insights-data-sources/custom-data/insert-custom-events-insights-api).
   * Retrieves the NeoLoad [Transaction values](https://documentation.tricentis.com/nlweb/latest/en/content/views_and_features/values_tab.htm) from NeoLoad Web and injects them to [New Relic Insights](https://newrelic.com/insights) [API](https://docs.newrelic.com/docs/insights/insights-data-sources/custom-data/insert-custom-events-insights-api).



| Property | Value |
| ----------------    | ----------------   |
| Maturity | Stable |
| Author | Neotys |
| License           | [BSD Simplified](https://www.neotys.com/documents/legal/bsd-neotys.txt) |
| NeoLoad         | From version 6.3|
| Requirements | <ul><li>License FREE edition, or Enterprise edition, or Professional with Integration & Advanced Usage</li><li>New Relic account with Infrastructures and Plugins</li></ul>|
| Optionals | <ul><li>NeoLoad Web SaaS subscription (for option to send data from NeoLoad Web to New Relic)</li><li>New Relic account with Insights</li></ul>|
| Bundled in NeoLoad | No |
| Download Binaries    |  <ul><li>[latest release](https://github.com/Neotys-Labs/NewRelic/releases/latest) is only compatible with NeoLoad from version 6.7</li><li> Use this [release](https://github.com/Neotys-Labs/NewRelic/releases/tag/1.0.2) for previous NeoLoad versions</li></ul>|


## Installation

1. Download the [latest release](https://github.com/Neotys-Labs/NewRelic/releases/latest) for NeoLoad from version 6.7 or this [release](https://github.com/Neotys-Labs/NewRelic/releases/tag/1.0.2) for previous NeoLoad versions.
1. Read the NeoLoad documentation to see [How to install a custom Advanced Action](https://documentation.tricentis.com/neoload/latest/en/content/reference_guide/manage_advanced_actions.htm).

<p align="center"><img src="/screenshots/new_relic_advanced_action.png" alt="New Relic Advanced Action" /></p>

## Set-up

Once installed, how to use in a given NeoLoad project:

1. Create a User Path "New Relic".
2. Insert Custom action "New Relic Monitoring" in the **Actions** container (custom action is inside Advanced > APM > New Relic).

<p align="center"><img src="/screenshots/new_relic_user_path.png" alt="New Relic User Path" /></p>

3. Select the **Actions** container and set a pacing duration of 60 seconds.

<p align="center"><img src="/screenshots/actions_container_pacing.png" alt="Action's Pacing" /></p>

4. Select the **Actions** container and set the runtime parameters "Reset user session and emulate new browser between each iteration" to "No".

<p align="center"><img src="/screenshots/actions_container_reset_iteration_no.png" alt="Action's Runtime parameters" /></p>

5. Create a Population "PopulationNewRelic" which contains 100% of User Path "New Relic".

<p align="center"><img src="/screenshots/new_relic_population.png" alt="New Relic Population" /></p>

6. In the **Runtime** section, select your scenario, select the "PopulationNewRelic" population and define a constant load of 1 user for the full duration of the load test.

<p align="center"><img src="/screenshots/new_relic_load_variation_policy.png" alt="Load Variation Policy" /></p>

7. Do not use multiple load generators. Good practice should be to keep only the local one.

8. Verify to have a license with "Integration & Advanced Usage".

<p align="center"><img src="/screenshots/license_integration_and_advanced_usage.png" alt="License with Integration & Advanced Usage" /></p>

9. On the New Relic APM interface, create (or retrieve) a New Relic API key from menu **Account settings**, section **INTEGRATIONS**, subsection **API keys**.

<p align="center"><img src="/screenshots/new_relic_api_key.png" alt="New Relic API key" /></p>

10. On the New Relic APM interface, find out the name of the application being tested.

<p align="center"><img src="/screenshots/new_relic_application_name.png" alt="New Relic Application name" /></p>

## Optional Set-up 

If you use option to send data from NeoLoad Web to New Relic, follow the steps below: 

1. Verify that NeoLoad Web data transfer is properly configured on the Controller preferences (see **Preferences** / **General settings** / **NeoLoad Web**).

<p align="center"><img src="/screenshots/nlweb_preferences.png" alt="NeoLoad Web Preferences" /></p>

2. On the New Relic interface, retrieve the New Relic License Key from menu **Account settings**, section **Account information**. 

<p align="center"><img src="/screenshots/new_relic_license_key.png" alt="New Relic License Key" /></p>

3. On the New Relic interface, retrieve the New Relic Account Id, displayed in the URL.
 
<p align="center"><img src="/screenshots/new_relic_account_id.png" alt="New Relic Account Id" /></p>

4. On the New Relic interface, create (or retrieve) a New Relic Insights API key from menu **Insights**, section **Manage data**, subsection **API Keys**.

<p align="center"><img src="/screenshots/new_relic_insights_api_key.png" alt="New Relic Insights API key" /></p>

## Parameters

| Name                     | Description       | Required/Optional|
| ---------------          | ----------------- |----------------- |
| newRelicAPIKey          |  New Relic API key. List of New Relic API keys are defined on New Relic menu **Account settings**, section **INTEGRATIONS**, subsection **API keys**. |Required|
| newRelicApplicationName          | New Relic application name. List of New Relic application names are on New Relic menu **APM**.  |Required|
| sendNLWebDataToNewRelic | When set to 'true', sends NeoLoad Web data to New Relic (requires NeoLoad Web module). When set to 'false', only retrieves data from New Relic. The Check User Path mode only works when value is 'false', as there is no NeoLoad Web interaction. |Optional|
| newRelicLicenseKey | The New Relic license key to send data. Required when argument 'sendNLWebDataToNewRelic' is true.  |Optional|
| newRelicAccountId | The New Relic Account Id. It appears in the URL when going on New Relic menu **Account settings** 'https://rpm.newrelic.com/accounts/<accountId>'. Required when argument 'sendNLWebDataToNewRelic' is true. |Optional|
| newRelicInsightsAPIKey | The New Relic Insights API key. List of New Relic Insights API keys are defined on New Relic menu **Insights**, section **Manage data**, subsection **API Keys**. Required when argument 'sendNLWebDataToNewRelic' is true.  |Optional|
| dataExchangeApiUrl          | Where the DataExchange server is located. Optional, by default it is: http://${NL-ControllerIp}:7400/DataExchange/v1/Service.svc/ |Optional|
| dataExchangeApiKey | Identification key specified in NeoLoad. |Optional|
| proxyName | The NeoLoad proxy name to access New Relic. |Optional|
| newRelicRelevantMetricNames | Specify the comma separated list of the matching names (contains) of the New Relic metric names to retrieve (by default: Datastore/statement, Datastore/instance, CPU/main, Memory, Error/, connects).|Optional|
| newRelicRelevantMetricValues | Specify the comma separated list of the matching names (contains) of the New Relic metric statistics to retrieve (by default: min,max,average,used_mb,percent). |Optional|
| tls.insecure | By default set to false, it only accept TLS valid certificates. Set to true to ignore TLS certificates issues. |Optional|


<p align="center"><img src="/screenshots/parameters.png" alt="New Relic Monitoring Advanced Action Parameters" /></p>

## Analyse results in NeoLoad

All the metrics retrieved from New Relic are available on the NeoLoad Controller (live during the test, and after the test is executed), in the **External Data** tab.

<p align="center"><img src="/screenshots/neoload_external_data_graphs.png" alt="NeoLoad Graphs External Data" /></p>

## Analyse results in New Relic Plugins

The Performance Dashboard provides statistics below: 
* User Load
* Transaction (Success, Count, Failure)
* Request (Success, Count, Failure)
* DownLoaded Bytes
* Average Transaction Response Time
* Average Request Response Time

<p align="center"><img src="/screenshots/new_relic_plugins1.png" alt="New Relic Monitoring Advanced Action Parameters" /></p>

<p align="center"><img src="/screenshots/new_relic_plugins2.png" alt="New Relic Monitoring Advanced Action Parameters" /></p>


## Analyse results in New Relic Insights
In addition to their core APM product, New Relic offers New Relic Insights, which provides a wide variety of methods to analyse and present custom, real-time data visualizations. New Relic offers the ability to build precise dashboards with the help of their New Relic Query (NQR) language. Any data measured by New Relic (APM, Infrastructure, RUM, or Synthetic) can be accessed using NRQ. 

NeoLoad sends statistics in two Insights tables: "NeoLoadData" and "NeoLoadValues"

### NeoLoadData Insights table

These data are the [Main Statistics](https://documentation.tricentis.com/nlweb/latest/en/content/views_and_features/overview_indicators_panel.htm) from NeoLoad Web. They are retrieved from [NeoLoad Web API](https://neoload-api.saas.neotys.com/explore/index.html) with REST call "/tests/{testId}/statistics".

NeoLoadData table has all the global statistics of the test:

| Name                               | Unit    | Description  |
| --------------------------------   | ------- | ------------ |
| Timestamp                          | Date    |   |
| Metric Unit                        | -       | Always empty |
| Account                            | -       | ID of the NewRelic account  |
| App ID                             | -       | ID of the application  |
| Application Name                   | -       |   |
| Scenario Name                      | -       | Name of the Neoload scenario  |
| Test Name                          | -       | Name of the Neoload test result  |
| transactionInstantDuration         | Second  | Average transaction duration at this timestamp  |
| transactionSuccess                 | Count   | Transaction in success since the previous event  |
| transactionFailure                 | Count   | Transaction in failure since the previous event  |
| transactionCount                   | Transaction/Second | Number of transaction per second from start of test  |
| transactionAverageDuration         | Second  | Average transaction duration from start of test  |
| downloadedBytes                    | Bytes   | Downloaded bytes since previous event |
| downloadedBytesPerSecond           | Bytes/Second | Downloaded bytes per second from start of test  |
| userLoad                           | Count   | User load at this timestamp  |
| iterationFailure                   | Count   | Iteration in failure since the previous event  |
| iterationSuccess                   | Count   | Iteration in success since the previous event  |
| globalCountFailure                 | Count   | Number of failure since previous event |
| requestInstantRate                 | Request/Second | Request rate at this timestamp  |
| requestInstantDuration             | Second  | Last request duration at this timestamp  |
| requestSuccess                     | Count   | Request in success since the previous event  |
| requestFailure                     | Count   | Request in failure since the previous event  |
| requestCount                       | Request/Second | Number of request per second from start  |

### NeoLoadValues Insights table

These data are the [Transaction values](https://documentation.tricentis.com/nlweb/latest/en/content/views_and_features/values_tab.htm) from NeoLoad Web. They are retrieved from [NeoLoad Web API](https://neoload-api.saas.neotys.com/explore/index.html) with REST call "/tests/{testId}/elements/{elementId}/values".

NeoLoadValues table has all the Transaction values of the test:
* Timestamp
* Account
* App ID
* Application Name
* Downloaded Bytes Per Second
* Element Per Second
* Path
* Response Time
* Scenario Name
* Test Name
* Transaction Name
* Trendfield
* Type
* User Path Name

### How to build New Relic Insights dashboards ?

See New Relic [Documentation](https://docs.newrelic.com/docs/insights/use-insights-ui/manage-dashboards/create-edit-copy-insights-dashboards).

<p align="center"><img src="/screenshots/new_relic_insights_dashboard.png" alt="New Relic Insights" /></p>

## NeoLoad Error Codes
* NL-NEW_RELIC_ACTION-01: Issue while parsing advanced action arguments.
* NL-NEW_RELIC_ACTION-02: Technical Error. See details for more information.
* NL-NEW_RELIC_ACTION-03: A NeoLoad Web test should be running when argument 'sendNLWebDataToNewRelic' is true.
* NL-NEW_RELIC_ACTION-04: Not enough delay between the two executions of the New Relic advanced action. Make sure to have at least 60 seconds pacing on the Actions container.  

## ChangeLog
* Version 2.2.0 (March 2023): For security purpose, do not ignore TLS certificates issues (use parameter tls.insecure for backward compatibility).
* Version 2.1.0 (April 15, 2019): 
    * The number of default counters to return has been decreased.
    * Bug fix: The Advanced Action can now retrieve all the existing metric names.
* Version 1.0.2 (April 23, 2018): Update Apache HttpClient version and set as provided.
* Version 1.0.1 (March 1, 2018): Ability to send data to New Relic Plugins only (without New Relic Insights).
* Version 1.0.0 (February 27, 2018): Initial release.
