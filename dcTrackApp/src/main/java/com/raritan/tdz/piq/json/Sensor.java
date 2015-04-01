package com.raritan.tdz.piq.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * The response representing a single PDU sensor from PIQ.
 * @author basker
 */
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class Sensor  extends SensorBase{
	private String label;
	private String removed;
	private String position;
	private Reading reading;
	private State state;

	public Sensor() {
		super();
	}
	
	@JsonProperty(value="label")
	public String getLabel() {
		return label;
	}
	@JsonSetter(value="label")
	public void setLabel(String label) {
		this.label = label;
	}
	
	@JsonProperty(value="position")
	public String getPosition() {
		return position;
	}
	@JsonSetter(value="position")
	public void setPosition(String position) {
		this.position = position;
	}
	
	@JsonProperty(value="reading")
	public Reading getReading() {
		return reading;
	}
	@JsonSetter(value="reading")
	public void setReading(Reading reading) {
		this.reading = reading;
	}
	
	@JsonProperty(value="removed")
	public String getRemoved() {
		return removed;
	}
	@JsonSetter(value="removed")
	public void setRemoved(String removed) {
		this.removed = removed;
	}
	
	@JsonProperty(value="state")
	public State getState() {
		return state;
	}
	@JsonSetter(value="state")
	public void setState(State state) {
		this.state = state;
	}

	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
	public static class Reading {
		private long id;
		private long sensorId;
		private Double value;
		private Double maxValue;
		private Double minValue;
		private String uom;

		public Reading() {
			super();
		}
		@JsonProperty(value="id")
		public long getId() {
			return id;
		}
		@JsonSetter(value="id")
		public void setId(long id) {
			this.id = id;
		}
		@JsonProperty(value="sensor_id")
		public long getSensorId() {
			return sensorId;
		}
		@JsonSetter(value="sensor_id")
		public void setSensorId(long sensorId) {
			this.sensorId = sensorId;
		}
		@JsonProperty(value="value")
		public Double getValue() {
			return value;
		}
		@JsonSetter(value="value")
		public void setValue(Double value) {
			this.value = value;
		}
		@JsonProperty(value="max_value")
		public Double getMaxValue() {
			return maxValue;
		}
		@JsonSetter(value="max_value")
		public void setMaxValue(Double maxValue) {
			this.maxValue = maxValue;
		}
		@JsonProperty(value="min_value")
		public Double getMinValue() {
			return minValue;
		}
		@JsonSetter(value="min_value")
		public void setMinValue(Double minValue) {
			this.minValue = minValue;
		}
		@JsonProperty(value="uom")
		public String getUom() {
			return uom;
		}
		@JsonSetter(value="uom")
		public void setUom(String uom) {
			this.uom = uom;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
	public static class State {
		private long id;
		private long sensorId;
		private String state;
		private String startTime;
		private String endTime;
		
		public State() {
			super();
		}
		@JsonProperty(value="id")
		public long getId() {
			return id;
		}
		@JsonSetter(value="id")
		public void setId(long id) {
			this.id = id;
		}
		@JsonProperty(value="sensor_id")
		public long getSensorId() {
			return sensorId;
		}
		@JsonSetter(value="sensor_id")
		public void setSensorId(long sensorId) {
			this.sensorId = sensorId;
		}
		@JsonProperty(value="state")
		public String getState() {
			return state;
		}
		@JsonSetter(value="state")
		public void setState(String state) {
			this.state = state;
		}
		@JsonProperty(value="start_time")
		public String getStartTime() {
			return startTime;
		}
		@JsonSetter(value="start_time")
		public void setStartTime(String startTime) {
			this.startTime = startTime;
		}
		@JsonProperty(value="end_time")
		public String getEndTime() {
			return endTime;
		}
		@JsonSetter(value="end_time")
		public void setEndTime(String endTime) {
			this.endTime = endTime;
		}
		
	}
}
