package com.raritan.tdz.piq.json;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * The response representing a single PDU outlet from PIQ.
 * @author Andrew Cohen
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Outlet {
	private long id;
	private int outletId;
	private String outletName;
	private Long deviceId;
	private String state;
	private long pduId;
	private Reading reading; 
	
	public Outlet() {
		super();
	}
	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
	public static class Reading {
		private long id;
		private double currentAmps;

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
		@JsonProperty(value="current_amps")
		public double getCurrentAmps() {
			return currentAmps;
		}
		@JsonSetter(value="current_amps")
		public void setCurrentAmps(double currentAmps) {
			this.currentAmps = currentAmps;
		}
	}

	@JsonIgnore
	public Reading getReading() {
		return reading;
	}
	@JsonSetter(value="reading")
	public void setReading(Reading reading) {
		this.reading = reading;
	}
	
	@JsonProperty(value="id")
	public long getId() {
		return id;
	}
	@JsonSetter(value="id")
	public void setId(long id) {
		this.id = id;
	}

	@JsonProperty(value="outlet_id")
	public int getOutletId() {
		return outletId;
	}
	@JsonSetter(value="outlet_id")
	public void setOutletId(int outletId) {
		this.outletId = outletId;
	}

	@JsonProperty(value="outlet_name")
	public String getOutletName() {
		return outletName;
	}
	@JsonSetter(value="outlet_name")
	public void setOutletName(String outletName) {
		this.outletName = outletName;
	}

	@JsonProperty(value="device_id")
	public Long getDeviceId() {
		return deviceId;
	}
	@JsonSetter(value="device_id")
	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	@JsonProperty(value="state")
	public String getState() {
		return state;
	}
	@JsonSetter(value="state")
	public void setState(String state) {
		this.state = state;
	}
	
	@JsonProperty(value="pdu_id")
	public long getPduId() {
		return pduId;
	}
	@JsonSetter(value="pdu_id")
	public void setPduId(long pduId) {
		this.pduId = pduId;
	}
	
}
