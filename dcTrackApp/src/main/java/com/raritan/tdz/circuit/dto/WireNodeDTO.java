package com.raritan.tdz.circuit.dto;

public class WireNodeDTO implements WireNodeInterface {
	private String fePortName;
	private String nePortName;
	private Long cordId;
	private Long cordLkuId;
	private String cordLkuDesc;
	private String cordLabel;
	private int cordLength;
	private boolean isUsed;
	private Long cordColor;
	private boolean readOnly;
	private boolean clickable;
	private long id;
	private boolean isSharedConnection;
	private boolean isInputCord; 
	private boolean isPatchCord;

	public boolean isPatchCord() {
		return isPatchCord;
	}

	public void setPatchCord(boolean isPatchCord) {
		this.isPatchCord = isPatchCord;
	}

	public boolean isInputCord() {
		return isInputCord;
	}

	public void setInputCord(boolean isInputCord) {
		this.isInputCord = isInputCord;
	}

	public WireNodeDTO(){
		clickable = true;
	}

	@Override
	public String getFePortName() {
		return fePortName;
	}

	@Override
	public void setFePortName(String fePortName) {
		this.fePortName = fePortName;
	}

	@Override
	public String getNePortName() {
		return nePortName;
	}

	@Override
	public void setNePortName(String nePortName) {
		this.nePortName = nePortName;
	}

	@Override
	public Long getCordId() {
		return cordId;
	}

	@Override
	public void setCordId(Long cordId) {
		this.cordId = cordId;
	}

	@Override
	public Long getCordLkuId() {
		return cordLkuId;
	}

	@Override
	public void setCordLkuId(Long cordLkuId) {
		this.cordLkuId = cordLkuId;
	}

	@Override
	public String getCordLkuDesc() {
		return cordLkuDesc;
	}

	@Override
	public void setCordLkuDesc(String cordLkuDesc) {
		this.cordLkuDesc = cordLkuDesc;
	}

	@Override
	public String getCordLabel() {
		return cordLabel;
	}

	@Override
	public void setCordLabel(String cordLabel) {
		this.cordLabel = cordLabel;
	}

	@Override
	public int getCordLength() {
		return cordLength;
	}

	@Override
	public void setCordLength(int cordLength) {
		this.cordLength = cordLength;
	}

	@Override
	public boolean isUsed() {
		return isUsed;
	}

	@Override
	public void setUsed(boolean isUsed) {
		this.isUsed = isUsed;
	}

	@Override
	public Long getCordColor() {
		return cordColor;
	}

	@Override
	public void setCordColor(Long cordColor) {
		this.cordColor = cordColor;
	}

	@Override
	public boolean getReadOnly() {
		return readOnly;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	@Override
	public boolean getClickable() {
		return clickable;
	}

	@Override
	public void setClickable(boolean clickable) {
		this.clickable = clickable;
	}

	@Override
	public void print(){
		System.out.println("\t\t===========Wire Information=====================");
		System.out.println("\t\t" + this.getClass().getName());
		System.out.println("\t\tNE Port Name: " + this.getNePortName());
		System.out.println("\t\tFE Port Name: " + this.getFePortName());
		System.out.println("\t\tRead Only: " + this.getReadOnly());
		System.out.println("\t\tCord Length: " + this.getCordLength());
		System.out.println("\t\tCord Label: " + this.getCordLabel());;
		System.out.println("\t\tCord Type: " + this.getCordLkuDesc());;
		System.out.println("\t\t================================");
	}

	@Override
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public boolean isLastNode() {
		return false;
	}

	@Override
	public void setLastNode(boolean lastNode) {
		//do nothing
	}
	
	@Override
	public boolean isSharedConnection() {
		return isSharedConnection;
	}

	@Override
	public void setSharedConnection(boolean isSharedConnection) {
		this.isSharedConnection = isSharedConnection;
	}	
	

	public String toString(){
		StringBuffer b = new StringBuffer();
		b.append("\n\t\t===========Wire Information=====================");
		b.append("\n\t\t" + this.getClass().getName());
		b.append("\n\t\tNE Port Name: " + this.getNePortName());
		b.append("\n\t\tFE Port Name: " + this.getFePortName());
		b.append("\n\t\tRead Only: " + this.getReadOnly());
		b.append("\n\t\t================================\n");
		
		return b.toString();
	}
		
}
