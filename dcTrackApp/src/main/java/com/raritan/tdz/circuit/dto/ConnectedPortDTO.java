package com.raritan.tdz.circuit.dto;
import java.util.List;

public class ConnectedPortDTO {
	private String itemPortDesc;
	private List<Float> circuitIdList;
	private Long circuitType;
	private String locationCode;
	
	public ConnectedPortDTO(){
		
	}
	
	public String getItemPortDesc() {
		return itemPortDesc;
	}
	public void setItemPortDesc(String itemPortDesc) {
		this.itemPortDesc = itemPortDesc;
	}
	public List<Float> getCircuitIdList() {
		return circuitIdList;
	}
	public void setCircuitIdList(List<Float> circuitIdList) {
		this.circuitIdList = circuitIdList;
	}
	public Long getCircuitType() {
		return circuitType;
	}
	public void setCircuitType(Long circuitType) {
		this.circuitType = circuitType;
	}

	public String getLocationCode() {
		return locationCode;
	}

	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}			
}
