package com.raritan.tdz.views;

import java.util.List;


public class CircuitObject {

	private Long circuitId;
	private Long  startConnId;
	private Long  endConnId;
	private String circuitTrace;
	private boolean isImplicit;
	private boolean isPower;
	
	private List<DataConnObject> dataConnList;
	private List<PowerConnObject> powerConnList;
	
	//Fields for creating connection for panels and switches
	private boolean autoConnectPanel;
	private List<DataPortObject> sourcePortList;
	private List<DataPortObject> destPortList; //item to be connect to panel 
	
	public CircuitObject(){
		dataConnList = null;
		powerConnList = null;
		sourcePortList = null;
		destPortList = null;
		isPower = false;
	}
	
	public Long getCircuitId() {
		return circuitId;
	}

	public void setCircuitId(Long circuitId) {
		this.circuitId = circuitId;
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

	public List<DataConnObject> getDataConnList() {
		return dataConnList;
	}

	public void setDataConnList(List<DataConnObject> dataConnList) {
		this.dataConnList = dataConnList;
		this.isPower = false;
	}

	public void setPower(boolean isPower) {
		this.isPower = isPower;
	}

	public boolean isPower() {		
		return isPower;
	}

	public void setPowerConnList(List<PowerConnObject> powerConnList) {
		this.powerConnList = powerConnList;
		this.isPower = true;
	}

	public List<PowerConnObject> getPowerConnList() {
		return powerConnList;
	}

	public boolean isAutoConnectPanel() {
		return autoConnectPanel;
	}

	public void setAutoConnectPanel(boolean autoConnectPanel) {
		this.autoConnectPanel = autoConnectPanel;
	}

	public List<DataPortObject> getSourcePortList() {
		return sourcePortList;
	}

	public void setSourcePortList(List<DataPortObject> sourcePortList) {
		this.sourcePortList = sourcePortList;
	}

	public List<DataPortObject> getDestPortList() {
		return destPortList;
	}

	public void setDestPortList(List<DataPortObject> destPortList) {
		this.destPortList = destPortList;
	}
	
}
