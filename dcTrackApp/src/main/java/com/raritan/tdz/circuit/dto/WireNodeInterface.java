package com.raritan.tdz.circuit.dto;

public interface WireNodeInterface extends CircuitNodeInterface {

	public abstract String getFePortName();

	public abstract void setFePortName(String fePortName);

	public abstract String getNePortName();

	public abstract void setNePortName(String nePortName);

	public abstract Long getCordId();

	public abstract void setCordId(Long cordId);

	public abstract Long getCordLkuId();

	public abstract void setCordLkuId(Long cordLkuId);

	public abstract String getCordLkuDesc();

	public abstract void setCordLkuDesc(String cordLkuDesc);

	public abstract String getCordLabel();

	public abstract void setCordLabel(String cordLabel);

	public abstract int getCordLength();

	public abstract void setCordLength(int cordLength);

	public abstract boolean isUsed();

	public abstract void setUsed(boolean isUsed);

	public abstract Long getCordColor();

	public abstract void setCordColor(Long cordColor);
	
	public abstract boolean getClickable();
	
	public abstract void setClickable(boolean isClickable);
	
	public abstract void print();
	
	public abstract void setInputCord(boolean isInputCord);
	
	public abstract boolean isInputCord();
	
	public abstract void setPatchCord(boolean isPatchCord);
	
	public abstract boolean isPatchCord();
}
