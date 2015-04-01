package com.raritan.tdz.unit.item.request;

import org.jmock.Mockery;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.unit.item.ItemMock;

public interface ItemMoveMock extends ItemMock {

	public abstract Item getItemToMove();

	public abstract void setItemToMove(Item itemToMove);

	public abstract void addExpectations(Item item, Mockery currentJmockContext);

	boolean isPendingRequest();

	void setPendingRequest(boolean pendingRequest);
	
}