package com.neotys.newrelic;

/**
 * Created by anouvel on 05/02/2018.
 */
public class NewRelicException extends Exception {
	
	public NewRelicException() {
	}

	public NewRelicException(final String message) {
		super(message);
	}
	
	public NewRelicException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
	
	public NewRelicException(final Throwable throwable) {
		super(throwable);
	}
}
