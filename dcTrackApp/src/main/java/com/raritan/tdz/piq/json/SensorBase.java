package com.raritan.tdz.piq.json;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;


public abstract class SensorBase {
	private long id;
	private long pduId;
	private int ordinal;
	private String attributeName;


	public SensorBase() {
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
	
	@JsonProperty(value="attribute_name")
	public String getAttributeName() {
		return attributeName;
	}
	@JsonSetter(value="attribute_name")
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	
}
