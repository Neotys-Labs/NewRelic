package com.neotys.newrelic.fromnlweb;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

import io.swagger.client.model.TestStatistics;

public class NLWebStats {

	/* Transactions */
	private float totalTransactionCountPerSecond = 0;

	private long deltaTransactionCountSuccess = 0;
	private long totalTransactionCountSuccess = 0;

	private long deltaTransactionCountFailure = 0;
	private long totalTransactionCountFailure = 0;

	private float lastTransactionDurationAverage = 0;

	//	private float totalTransactionDurationAverage;


	/* Requests */
	private float lastRequestCountPerSecond = 0;
	private float totalRequestCountPerSecond = 0;

	private long deltaRequestCountSuccess = 0;
	private long totalRequestCountSuccess = 0;

	private long deltaRequestCountFailure = 0;
	private long totalRequestCountFailure = 0;

	private float totalRequestDurationAverage = 0;


	/* Throughput */
	private long deltaGlobalCountFailure = 0;
	private long totalGlobalCountFailure = 0;

	private float totalGlobalDownloadedBytesPerSecond = 0;

	private float deltaGlobalDownloadedBytes = 0;
	private float totalGlobalDownloadedBytes = 0;


	/* VU Iterations */
	private long deltaIterationCountSuccess = 0;
	private long totalIterationCountSuccess = 0;

	private long deltaIterationCountFailure = 0;
	private long totalIterationCountFailure = 0;

	/** Last value received for the total number of Virtual Users executed in the test */
	private long lastVirtualUserCount = 0;

	private Optional<Long> lastTimestamp = Optional.empty();

	private static final String LastRequestCountPerSecond_MetricName = "Last Request Count";
	private static final String LastRequestCountPerSecond_Component = "lastRequestCount";
	private static final String LastRequestCountPerSecond_Unit = "request/Second";

	private static final String LastRequestDurationAverage_MetricName = "Last Request duration";
	private static final String LastRequestDurationAverage_Component = "lastRequestduration";
	private static final String LastRequestDurationAverage_Unit = "Second";

	private static final String LastTransactionDurationAverage_MetricName = "Last Average Transaction Duration";
	private static final String LastTransactionDurationAverage_Component = "lastAverageTransactionDuration";
	private static final String LastTransactionDurationAverage_Unit = "Second";

	private static final String LastVirtualUserCount_MetricName = "Last User Load";
	private static final String LastVirtualUserCount_Component = "lastUserLoad";
	private static final String LastVirtualUserCount_Unit = "Count";

	private static final String TotalGlobalCountFailure_MetricName = "Number of Failure";
	private static final String TotalGlobalCountFailure_Component = "globalCountFailure";
	private static final String TotalGlobalCountFailure_Unit = "count";

	private static final String TotalGlobalDownloadedBytes_MetricName = "Downloaded Bytes";
	private static final String TotalGlobalDownloadedBytes_Component = "dowLoadedBytes";
	private static final String TotalGlobalDownloadedBytes_Unit = "Bytes";

	private static final String TotalGlobalDownloadedBytesPerSecond_MetricName = "Downloaded Bytes";
	private static final String TotalGlobalDownloadedBytesPerSecond_Component = "downloadedBytesPerSecond";
	private static final String TotalGlobalDownloadedBytesPerSecond_Unit = "Bytes/Second";

	private static final String TotalIterationCountFailure_MetricName = "Iteration in Failure";
	private static final String TotalIterationCountFailure_Component = "iterationFailure";
	private static final String TotalIterationCountFailure_Unit = "Count";

	private static final String TotalIterationCountSuccess_MetricName = "Iteration in Success";
	private static final String TotalIterationCountSuccess_Component = "iterationSuccess";
	private static final String TotalIterationCountSuccess_Unit = "Count";

	private static final String TotalRequestCountFailure_MetricName = "Request in Failure";
	private static final String TotalRequestCountFailure_Component = "requestFailure";
	private static final String TotalRequestCountFailure_Unit = "Count";

	private static final String TotalRequestCountPerSecond_MetricName = "Number of request";
	private static final String TotalRequestCountPerSecond_Component = "requestCount";
	private static final String TotalRequestCountPerSecond_Unit = "Request/Second";

	private static final String TotalRequestCountSuccess_MetricName = "Request in Success";
	private static final String TotalRequestCountSuccess_Component = "requestSuccess";
	private static final String TotalRequestCountSuccess_Unit = "Count";

	private static final String TotalTransactionCountFailure_MetricName = "Transaction in Failure";
	private static final String TotalTransactionCountFailure_Component = "transactionFailure";
	private static final String TotalTransactionCountFailure_Unit = "Count";

	private static final String TotalTransactionCountSucess_MetricName = "Transaction in Success";
	private static final String TotalTransactionCountSucess_Component = "transactionSuccess";
	private static final String TotalTransactionCountSucess_Unit = "Count";

	private static final String TotalTransactionCountPerSecond_MetricName = "Number of Transaction";
	private static final String TotalTransactionCountPerSecond_Component = "transactionCount";
	private static final String TotalTransactionCountPerSecond_Unit = "Transaction/Second";

	public Optional<Long> getLastTimestamp() {
		return lastTimestamp;
	}

	public void setLastTimestamp(final long lastTimestamp) {
		this.lastTimestamp = Optional.of(lastTimestamp);
	}

	public void updateTestStatistics(final TestStatistics response) {
		/* Transactions */
		this.totalTransactionCountPerSecond = response.getTotalTransactionCountPerSecond();

		this.deltaTransactionCountSuccess = response.getTotalTransactionCountSuccess() - this.totalTransactionCountSuccess;
		this.totalTransactionCountSuccess = response.getTotalTransactionCountSuccess();

		this.deltaTransactionCountFailure = response.getTotalTransactionCountFailure() - this.totalTransactionCountFailure;
		this.totalTransactionCountFailure = response.getTotalTransactionCountFailure();

		this.lastTransactionDurationAverage = response.getLastTransactionDurationAverage();


		/* Requests */
		this.lastRequestCountPerSecond = response.getLastRequestCountPerSecond();
		this.totalRequestCountPerSecond = response.getTotalRequestCountPerSecond();

		this.deltaRequestCountSuccess = response.getTotalRequestCountSuccess() - this.totalRequestCountSuccess;
		this.totalRequestCountSuccess = response.getTotalRequestCountSuccess();

		this.deltaRequestCountFailure = response.getTotalRequestCountFailure() - this.totalRequestCountFailure;
		this.totalRequestCountFailure = response.getTotalRequestCountFailure();

		this.totalRequestDurationAverage = response.getTotalRequestDurationAverage();


		/* Throughput */
		this.deltaGlobalCountFailure = response.getTotalGlobalCountFailure() - this.totalGlobalCountFailure;
		this.totalGlobalCountFailure = response.getTotalGlobalCountFailure();

		this.totalGlobalDownloadedBytesPerSecond = response.getTotalGlobalDownloadedBytesPerSecond();

		this.deltaGlobalDownloadedBytes = response.getTotalGlobalDownloadedBytes() - this.totalGlobalDownloadedBytes;
		this.totalGlobalDownloadedBytes = response.getTotalGlobalDownloadedBytes();


		/* VU Iterations */
		this.deltaIterationCountSuccess = response.getTotalIterationCountSuccess() - this.totalIterationCountSuccess;
		this.totalIterationCountSuccess = response.getTotalIterationCountSuccess();

		this.deltaIterationCountFailure = response.getTotalIterationCountFailure() - this.totalIterationCountFailure;
		this.totalIterationCountFailure = response.getTotalIterationCountFailure();

		/** Last value received for the total number of Virtual Users executed in the test */
		this.lastVirtualUserCount = response.getLastVirtualUserCount();
	}

	private String[] GetRequestrequestDuration() {
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result = new String[4];
		result[0] = LastRequestDurationAverage_MetricName;
		result[1] = LastRequestDurationAverage_Component;
		result[2] = LastRequestDurationAverage_Unit;
		result[3] = df.format(totalRequestDurationAverage);
		return result;

	}

	private String[] GetRequestCountData() {
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result = new String[4];
		result[0] = LastRequestCountPerSecond_MetricName;
		result[1] = LastRequestCountPerSecond_Component;
		result[2] = LastRequestCountPerSecond_Unit;
		result[3] = df.format(lastRequestCountPerSecond);
		return result;

	}

	private String[] GetTransactionDuractionData() {
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result = new String[4];
		result[0] = LastTransactionDurationAverage_MetricName;
		result[1] = LastTransactionDurationAverage_Component;
		result[2] = LastTransactionDurationAverage_Unit;
		result[3] = df.format(lastTransactionDurationAverage);
		return result;

	}

	private String[] GetVirtualUserCountData() {
		String[] result = new String[4];
		result[0] = LastVirtualUserCount_MetricName;
		result[1] = LastVirtualUserCount_Component;
		result[2] = LastVirtualUserCount_Unit;
		result[3] = String.valueOf(lastVirtualUserCount);
		return result;

	}

	private String[] getDeltaGlobalCountFailureData() {
		String[] result = new String[4];
		result[0] = TotalGlobalCountFailure_MetricName;
		result[1] = TotalGlobalCountFailure_Component;
		result[2] = TotalGlobalCountFailure_Unit;
		result[3] = String.valueOf(deltaGlobalCountFailure);
		return result;

	}

	public List<String[]> getNLData() {
		final List<String[]> result = new ArrayList<>();
		result.add(GetRequestCountData());
		result.add(getDeltaGlobalCountFailureData());
		result.add(GetDeltaGlobalDownloadedBytesData());
		result.add(GetTotalGlobalDownloadedBytesPerSecondData());
		result.add(GetDeltaIterationCountFailureData());
		result.add(GetDeltaIterationCountSuccessData());
		result.add(GetDeltaRequestCountFailureData());
		result.add(GetTotalRequestCountPerSecondData());
		result.add(getDeltaRequestCountSuccessData());
		result.add(GetDeltaTransactionCountFailureData());
		result.add(GetTotalTransactionCountPerSecondData());
		result.add(GetDeltaTransactionCountSucessData());
		result.add(GetTransactionDuractionData());
		result.add(GetVirtualUserCountData());
		result.add(GetRequestrequestDuration());

		return result;
	}

	private String[] GetDeltaGlobalDownloadedBytesData() {
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result = new String[4];
		result[0] = TotalGlobalDownloadedBytes_MetricName;
		result[1] = TotalGlobalDownloadedBytes_Component;
		result[2] = TotalGlobalDownloadedBytes_Unit;
		result[3] = df.format(deltaGlobalDownloadedBytes);
		return result;

	}

	private String[] GetTotalGlobalDownloadedBytesPerSecondData() {
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result = new String[4];
		result[0] = TotalGlobalDownloadedBytesPerSecond_MetricName;
		result[1] = TotalGlobalDownloadedBytesPerSecond_Component;
		result[2] = TotalGlobalDownloadedBytesPerSecond_Unit;
		result[3] = df.format(totalGlobalDownloadedBytesPerSecond);
		return result;

	}

	private String[] GetDeltaIterationCountFailureData() {
		String[] result = new String[4];
		result[0] = TotalIterationCountFailure_MetricName;
		result[1] = TotalIterationCountFailure_Component;
		result[2] = TotalIterationCountFailure_Unit;
		result[3] = String.valueOf(deltaIterationCountFailure);
		return result;

	}

	private String[] GetDeltaIterationCountSuccessData() {
		String[] result = new String[4];
		result[0] = TotalIterationCountSuccess_MetricName;
		result[1] = TotalIterationCountSuccess_Component;
		result[2] = TotalIterationCountSuccess_Unit;
		result[3] = String.valueOf(deltaIterationCountSuccess);
		return result;

	}

	private String[] GetDeltaRequestCountFailureData() {
		String[] result = new String[4];
		result[0] = TotalRequestCountFailure_MetricName;
		result[1] = TotalRequestCountFailure_Component;
		result[2] = TotalRequestCountFailure_Unit;
		result[3] = String.valueOf(deltaRequestCountFailure);
		return result;

	}

	private String[] GetTotalRequestCountPerSecondData() {
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result = new String[4];
		result[0] = TotalRequestCountPerSecond_MetricName;
		result[1] = TotalRequestCountPerSecond_Component;
		result[2] = TotalRequestCountPerSecond_Unit;
		result[3] = df.format(totalRequestCountPerSecond);
		return result;

	}

	private String[] getDeltaRequestCountSuccessData() {
		String[] result = new String[4];
		result[0] = TotalRequestCountSuccess_MetricName;
		result[1] = TotalRequestCountSuccess_Component;
		result[2] = TotalRequestCountSuccess_Unit;
		result[3] = String.valueOf(deltaRequestCountSuccess);
		return result;

	}

	private String[] GetDeltaTransactionCountFailureData() {
		String[] result = new String[4];
		result[0] = TotalTransactionCountFailure_MetricName;
		result[1] = TotalTransactionCountFailure_Component;
		result[2] = TotalTransactionCountFailure_Unit;
		result[3] = String.valueOf(deltaTransactionCountFailure);
		return result;

	}

	private String[] GetDeltaTransactionCountSucessData() {

		String[] result = new String[4];
		result[0] = TotalTransactionCountSucess_MetricName;
		result[1] = TotalTransactionCountSucess_Component;
		result[2] = TotalTransactionCountSucess_Unit;
		result[3] = String.valueOf(deltaTransactionCountSuccess);
		return result;

	}

	private String[] GetTotalTransactionCountPerSecondData() {
		DecimalFormat df = new DecimalFormat("#.##########");

		String[] result = new String[4];
		result[0] = TotalTransactionCountPerSecond_MetricName;
		result[1] = TotalTransactionCountPerSecond_Component;
		result[2] = TotalTransactionCountPerSecond_Unit;
		result[3] = df.format(totalTransactionCountPerSecond);
		return result;

	}
}
