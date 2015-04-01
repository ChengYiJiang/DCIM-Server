/**
 * 
 */
package com.raritan.tdz.item.dto;

/**
 * @author Basker
 *
 */
public class StackItemDTO {
	
	private Long id;
	private Long siblingId;
	private String name;
	private String modelName;
	private String cabinetName; 
	private Long UPosition;
	private Long status;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public Long getsiblingId() {
		return siblingId;
	}
	
	public void setSiblingId(Long siblingId) {
		this.siblingId = siblingId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getModelName() {
		return modelName;
	}
	
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	
	public String getCabinetName() {
		return cabinetName;
	}
	
	public void setCabinetName(String cabinetName) {
		this.cabinetName = cabinetName;
	}
	
	public Long getUPosition() {
		return UPosition;
	}
	
	public void setUPosition(Long uPosition) {
		UPosition = uPosition;
	}
	
	public Long getStatus() {
		return status;
	}
	
	public void setStatus(Long status) {
		this.status = status;
	}
	
}
