package com.raritan.tdz.events.dto;

import java.io.Serializable;


public class EventDetailDTOImpl implements Serializable, EventDetailDTO {
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String value;
	
	public EventDetailDTOImpl(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
}
