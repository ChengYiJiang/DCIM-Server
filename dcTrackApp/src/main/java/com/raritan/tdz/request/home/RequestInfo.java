package com.raritan.tdz.request.home;

public class RequestInfo {

	String itemName;
	
	String requestNumber;
	
	String requestType;

	
	
	public RequestInfo() {
		
	}

	public RequestInfo(String itemName, String requestNumber, String requestType) {
		super();
		this.itemName = itemName;
		this.requestNumber = requestNumber;
		this.requestType = requestType;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getRequestNumber() {
		return requestNumber;
	}

	public void setRequestNumber(String requestNumber) {
		this.requestNumber = requestNumber;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	};
	
	
}
