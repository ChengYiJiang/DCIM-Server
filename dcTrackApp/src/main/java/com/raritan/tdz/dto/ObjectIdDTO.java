package com.raritan.tdz.dto;

/**
 * 
 * @author bunty
 *
 */
public class ObjectIdDTO {
	private Object id; 
	private Object value; 
	
	public Object getId() {
		return id;
	}
	
	public void setId(Object id) {
		this.id = id;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
	
	public ObjectIdDTO(Object id, Object value) {
		super();
		this.id = id;
		this.value = value;
	}

	public ObjectIdDTO() {
	}
	

}
