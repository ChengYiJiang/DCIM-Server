package com.raritan.tdz.circuit.dto;

public class ConnCordDTO {
	private Long cordId;
	private Long cordLkuId;
	private String cordLabel;
	private int cordLength;
	private boolean isUsed;
	private Long cordColor;
	
	public ConnCordDTO(){
		
	}
	
	public Long getCordId() {
		return cordId;
	}
	public void setCordId(Long cordId) {
		this.cordId = cordId;
	}
	public Long getCordLkuId() {
		return cordLkuId;
	}
	public void setCordLkuId(Long cordLkuId) {
		this.cordLkuId = cordLkuId;
	}
	public String getCordLabel() {
		return cordLabel;
	}
	public void setCordLabel(String cordLabel) {
		this.cordLabel = cordLabel;
	}
	public int getCordLength() {
		return cordLength;
	}
	public void setCordLength(int cordLength) {
		this.cordLength = cordLength;
	}
	public boolean isUsed() {
		return isUsed;
	}
	public void setUsed(boolean isUsed) {
		this.isUsed = isUsed;
	}
	public Long getCordColor() {
		return cordColor;
	}
	public void setCordColor(Long cordColor) {
		this.cordColor = cordColor;
	}
	
		
}
