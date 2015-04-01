package com.raritan.tdz.dto;

public class PortConnectorCompatListDTO {
	private Long connectorCompatId;
	private String connectorName;
	private Long connectorId;
	
	public PortConnectorCompatListDTO(){
		
	}
	
	public Long getConnectorCompatId() {
		return connectorCompatId;
	}
	public void setConnectorCompatId(Long connectorCompatId) {
		this.connectorCompatId = connectorCompatId;
	}
	public String getConnectorName() {
		return connectorName;
	}
	public void setConnectorName(String connectorName) {
		this.connectorName = connectorName;
	}
	public Long getConnectorId() {
		return connectorId;
	}
	public void setConnectorId(Long connectorId) {
		this.connectorId = connectorId;
	}
}
