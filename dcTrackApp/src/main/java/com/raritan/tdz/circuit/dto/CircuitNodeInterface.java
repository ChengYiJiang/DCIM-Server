package com.raritan.tdz.circuit.dto;

public interface CircuitNodeInterface {
	public abstract boolean getReadOnly();
	public abstract void setReadOnly(boolean readOnly);
	public abstract void print();
	public abstract long getId();
	public abstract boolean isLastNode();
	public abstract void setLastNode(boolean value);
	public abstract boolean getClickable();
	public abstract void setClickable(boolean value);
	public boolean isSharedConnection();
	public void setSharedConnection(boolean isSharedConnection);
}
