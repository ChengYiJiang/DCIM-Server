package com.raritan.tdz.piq.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;

/**
 * A response from the PIQ sensors API call for information on a single Sensor.
 * Example: https://vm1/api/v2/sensors/3
 * @author basker
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class SensorJSON {

	private Sensor sensor;
	
	public SensorJSON() {
		super();
	}
	
	public SensorJSON(Sensor sensor) {
		setSensor( sensor );
	}
	
	@JsonProperty(value="sensor")
	public Sensor getSensor() {
		return sensor;
	}

	@JsonSetter(value="sensor")
	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}
}
