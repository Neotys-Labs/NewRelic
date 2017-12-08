# NewRelic Integration

## Overview

This Advanced Action allows you to integrate [NeoLoad](https://www.neotys.com/neoload/overview) with NewRelic Plugin or NewRelic Insights to be able to correlate the graphs from both tools. 

The NewRelicMonitoring Advanced Action:

* **NewRelic -> NeoLoad**: Retrieve metrics of the SUT from NewRelic and inject them in NeoLoad (external data)   to correlate graphs in NeoLoad’s Dashboard
* **NeoLoad -> New Relic Plugin**: Send the Global Statistics of a NeoLoad test to NewRelic Plugin to correlate load testing data in NewRelic.
* **NeoLoad -> New Relic Insight**: Send Global Statistics of a NeoLoad test and the values of NeoLoad’s Transactions to NewRelic Insights to correlate load testing data in NewRelic Insight.


| Property          | Value             |
| ----------------    | ----------------   |
| Maturity           | Experimental |
| Author             | Neotys Partner Team |
| License           | [BSD Simplified](https://www.neotys.com/documents/legal/bsd-neotys.txt) |
| NeoLoad         | 6.1 (Enterprise or Professional Edition w/ Integration & Advanced Usage and NeoLoad Web option required)|
| Requirements | <ul><li>NeoLoad Web SaaS subscription</li><li>NewRelic account with Infrastructure, Plugins, Insights (Optional)</li></ul>|
| Bundled in NeoLoad | No |
| Download Binaries    | See the [latest release](https://github.com/Neotys-Labs/NewRelic/releases/latest)


## Installation

1. Download the [latest release](https://github.com/Neotys-Labs/NewRelic/releases/latest)
1. Read the NeoLoad documentation to see [How to install a custom Advanced Action](https://www.neotys.com/documents/doc/neoload/latest/en/html/#25928.htm)


## Set-up

Once installed, how to use in a given NeoLoad project:

1. Create a User Path “NewRelic”
1. Insert NewRelicMonitoring in the ‘End’ block.
1. Create a Population “NewRelic” that contains 100% of User Path “Dynatrace”
1. In the Runtime section, select your scenario, select the “NewRelic” population and define a constant load of 1 user.
## Parameters

Tip: Get NeoLoad API information in NeoLoad preferences: Project Preferences / REST API.

| Name                     | Description       |
| ---------------          | ----------------- |
| NewRelic_APIKEY          |  NewRelic API key |
| NewRelic_ApplicationName | New Relic’sName of the application monitored by NewRelic |
| NeoLoadAPIHost           | IP address or Host of the NeoLoad DataExchange API |
| NeoLoadAPIport           | Port of the NL DataExchange API |
| NeoLoadKeyAPI            | NeoLoad API Key |
| ENABLE_NEWRELIC_PLUGIN   | If “true”, this Action sends NeoLoad statistics to NewRelic and retrieves NewRelic statistics. Otherwise, this Action retrieves NewRelic statistics only.  |
| NewRelic_License_Key     | NewRelic plugin License Key    |
| Insight_AccountID        | (Optional) Newrelic Insights account id. If not specified, NeoLoad statistics are not sent to NewRelic Insight | 
| Insight_ApiKey           | (Optional) NewRelic Insights API Key |


## Status Codes

* NLNewRelicInfraStructureMonitoring_ERROR: Issue while monitoring NewRelic


