package com.raritan.tdz.piq.json;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * Represents a generic error response from PIQ typically sent back with HTTP 4xx status code errors.
 *
 * @author Andrew Cohen
 */
@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown=true)
public class ErrorJSON {
	
	private String error;
	private String message;
	private List<String> messages;
	
	public ErrorJSON(){
		super();
	}
	
	@JsonProperty(value="error")
	public String getError() {
		return error;
	}
	@JsonSetter(value="error")
	public void setError(String error) {
		this.error = error;
	}
	
	@JsonProperty("messages")
	public List<String> getMessages() {
		return messages;
	}
	@JsonSetter("messages")
	public void setMessage(List<String> messages) {
		this.messages = messages;
	}
}
