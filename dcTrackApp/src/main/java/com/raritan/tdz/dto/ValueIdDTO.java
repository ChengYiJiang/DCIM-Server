/**
 * 
 */
package com.raritan.tdz.dto;

/**
 * @author prasanna
 * This represents any kind of lookup data transfer object.
 */
public class ValueIdDTO {
	private Object data; //This is for convenience  to the client
	private String label; //This is for convenience  to the client
	
	public ValueIdDTO() {
		//Default
	}
	
	public ValueIdDTO(String label, Object data){
		this.label = label;
		this.data = data;
	}
	
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	
}
