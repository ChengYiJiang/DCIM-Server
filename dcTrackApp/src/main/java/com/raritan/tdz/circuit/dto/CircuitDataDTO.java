package com.raritan.tdz.circuit.dto;

import java.util.List;
import  com.raritan.tdz.dto.DataPortDTO;

public class CircuitDataDTO extends CircuitDTO {
	//Fields for creating connection for panels and switches
	private boolean autoConnectPanel;
	private List<DataPortDTO> sourcePortList;
	private List<DataPortDTO> destPortList; //item to be connect to panel
	
	public CircuitDataDTO(){
		
	}
	
	public boolean isAutoConnectPanel() {
		return autoConnectPanel;
	}
	public void setAutoConnectPanel(boolean autoConnectPanel) {
		this.autoConnectPanel = autoConnectPanel;
	}
	public List<DataPortDTO> getSourcePortList() {
		return sourcePortList;
	}
	public void setSourcePortList(List<DataPortDTO> sourcePortList) {
		this.sourcePortList = sourcePortList;
	}
	public List<DataPortDTO> getDestPortList() {
		return destPortList;
	}
	public void setDestPortList(List<DataPortDTO> destPortList) {
		this.destPortList = destPortList;
	}
		
}
