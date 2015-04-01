package com.raritan.tdz.circuit.dto;

import java.util.Collections;
import java.util.List;

import com.raritan.tdz.domain.CircuitUID;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.PortDTOBase;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.ApplicationCodesEnum;

public class CircuitDTO {
	private Float circuitId;
	private Long  startConnId;
	private Long  endConnId;
	private String circuitTrace;
	private boolean isImplicit;

	private List<ConnectionDTO> connList;
	private List<CircuitNodeInterface> nodeList;
	private Long requestTypeCode;
	private Long proposeCircuitId;
	private long statusCode;
	private String sharedCircuitTrace;
	private boolean isPartialCircuitInUse;
	private boolean disconnectFirstPort = true;
	private Boolean readOnly = null;
	private int circuitState;
	private CircuitUID circuitUID;
	private UserInfo userInfo;
	private Long origin;

	public Boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

	public int getCircuitState() {
		return circuitState;
	}

	public void setCircuitState(int circuitState) {
		this.circuitState = circuitState;
	}

	public void setStatusCode(long statusCode) {
		this.statusCode = statusCode;
	}

	public CircuitDTO(){
		statusCode = 0;
		userInfo = null;
		origin = SystemLookup.ItemOrigen.CLIENT;
	}

	public Float getCircuitId() {
		return circuitId;
	}
	public void setCircuitId(Float circuitId) {
		this.circuitId = circuitId;
		this.circuitUID = null;
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
	public String getCircuitTrace() {
		return circuitTrace;
	}
	public void setCircuitTrace(String circuitTrace) {
		this.circuitTrace = circuitTrace;
	}
	public boolean isImplicit() {
		return isImplicit;
	}
	public void setImplicit(boolean isImplicit) {
		this.isImplicit = isImplicit;
	}
	public List<ConnectionDTO> getConnList() {
		return connList;
	}
	public void setConnList(List<ConnectionDTO> connList) {
		this.connList = connList;
	}
	public List<CircuitNodeInterface> getNodeList() {
		return nodeList;
	}
	public void setNodeList(List<CircuitNodeInterface> nodeList) {
		this.nodeList = nodeList;
		this.readOnly = null;
	}

	public Long getRequestTypeCode() {
		return requestTypeCode;
	}

	public void setRequestTypeCode(Long requestTypeCode) {
		this.requestTypeCode = requestTypeCode;
	}

	public String getSharedCircuitTrace() {
		return sharedCircuitTrace;
	}

	public void setSharedCircuitTrace(String sharedCircuitTrace) {
		this.sharedCircuitTrace = sharedCircuitTrace;
	}

	public long getCircuitType(){
		if(nodeList != null && nodeList.size() > 0){
			if(nodeList.get(0) instanceof DataPortNodeDTO){
				return SystemLookup.PortClass.DATA;
			}
		}
		return SystemLookup.PortClass.POWER; // default to power
	}

	public boolean isDataCircuit(){
		if(nodeList != null && nodeList.size() > 0){
			if(nodeList.get(0) instanceof DataPortNodeDTO){
				return true;
			}
		}
		return false;
	}

	public boolean isPowerCircuit(){
		if(nodeList != null && nodeList.size() > 0){
			if(nodeList.get(0) instanceof PowerPortNodeDTO){
				return true;
			}
		}
		return false;
	}

	public boolean isPartialCircuitInUse() {
		return isPartialCircuitInUse;
	}

	public void setPartialCircuitInUse(boolean isPartialCircuitInUse) {
		this.isPartialCircuitInUse = isPartialCircuitInUse;
	}

	public String getEditEnumError(){
		if(isDataCircuit()){
			return ApplicationCodesEnum.DATA_CIR_EDIT_FAIL.value();
		}
		return ApplicationCodesEnum.PWR_CIR_EDIT_FAIL.value();
	}

	public Long getProposeCircuitId() {
		return proposeCircuitId;
	}

	public void setProposeCircuitId(Long proposeCircuitId) {
		this.proposeCircuitId = proposeCircuitId;
	}

	public Long getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(Long statusCode) {
		this.statusCode = statusCode;
	}

	public boolean isStatusInstalled(){
		if(statusCode == SystemLookup.ItemStatus.INSTALLED){
			return true;
		}
		return false;
	}

	public boolean isNewCircuit(){
		if((circuitId != null && circuitId > 0) ||
		   (proposeCircuitId != null && proposeCircuitId > 0)){
			return false;
		}
		return true;
	}

	public List<CircuitNodeInterface> reverseNodeList() {
		Collections.reverse( nodeList );
		return getNodeList();
	}

	public boolean isDisconnectFirstPort() {
		return disconnectFirstPort;
	}

	public void setDisconnectFirstPort(boolean disconnectFirstPort) {
		this.disconnectFirstPort = disconnectFirstPort;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public CircuitUID getCircuitUID() {
		if (circuitUID == null) {
			circuitUID = new CircuitUID( circuitId );
		}
		return circuitUID;
	}

	@Override
	public String toString() {
		return "CircuitDTO [circuitId=" + circuitId + ", startConnId="
				+ startConnId + ", endConnId=" + endConnId + ", circuitTrace="
				+ circuitTrace + ", isImplicit=" + isImplicit + ", connList="
				+ connList + ", nodeList=" + nodeList + ", requestTypeCode="
				+ requestTypeCode + ", proposeCircuitId=" + proposeCircuitId
				+ ", statusCode=" + statusCode + ", sharedCircuitTrace="
				+ sharedCircuitTrace + ", isPartialCircuitInUse="
				+ isPartialCircuitInUse + ", disconnectFirstPort="
				+ disconnectFirstPort + ", readOnly=" + readOnly
				+ ", circuitState=" + circuitState + ", circuitUID="
				+ circuitUID + "]";
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public Long getOrigin() {
		return origin;
	}

	public void setOrigin(Long origin) {
		this.origin = origin;
	}

	public boolean isOriginImport() {
		if(origin != null && origin == SystemLookup.ItemOrigen.IMPORT) {
			return true;
		}
		
		return false;
	}
	
	public Long getStartPortId() {
		if(nodeList != null && nodeList.size() > 0){
			if(nodeList.get(0) instanceof PortDTOBase){
				return ((PortDTOBase)nodeList.get(0)).getPortId();
			}
		}
		
		return null;
	}
	
	public Long getEndPortId() {
		if(nodeList != null && nodeList.size() > 0){
			int idx = nodeList.size() - 1;
			if(nodeList.get(idx) instanceof PortDTOBase){
				return ((PortDTOBase)nodeList.get(idx)).getPortId();
			}
		}
		
		return null;
	}

	public void setUsedFlag(boolean value) {
		if(nodeList != null && nodeList.size() > 0){
			for (CircuitNodeInterface node: getNodeList()) {
				if (node instanceof PortDTOBase) {
					((PortDTOBase) node).setUsed(value);
				}
			}
		}
	}	
}