package com.raritan.tdz.circuit.home;

import java.util.List;

public class ConnectedPort {
	private String itemName;
	private String portName;
	private Long circuitId;
	private Long circuitType;
	private String locationCode;
	
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getPortName() {
		return portName;
	}
	public void setPortName(String portName) {
		this.portName = portName;
	}
	public Long getCircuitId() {
		return circuitId;
	}
	public void setCircuitId(Long circuitId) {
		this.circuitId = circuitId;
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
	
	public String getItemPortDesc() {
		return itemName +"/" + portName;
	}
	
	public boolean isSamePort(ConnectedPort port) {
		if(port == null){
			return false;
		}
		
		String key1 = itemName + circuitType + portName + locationCode;
		String key2 = port.getItemName() + port.getCircuitType() + port.getPortName() + port.getLocationCode();
		
		return key1.equals(key2);
	}

		
}
