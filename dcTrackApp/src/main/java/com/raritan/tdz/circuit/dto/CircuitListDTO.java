package com.raritan.tdz.circuit.dto;

import java.sql.Timestamp;

import com.raritan.tdz.domain.CircuitUID;

/**
* CircuitPDListObject
*/
public class CircuitListDTO {
	
	/**
	 * 
	 */
	private float circuitId;
	private String circuitTypeDesc;
	private Long circuitType;
	private String status;
	private Timestamp creationDate;
	private String createdBy;
	private String startCabinetName;
	private String startItemName;
	private String startPortName;
	private String comments;
	private String startConnectorName;
	private Long startLocationId;
	private Long startCabinetId;
	private Long startItemId;
	private Long startPortId;
	private String endCabinetName;
	private String endItemName;
	private String endPortName;
	private String endConnectorName;
	private Long endItemId;
	private Long endPortId;
	private Long statusLksCode;
	private Long totalCordLength;
	private String requestNumber;
	private String interNodes;
	private String createdByTeam;
	private Long createdByTeamId;
	private String locationCode;
	private String requestStage;
	private Long requestStageLksCode;
	private Long proposeCircuitId;
	private String requestType;
	private Long requestId;
	private boolean isPartialCircuitInUse;
	private String visualCircuitTrace;
	
	private CircuitUID circuitUID;
	
	public CircuitListDTO() {
	}

	public float getCircuitId() {
		return circuitId;
	}

	public void setCircuitId(float circuitId) {
		this.circuitId = circuitId;
		this.circuitUID = null;
	}

	public String getCircuitTypeDesc() {
		return circuitTypeDesc;
	}

	public void setCircuitTypeDesc(String circuitTypeDesc) {
		this.circuitTypeDesc = circuitTypeDesc;
	}

	public Long getCircuitType() {
		return circuitType;
	}

	public void setCircuitType(Long circuitType) {
		this.circuitType = circuitType;
		this.circuitUID = null;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Timestamp getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getStartCabinetName() {
		return startCabinetName;
	}

	public void setStartCabinetName(String startCabinetName) {
		this.startCabinetName = startCabinetName;
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

	public String getStartConnectorName() {
		return startConnectorName;
	}

	public void setStartConnectorName(String startConnectorName) {
		this.startConnectorName = startConnectorName;
	}

	public Long getStartLocationId() {
		return startLocationId;
	}

	public void setStartLocationId(Long startLocationId) {
		this.startLocationId = startLocationId;
	}

	public Long getStartCabinetId() {
		return startCabinetId;
	}

	public void setStartCabinetId(Long startCabinetId) {
		this.startCabinetId = startCabinetId;
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

	public String getEndCabinetName() {
		return endCabinetName;
	}

	public void setEndCabinetName(String endCabinetName) {
		this.endCabinetName = endCabinetName;
	}

	public String getEndItemName() {
		return endItemName;
	}

	public void setEndItemName(String endItemName) {
		this.endItemName = endItemName;
	}

	public String getEndPortName() {
		return endPortName;
	}

	public void setEndPortName(String endPortName) {
		this.endPortName = endPortName;
	}

	public String getEndConnectorName() {
		return endConnectorName;
	}

	public void setEndConnectorName(String endConnectorName) {
		this.endConnectorName = endConnectorName;
	}

	public Long getEndItemId() {
		return endItemId;
	}

	public void setEndItemId(Long endItemId) {
		this.endItemId = endItemId;
	}

	public Long getEndPortId() {
		return endPortId;
	}

	public void setEndPortId(Long endPortId) {
		this.endPortId = endPortId;
	}

	public Long getStatusLksCode() {
		return statusLksCode;
	}

	public void setStatusLksCode(Long statusLksCode) {
		this.statusLksCode = statusLksCode;
	}

	public Long getTotalCordLength() {
		return totalCordLength;
	}

	public void setTotalCordLength(Long totalCordLength) {
		this.totalCordLength = totalCordLength;
	}

	public String getRequestNumber() {
		return requestNumber;
	}

	public void setRequestNumber(String requestNumber) {
		this.requestNumber = requestNumber;
	}

	public String getInterNodes() {
		return interNodes;
	}

	public void setInterNodes(String interNodes) {
		this.interNodes = interNodes;
	}

	public String getCreatedByTeam() {
		return createdByTeam;
	}

	public void setCreatedByTeam(String createdByTeam) {
		this.createdByTeam = createdByTeam;
	}

	public Long getCreatedByTeamId() {
		return createdByTeamId;
	}

	public void setCreatedByTeamId(Long createdByTeamId) {
		this.createdByTeamId = createdByTeamId;
	}

	public String getLocationCode() {
		return locationCode;
	}

	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}

	public String getRequestStage() {
		return requestStage;
	}

	public void setRequestStage(String requestStage) {
		this.requestStage = requestStage;
	}

	public Long getRequestStageLksCode() {
		return requestStageLksCode;
	}

	public void setRequestStageLksCode(Long requestStageLksCode) {
		this.requestStageLksCode = requestStageLksCode;
	}

	public Long getProposeCircuitId() {
		return proposeCircuitId;
	}

	public void setProposeCircuitId(Long proposeCircuitId) {
		this.proposeCircuitId = proposeCircuitId;
	}
	
	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public Long getRequestId() {
		return requestId;
	}

	public void setRequestId(Long requestId) {
		this.requestId = requestId;
	}	
	
	public boolean isPartialCircuitInUse() {
		return isPartialCircuitInUse;
	}

	public void setPartialCircuitInUse(boolean isPartialCircuitInUse) {
		this.isPartialCircuitInUse = isPartialCircuitInUse;
	}
	
	// For post hibernate sorting
	public String isProposedString() {
		return proposeCircuitId != null ? "View" : "";
	}
	public void setProposedString(String proposedString) {
	}
	

	public String getVisualCircuitTrace() {
		return visualCircuitTrace;
	}

	public void setVisualCircuitTrace(String visualCircuitTrace) {
		this.visualCircuitTrace = visualCircuitTrace;
	}
	
	// End post hibernate sorting methods


	public void print(){
		System.out.println("=======================================");
		System.out.println("Start Item Name: " + this.startItemName);
		System.out.println("Start Port Name: " + this.startPortName);
		System.out.println("Circuit ID: " + this.circuitId);
		System.out.println("Status: " + this.status);
	}
	
	/**
	 * Swap the start and end node information of the list.
	 */
	public void swapStartAndEnd() {
		final long tmpItemId = getStartItemId();
		final long tmpPortId = getStartPortId();
		final String tmpConnName = getStartConnectorName();
		final String tmpItemName = getStartItemName();
		final String tmpPortName = getStartPortName();
		//final long tmpLocationId = getStartLocationId();
		//final long tmpCabinetId = getStartCabinetId();
		final String tmpCabinetName = getStartCabinetName();
		
		setStartItemId( getEndItemId() );
		setEndItemId( tmpItemId );
		
		setStartPortId( getEndPortId() );
		setEndPortId( tmpPortId );
		
		setStartConnectorName( getEndConnectorName() );
		setEndConnectorName( tmpConnName );
		
		setStartItemName( getEndItemName() );
		setEndItemName( tmpItemName );
		
		setStartPortName( getEndPortName() );
		setEndPortName( tmpPortName );
		
		setStartCabinetName( getEndCabinetName() );
		setEndCabinetName( tmpCabinetName );
	}
	
	public CircuitUID getCircuitUID() {
		if (circuitUID == null) {
			circuitUID = new CircuitUID( circuitId  );
		}
		return circuitUID;
	}
}


