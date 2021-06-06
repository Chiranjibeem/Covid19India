package com.covid19.india.Covid19India.model;

public class LocationConfigurationException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5517457344163789881L;
	private String message;

	public LocationConfigurationException(String message) {
		super();
		this.setMessage(message);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	

}
