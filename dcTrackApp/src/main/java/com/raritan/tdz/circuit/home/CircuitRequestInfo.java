package com.raritan.tdz.circuit.home;

import com.raritan.tdz.lookup.SystemLookup;

public class CircuitRequestInfo {
	private Long circuitId;
	private Long proposeCircuitId;
	private Long requestId;
	private String requestNo;
	private Long requestStageCode;
	private String requestStage;
	private String requestType;


/*<return-scalar column="circuit_id" type="long"/>
<return-scalar column="requestno" type="string"/>
<return-scalar column="requeststage" type="string"/>
<return-scalar column="propose_circuit_id" type="long"/>
<return-scalar column="stage_value_code" type="long"/>
<return-scalar column="request_id" type="long"/>
<return-scalar column="request_type" type="string"/>
*/

	public CircuitRequestInfo(){
		this.requestStageCode = 0L;
		this.requestId = 0L;
	}
	
	public CircuitRequestInfo(Object[] columns){
		this.circuitId = (Long)columns[0];
		this.requestNo = (String)columns[1];
		this.requestStage = (String)columns[2];		
		this.proposeCircuitId = (Long)columns[3];
		this.requestStageCode = (Long)columns[4];
		this.requestId = (Long)columns[5];
		this.requestType = (String)columns[6];	
	}
	
	public Long getCircuitId() {
		return circuitId;
	}
	public void setCircuitId(Long circuitId) {
		this.circuitId = circuitId;
	}
	public Long getProposeCircuitId() {
		return proposeCircuitId;
	}
	public void setProposeCircuitId(Long proposeCircuitId) {
		this.proposeCircuitId = proposeCircuitId;
	}
	public Long getRequestId() {
		return requestId;
	}
	public void setRequestId(Long requestId) {
		this.requestId = requestId;
	}
	public String getRequestNo() {
		return requestNo;
	}
	public void setRequestNo(String requestNo) {
		this.requestNo = requestNo;
	}
	public Long getRequestStageCode() {
		return requestStageCode;
	}
	public void setRequestStageCode(Long requestStageCode) {
		this.requestStageCode = requestStageCode;
	}
	public String getRequestStage() {
		return requestStage;
	}
	public void setRequestStage(String requestStage) {
		this.requestStage = requestStage;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public boolean isRequestPending() {
		long code = requestStageCode.longValue();
		
		if(code == 0){
			return false;
		}
		
		if(code != SystemLookup.RequestStage.REQUEST_ISSUED &&
		   code != SystemLookup.RequestStage.REQUEST_REJECTED &&
		   code != SystemLookup.RequestStage.REQUEST_UPDATED){			
			return true;
		}		
		
		return false;
	}

}
