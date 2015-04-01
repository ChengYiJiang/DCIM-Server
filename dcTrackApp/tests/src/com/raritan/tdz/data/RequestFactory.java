package com.raritan.tdz.data;

import java.util.HashMap;
import java.util.List;

import com.raritan.tdz.data.RequestFactoryImpl.RequestInfo;
import com.raritan.tdz.domain.ICircuitInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestPointer;

public interface RequestFactory {

	public abstract Request createRequestForGoingToItemStatus(Item item,
			Long statusValueCode);

	public abstract Request createRequestInstalled(Item item);

	public abstract Request createRequestMove(Item item);

	public abstract Request createRequestOnSite(Item item);

	public abstract Request createRequestToArchive(Item item);

	public abstract Request createRequestToStorage(Item item);

	public abstract Request createRequestPowerOff(Item item);

	public abstract Request createRequestPowerOn(Item item);

	public abstract Request createRequestOffSite(Item item);

	public abstract List<Request> createRequestConvertToVM(Item item);

	public abstract Request createRequestDisconnect(ICircuitInfo circuit);

	public abstract Request createRequestConnect(ICircuitInfo circuit);

	public abstract Request createRequestReConnect(ICircuitInfo circuit);

	public abstract Request createRequest(Item item, String reqType,
			LksData reqTypeLks);

	public abstract RequestPointer createRequetPointer(Request req,
			String tableName);

	public abstract void addPortMoveToRequest(Request request, Item origItem,
			Item moveItem);

	public abstract HashMap<Long, RequestInfo> getMapStatusToReqType();
	
	public abstract Long save(Request request);


}