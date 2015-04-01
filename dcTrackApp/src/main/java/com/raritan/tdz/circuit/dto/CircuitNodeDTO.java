package com.raritan.tdz.circuit.dto;

public class CircuitNodeDTO implements CircuitNodeInterface {
	
	private boolean isSharedConnection;
	
	public CircuitNodeDTO(){
		
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setLastNode(boolean value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getClickable() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void setClickable(boolean value) {
		// TODO Auto-generated method stub
		
	}	
	
	@Override
	public boolean isSharedConnection() {
		return isSharedConnection;
	}
	
	@Override
	public void setSharedConnection(boolean isSharedConnection) {
		this.isSharedConnection = isSharedConnection;
	}	
}
