package com.raritan.tdz.piq.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;

import com.raritan.tdz.piq.json.SensorBase;

/**
 * Response information about a single asset strip from PIQ.
 * @author Andrew Cohen
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class AssetStrip extends SensorBase{

	public final static String DISCONNECTED_STATE = "disconnected";
	public final static String AVAILABLE_STATE = "available";
	
	private String name;
	private String state;
	
	public AssetStrip(){
		super();
	}
	
	@JsonProperty(value="name")
	public String getName() {
		return name;
	}
	@JsonSetter(value="name")
	public void setName(String name) {
		this.name = name;
	}
	
	@JsonProperty(value="state")
	public String getState() {
		return state;
	}
	@JsonSetter(value="state")
	public void setState(String state) {
		this.state = state;
	}
}
