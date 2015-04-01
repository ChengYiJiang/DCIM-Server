package com.raritan.tdz.piq.json;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * A response from the PIQ sensors API call for information on multiple sensors (as for a single PDU).
 * Example: https://vm1/api/v2/pdu/3/sensors
 * @author basker
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class SensorsJSON {

	private List<Sensor> sensors;
	
	public SensorsJSON() {
		super();
	}

	@JsonProperty(value="sensors")
	public List<Sensor> getSensors() {
		return sensors;
	}

	@JsonSetter(value="sensors")
	@JsonDeserialize(contentAs=Sensor.class)
	public void setSensors(List<Sensor> sensors) {
		this.sensors = sensors;
	}
}
