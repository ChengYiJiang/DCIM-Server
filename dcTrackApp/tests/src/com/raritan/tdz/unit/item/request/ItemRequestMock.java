package com.raritan.tdz.unit.item.request;

import org.jmock.Mockery;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.domain.RequestPointer;

public interface ItemRequestMock {

	public abstract Request createRequestInstalled(Item item);

	public abstract Request createRequestMove(Item item, boolean pendingRequest);

	public abstract Request createRequestOnSite(Item item);

	public abstract Request createRequestToArchive(Item item);

	public abstract Request createRequestToStorage(Item item);

	public abstract Request createRequestPowerOff(Item item);

	public abstract Request createRequestPowerOn(Item item);

	public abstract Request createRequestOffSite(Item item);

	public abstract Request createRequest(Item item, String reqType);

	public abstract RequestHistory createHistory(Request req,
			Long stageValueCode);

	public abstract RequestPointer createRequetPointer(Request req,
			String tableName);

	public void addExpectations(Request requestObj, Mockery currentJmockContext);
	
	public Mockery getJmockContext();
	public void setJmockContext(Mockery jmockContext);
}