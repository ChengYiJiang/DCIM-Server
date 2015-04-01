package com.raritan.tdz.circuit.dto;

public interface CircuitListInterface {

	public abstract long getCircuitId();

	public abstract void setCircuitId(long circuitId);

	public abstract Long getCircuitType();

	public abstract void setCircuitType(Long circuitType);

	public abstract String getStatus();

	public abstract void setStatus(String status);

	public abstract String getCreationDate();

	public abstract void setCreationDate(String creationDate);

	public abstract String getCreatedBy();

	public abstract void setCreatedBy(String createdBy);

	public abstract String getCabinetName();

	public abstract void setCabinetName(String cabinetname);

	public abstract String getStartItemName();

	public abstract void setStartItemName(String startItemName);

	public abstract String getStartPortName();

	public abstract void setStartPortName(String startPortName);

	public abstract String getComments();

	public abstract void setComments(String comments);

	public abstract Long getStartItemId();

	public abstract void setStartItemId(Long startItemId);

	public abstract Long getStartPortId();

	public abstract void setStartPortId(Long startPortId);

	public abstract Long getCabinetId();

	public abstract void setCabinetId(Long cabinetId);

	public abstract Long getLocationId();

	public abstract void setLocationId(Long locationId);

	public abstract String getConnectorName();

	public abstract void setConnectorName(String connectorName);
	
	public abstract String getCircuitTypeDesc();
	
	public abstract void setCircuitTypeDesc(String circuitTypeDesc);

}