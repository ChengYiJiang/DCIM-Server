package com.raritan.tdz.circuit.dto;
import com.raritan.tdz.views.ItemObject;

public interface PortNodeInterface extends CircuitNodeInterface {
	public abstract ItemObject getItemObject();
	public abstract void setItemObject(ItemObject itemObject);
	
	/**
	 * If this node is the first node of a shared partial circuit, it will
	 * return the length of the partial circuit. Otherwise, returns 0.
	 * @return
	 */
	public abstract int getPartialCircuitLength();
	
	public abstract void setPartialCircuitLength(int partialCircuitLength);
	
	public abstract boolean isVpcPowerOutlet();
	
}
