package com.raritan.tdz.component.inspector.home.impl;


public class PowerPortMetric{
	public String itemName;
	public Long itemId;
	public Long itemClassValueCode;
	public String portName;
	public Long portSubclassValueCode;
	public boolean isSinglePhase;
	public String connectedTo;
	public String ampsRBM;
	public String ampsL1RBM;
	public String ampsL2RBM;
	public String ampsL3RBM;
	public String powerSource;
	
	@Override
	public String toString() {
		return "PowerPortMetric [itemName=" + itemName + ", itemId=" + itemId
				+ ", itemClassValueCode=" + itemClassValueCode + ", portName="
				+ portName + ", portSubclassValueCode=" + portSubclassValueCode
				+ ", isSinglePhase= " + isSinglePhase + ", connectedTo= "
				+ connectedTo + ", ampsRBM= " + ampsRBM + ", ampsL1RBM= "
				+ ampsL1RBM + ", ampsL2RBM= " + ampsL2RBM + ", ampsL3RBM= "
				+ ampsL3RBM + ", powerSource= " + powerSource + "]";
	}
	
	
}
