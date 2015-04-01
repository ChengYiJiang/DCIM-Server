package com.raritan.tdz.views;
import java.util.List;

public class ItemPortObject {
	private List<DataPortObject> dataPortList;
	private List<PowerPortObject> powerPortList;
	
	public ItemPortObject(){
		dataPortList = null;
		powerPortList = null;
	}

	public List<DataPortObject> getDataPortList() {
		return dataPortList;
	}

	public void setDataPortList(List<DataPortObject> dataPortList) {
		this.dataPortList = dataPortList;
	}

	public List<PowerPortObject> getPowerPortList() {
		return powerPortList;
	}

	public void setPowerPortList(List<PowerPortObject> powerPortList) {
		this.powerPortList = powerPortList;
	}
}
