package com.neotys.newrelic.infrastucture;

/**
 * Created by anouvel on 05/02/2018.
 */
public class NewRelicException extends Exception {
	//Parameterless Constructor
	public NewRelicException() {
	}

	//Constructor that accepts a message
	public NewRelicException(String message) {
		super(message);
	}
}
