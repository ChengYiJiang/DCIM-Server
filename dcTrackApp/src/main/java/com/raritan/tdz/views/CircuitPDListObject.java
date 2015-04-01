package com.raritan.tdz.views;

/**
* CircuitPDListObject
*/
public class CircuitPDListObject {
	
	/**
	 * 
	 */
	private long circuitId;
	private String circuitType;
	private String status;
	private String creationDate;
	private String createdBy;
	private String cabinetName;
	private String startItemName;
	private String startPortName;
	private String comments;
	private String connectorName;
	private Long locationId;
	private Long cabinetId;
	private Long startItemId;
	private Long startPortId;

	
	public CircuitPDListObject() {
	}
	
	public long getCircuitId() {
		return circuitId;
	}

	public void setCircuitId(long circuitId) {
		this.circuitId = circuitId;
	}

	public String getCircuitType() {
		return circuitType;
	}

	public void setCircuitType(String circuitType) {
		this.circuitType = circuitType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getCabinetName() {
	    return this.cabinetName;
	}

	public void setCabinetName(String cabinetname) {
	    this.cabinetName = cabinetname;
	}
	 
	public String getStartItemName() {
		return startItemName;
	}

	public void setStartItemName(String startItemName) {
		this.startItemName = startItemName;
	}

	
	public String getStartPortName() {
		return startPortName;
	}

	public void setStartPortName(String startPortName) {
		this.startPortName = startPortName;
	}

	public String getComments() {
		return comments;
	}
	
	public void setComments(String comments) {
		this.comments = comments;
	}

	public Long getStartItemId() {
		return startItemId;
	}

	public void setStartItemId(Long startItemId) {
		this.startItemId = startItemId;
	}

	public Long getStartPortId() {
		return startPortId;
	}

	public void setStartPortId(Long startPortId) {
		this.startPortId = startPortId;
	}

	public Long getCabinetId() {
	    return this.cabinetId;
	}

	public void setCabinetId(Long cabinetId) {
	    this.cabinetId = cabinetId;
	}

	public Long getLocationId() {
		return locationId;
	}
	
	public void setLocationId(Long locationId) {
		this.locationId = locationId;
	}

	public String getConnectorName() {
		return connectorName;
	}

	public void setConnectorName(String connectorName) {
		this.connectorName = connectorName;
	}
}


