package com.raritan.tdz.piq.json;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * The response representing a single PDU inlet from PIQ.
 * @author basker
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Inlet {
	private long id;
	private int inletId;
	private long pduId;
	private int ordinal;
	private double ratedAmps;
	private Reading reading;
	
	public Inlet() {
		super();
	}
	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
	public static class Reading {
		private long id;
		private double current;

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
		@JsonProperty(value="current")
		public double getCurrent() {
			return current;
		}
		@JsonSetter(value="current")
		public void setCurrent(double current) {
			this.current = current;
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
	
	@JsonProperty(value="inlet_id")
	public int getInletId() {
		return inletId;
	}
	@JsonSetter(value="inlet_id")
	public void setInletId(int inletId) {
		this.inletId = inletId;
	}
	
	@JsonProperty(value="pdu_id")
	public long getPduId() {
		return pduId;
	}
	@JsonSetter(value="pdu_id")
	public void setPduId(long pduId) {
		this.pduId = pduId;
	}

	@JsonProperty(value="ordinal")
	public int getOrdinal() {
		return ordinal;
	}
	@JsonSetter(value="ordinal")
	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}
	
	@JsonProperty(value="rated_amps")
	public double getRatedAmps() {
		return ratedAmps;
	}
	@JsonSetter(value="rated_amps")
	public void setRatedAmps(double ratedAmps) {
		this.ratedAmps = ratedAmps;
	}
	
}
