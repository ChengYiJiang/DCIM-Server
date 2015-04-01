package com.raritan.tdz.piq.json;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * The response representing a single PDU inlet from PIQ.
 * @author bunty
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class InletPole {

	private long id;
	
	private long inletId;
	
	private long pduId;
	
	private int ordinal;

	private Reading reading;	
	
	public InletPole() {
		super();
	}



	@JsonIgnoreProperties(ignoreUnknown=true)
	@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
	public static class Reading {
		
		private long id;
		
		private double current;
		
		private long inletId;
		
		private int inletOrdinal;
		
		private long inletPoleOrdinal;

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

		@JsonProperty(value="inlet_id")
		public long getInletId() {
			return inletId;
		}

		@JsonSetter(value="inlet_id")
		public void setInletId(long inletId) {
			this.inletId = inletId;
		}

		@JsonProperty(value="inlet_ordinal")
		public int getInletOrdinal() {
			return inletOrdinal;
		}

		@JsonSetter(value="inlet_ordinal")
		public void setInletOrdinal(int inletOrdinal) {
			this.inletOrdinal = inletOrdinal;
		}

		@JsonProperty(value="inlet_pole_ordinal")
		public long getInletPoleOrdinal() {
			return inletPoleOrdinal;
		}

		@JsonSetter(value="inlet_pole_ordinal")
		public void setInletPoleOrdinal(long inletPoleOrdinal) {
			this.inletPoleOrdinal = inletPoleOrdinal;
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
	public long getInletId() {
		return inletId;
	}
	@JsonSetter(value="inlet_id")
	public void setInletId(long inletId) {
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


}
