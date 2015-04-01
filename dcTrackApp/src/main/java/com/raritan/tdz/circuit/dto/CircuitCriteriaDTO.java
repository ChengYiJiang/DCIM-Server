package com.raritan.tdz.circuit.dto;

import com.raritan.tdz.domain.CircuitUID;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.ApplicationCodesEnum;

public class CircuitCriteriaDTO {
	private Long portId;
	private Long startPortId;
	private Long connectionId;
	private Long itemId;
	private Float circuitId;
	private String circuitTrace;
	private Long startConnId;
	private Long endConnId;
	private Long locationId;
	private String containCircuitTrace;
	private Long circuitType;
	private boolean secondPortSearched;
	private Long proposeCircuitId;
	private Long requestTypeCode;
	private Long startWithClassCode;
	private Long endWithClassCode;
	private Long lastNodePortId;
	private Long lastNodeItemId;
	private String sortColumn;
	private boolean sortOrderDesc;
	private String locationCode;
	
	private int maxLinesPerPage = -1;
	private int pageNumber = 1;
	
	private CircuitUID circuitUID;
	
	private boolean skipPowerCalc;
	
	private String vpcChainLabel;
	
	private UserInfo userInfo;
	
	public CircuitCriteriaDTO(){
		secondPortSearched = false;
		skipPowerCalc = false;
		userInfo = null;
	}
	
	public void clear() {
		this.portId = null;
		this.startPortId = null;
		this.connectionId = null;
		this.itemId = null;
		this.circuitId = null;
		this.circuitTrace = null;
		this.startConnId = null;
		this.endConnId = null;
		this.locationId = null;
		this.containCircuitTrace = null;
		this.circuitType = null;
		this.secondPortSearched = false;
		this.proposeCircuitId = null;
		this.requestTypeCode = null;
		this.startWithClassCode = null;
		this.endWithClassCode = null;
		this.lastNodePortId = null;
		this.lastNodeItemId = null;
		this.vpcChainLabel = null;
	}

	
	public Long getPortId() {
		return portId;
	}
	public void setPortId(Long portId) {
		this.portId = portId;
	}
	public Long getStartPortId() {
		return startPortId;
	}
	public void setStartPortId(Long startPortId) {
		this.startPortId = startPortId;
	}
	public Long getLastNodePortId() {
		return lastNodePortId;
	}
	public void setLastNodePortId(Long lastNodePortId) {
		this.lastNodePortId = lastNodePortId;
	}
	public Long getConnectionId() {
		return connectionId;
	}
	public void setConnectionId(Long connectionId) {
		this.connectionId = connectionId;
	}
	public Long getItemId() {
		return itemId;
	}
	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}
	public Float getCircuitId() {
		return circuitId;
	}
	public void setCircuitId(Float circuitId) {
		this.circuitId = circuitId;
		this.circuitUID = null;
	}
	public String getCircuitTrace() {
		return circuitTrace;
	}
	public void setCircuitTrace(String circuitTrace) {
		this.circuitTrace = circuitTrace;
	}
	public Long getStartConnId() {
		return startConnId;
	}
	public void setStartConnId(Long startConnId) {
		this.startConnId = startConnId;
	}
	public Long getEndConnId() {
		return endConnId;
	}
	public void setEndConnId(Long endConnId) {
		this.endConnId = endConnId;
	}
	public Long getLocationId() {
		return locationId;
	}
	public void setLocationId(Long locationId) {
		this.locationId = locationId;
	}
	public String getContainCircuitTrace() {
		return containCircuitTrace;
	}
	public void setContainCircuitTrace(String containCircuitTrace) {
		this.containCircuitTrace = containCircuitTrace;
	}
	public Long getCircuitType() {
		return circuitType;
	}
	public void setCircuitType(Long circuitType) {
		this.circuitType = circuitType;
		this.circuitUID = null;
	}

	public boolean isSecondPortSearched() {
		return secondPortSearched;
	}

	public void setSecondPortSearched(boolean secondPortSearched) {
		this.secondPortSearched = secondPortSearched;
	}

	public Long getProposeCircuitId() {
		return proposeCircuitId;
	}

	public void setProposeCircuitId(Long proposeCircuitId) {
		this.proposeCircuitId = proposeCircuitId;
	}

	public Long getRequestTypeCode() {
		return requestTypeCode;
	}

	public void setRequestTypeCode(Long requestTypeCode) {
		this.requestTypeCode = requestTypeCode;
	}	
	
	public Long getStartWithClassCode() {
		return startWithClassCode;
	}

	public void setStartWithClassCode(Long startWithClassCode) {
		this.startWithClassCode = startWithClassCode;
	}

	public Long getEndWithClassCode() {
		return endWithClassCode;
	}

	public void setEndWithClassCode(Long endWithClassCode) {
		this.endWithClassCode = endWithClassCode;
	}
	
	

	public boolean isDataCircuit(){
		if(circuitType == SystemLookup.PortClass.DATA){
			return true;
		}
		return false;
	}
	
	public boolean isPowerCircuit(){
		if(circuitType == SystemLookup.PortClass.POWER){
			return true;
		}
		return false;
	}

	public String getEditEnumError(){
		if(isDataCircuit()){
			return ApplicationCodesEnum.DATA_CIR_EDIT_FAIL.value();
		}
		return ApplicationCodesEnum.PWR_CIR_EDIT_FAIL.value();
	}
	
	public String getFetchEnumError(){
		if(isDataCircuit()){
			return ApplicationCodesEnum.DATA_CIR_FETCH_FAIL.value();
		}
		return ApplicationCodesEnum.PWR_CIR_FETCH_FAIL.value();
	}

	public String getNotFoundEnumError() {
		if(isDataCircuit()){
			return ApplicationCodesEnum.DATA_CIR_DOES_NOT_EXIST.value();
		}
		return ApplicationCodesEnum.PWR_CIR_DOES_NOT_EXIST.value();
	}
	
	public int getNotFoundEnumErrorCode() {
		if(isDataCircuit()){
			return ApplicationCodesEnum.DATA_CIR_DOES_NOT_EXIST.errCode();
		}
		return ApplicationCodesEnum.PWR_CIR_DOES_NOT_EXIST.errCode();
	}
	
	public String getNotFoundInListEnumError() {
		if(isDataCircuit()){
			return ApplicationCodesEnum.DATA_CIR_DOES_NOT_EXIST_LIST.value();
		}
		return ApplicationCodesEnum.PWR_CIR_DOES_NOT_EXIST_LIST.value();
	}
	
	public int getNotFoundInListEnumErrorCode() {
		if(isDataCircuit()){
			return ApplicationCodesEnum.DATA_CIR_DOES_NOT_EXIST_LIST.errCode();
		}
		return ApplicationCodesEnum.PWR_CIR_DOES_NOT_EXIST_LIST.errCode();
	}
	
	public String getRequestPendingEnumError(){
		if(isDataCircuit()){
			return ApplicationCodesEnum.DATA_CIR_PENDING_REQUEST.value();
		}
		return ApplicationCodesEnum.PWR_CIR_PENDING_REQUEST.value(); //need to change for power
	}
	
	public String getRequestPendingEnumErrorSave(){
		if(isDataCircuit()){
			return ApplicationCodesEnum.DATA_CIR_PENDING_REQUEST_SAVE.value();
		}
		return ApplicationCodesEnum.PWR_CIR_PENDING_REQUEST_SAVE.value(); //need to change for power
	}
	
	public Long getLastNodeItemId() {
		return lastNodeItemId;
	}

	public void setLastNodeItemId(Long lastNodeItemId) {
		this.lastNodeItemId = lastNodeItemId;
	}

	public String getSortColumn() {
		return sortColumn;
	}

	public void setSortColumn(String sortColumn) {
		this.sortColumn = sortColumn;
	}

	public boolean getSortOrderDesc() {
		return sortOrderDesc;
	}

	public void setSortOrderDesc(boolean sortOrderDesc) {
		this.sortOrderDesc = sortOrderDesc;
	}

	public String getLocationCode() {
		return locationCode;
	}

	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}

	public int getMaxLinesPerPage() {
		return maxLinesPerPage;
	}

	public void setMaxLinesPerPage(int  maxLinesPerPage) {
		this.maxLinesPerPage = maxLinesPerPage;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
	
	public CircuitUID getCircuitUID() {
		if (circuitUID == null) {
			circuitUID = new CircuitUID( circuitId );
		}
		return circuitUID;
	}
	

	public boolean validateParams(){
		int count = 0;
		
		if(portId == null || portId == 0) count++;
		if(startPortId == null || startPortId == 0) count++;
		if(connectionId == null || connectionId == 0) count++;
		if(itemId == null || itemId == 0) count++;
		if(circuitId == null || circuitId == 0) count++;
		if(circuitTrace == null || circuitTrace.length() == 0) count++;
		if(locationId == null || locationId == 0) count++;
		if(containCircuitTrace == null || containCircuitTrace.length() == 0) count++;
		if(proposeCircuitId == null || proposeCircuitId == 0) count++;
		
		if(circuitType == null || circuitType == 0 || count == 9) return true;
		
		return false;
	}	
	
	public String getInvalidSearchParamsEnumError(){
		return ApplicationCodesEnum.CIRCUIT_INVALID_SEARCH_PARAMS.value(); 
	}

	public boolean isSkipPowerCalc() {
		return skipPowerCalc;
	}

	public void setSkipPowerCalc(boolean skipPowerCalc) {
		this.skipPowerCalc = skipPowerCalc;
	}

	public String getVpcChainLabel() {
		return vpcChainLabel;
	}

	public void setVpcChainLabel(String vpcChainLabel) {
		this.vpcChainLabel = vpcChainLabel;
	}
	
	public boolean isVpcRequested() {
		
		return (null != vpcChainLabel && vpcChainLabel.length() > 0);
		
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}
	
}