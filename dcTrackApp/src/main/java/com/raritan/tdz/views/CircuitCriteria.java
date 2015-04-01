package com.raritan.tdz.views;

public class CircuitCriteria {
	public Long portId;
	public Long startPortId;
	public Long connectionId;
	public Long itemId;
	public Long circuitId;
	public String circuitTrace;
	public Long startConnId;
	public Long endConnId;
	public Long locationId;
	//private boolean returnListView; 
	public String containCircuitTrace;

	public CircuitCriteria(){
		
	}
	
	public CircuitCriteria(Long circuitId){
		this.circuitId = circuitId;
	}
	
	public void clear(){
		portId = null;
		startPortId = null;
		connectionId = null;
		itemId = null;
		circuitId = null;
		circuitTrace = null;
		startConnId = null;
		endConnId = null;
		locationId = null;		
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
	public Long getCircuitId() {
		return circuitId;
	}
	public void setCircuitId(Long circuitId) {
		this.circuitId = circuitId;
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
	/*public void setReturnListView(boolean returnListView) {
		this.returnListView = returnListView;
	}
	public boolean isReturnListView() {
		return returnListView;
	}*/
	public void setContainCircuitTrace(String containCircuitTrace) {
		this.containCircuitTrace = containCircuitTrace;
	}
	public String getContainCircuitTrace() {
		return containCircuitTrace;
	}
}

