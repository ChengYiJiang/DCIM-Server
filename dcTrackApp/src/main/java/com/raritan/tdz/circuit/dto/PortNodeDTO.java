package com.raritan.tdz.circuit.dto;

import com.raritan.tdz.views.ItemObject;

public class PortNodeDTO implements PortNodeInterface {
	private boolean isSharedConnection;
	private int partialCircuitLength = 0;
	
	public PortNodeDTO(){
		
	}

	@Override
	public ItemObject getItemObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setItemObject(ItemObject itemObject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void print() {
		// TODO Auto-generated method stub
		
	}	

	@Override
	public boolean isLastNode() {
		return false;
	}

	@Override
	public void setLastNode(boolean lastNode) {
		//do nothing;
	}

	@Override
	public boolean getClickable() {
		return true;
	}

	@Override
	public void setClickable(boolean clickable) {
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

	@Override
	public int getPartialCircuitLength() {
		return partialCircuitLength;
	}

	@Override
	public void setPartialCircuitLength(int partialCircuitLength) {
		this.partialCircuitLength = partialCircuitLength;
	}

	@Override
	public boolean isVpcPowerOutlet() {
		// TODO Auto-generated method stub
		return false;
	}	
}
