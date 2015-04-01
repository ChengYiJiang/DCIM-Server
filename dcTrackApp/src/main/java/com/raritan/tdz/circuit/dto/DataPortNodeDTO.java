package com.raritan.tdz.circuit.dto;
import com.raritan.tdz.dto.DataPortDTO;
import com.raritan.tdz.views.ItemObject;
import com.raritan.tdz.lookup.SystemLookup;

public class DataPortNodeDTO extends DataPortDTO implements PortNodeInterface {
	private ItemObject itemObject;
	private boolean readOnly;
	private long id;
	private boolean lastNode;
	private boolean clickable;

	public DataPortNodeDTO(){
		lastNode = false;
	}

	@Override
	public ItemObject getItemObject() {
		return itemObject;
	}

	@Override
	public void setItemObject(ItemObject itemObject) {
		this.itemObject = itemObject;
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
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void print(){
		System.out.println("===========Node Information=====================");
		System.out.println("Item Name: " + this.getItemName());
		System.out.println("Port Name: " + this.getPortName());
		System.out.println("Port Id: " + this.getPortId());
		System.out.println("Is Used: " + this.isUsed());
		System.out.println("Read Only: " + this.getReadOnly());
		System.out.println("Port Sub Class Code: " + this.getPortSubClassLksValueCode());
		System.out.println("================================");
	}

	@Override
	public boolean isLastNode() {
		return lastNode;
	}

	@Override
	public void setLastNode(boolean lastNode) {
		this.lastNode = lastNode;
	}

	@Override
	public boolean getClickable() {
		return clickable;
	}

	@Override
	public void setClickable(boolean clickable) {
		this.clickable = clickable;
	}

	public String toString(){
		StringBuffer b = new StringBuffer();
		b.append("\n===========Node Information=====================");
		b.append("\nItem Name: " + this.getItemName());
		b.append("\nPort Name: " + this.getPortName());
		b.append("\nPort Id: " + this.getPortId());
		b.append("\nIs Used: " + this.isUsed());
		b.append("\nRead Only: " + this.getReadOnly());
		b.append("\nPort Sub Class Code: " + this.getPortSubClassLksValueCode());
		b.append("\nItem Class Code: " + this.getItemClassLksValueCode());
		b.append("\n================================\n");

		return b.toString();
	}

	@Override
	public boolean isVpcPowerOutlet() {
		// TODO Auto-generated method stub
		return false;
	}
}

